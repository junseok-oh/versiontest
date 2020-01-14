package com.deliveryhero.alfred.connector.adyen.common.util

import com.adyen.service.exception.ApiException
import com.deliveryhero.alfred.connector.sdk.operation.OperationType
import com.deliveryhero.alfred.connector.sdk.operation.request.*
import com.deliveryhero.alfred.connector.sdk.operation.response.OperationResponse
import com.deliveryhero.alfred.connector.sdk.operation.response.OperationStatus
import com.deliveryhero.alfred.connector.sdk.operation.response.redirect.RedirectRequest
import com.google.gson.GsonBuilder
import com.adyen.model.PaymentResult as AdyenApiPaymentResult
import com.adyen.model.checkout.PaymentsRequest as AdyenApiAuthorizeRequest
import com.adyen.model.checkout.PaymentsResponse as AdyenApiPaymentsResponse
import com.adyen.model.modification.CancelOrRefundRequest as AdyenApiCancelOrRefundRequest
import com.adyen.model.modification.CancelRequest as AdyenApiCancelRequest
import com.adyen.model.modification.CaptureRequest as AdyenApiCaptureRequest
import com.adyen.model.modification.ModificationResult as AdyenApiModificationResult

object AdyenResponseParser {

    private val GSON = GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX").create()

    val ADDITIONAL_DATA_RECURRING_DETAIL_REFERENCE = "recurring.recurringDetailReference"

    val ADYEN_SUCCESS_CODES: Set<AdyenApiPaymentsResponse.ResultCodeEnum> = setOf(
        AdyenApiPaymentsResponse.ResultCodeEnum.AUTHORISED,
        AdyenApiPaymentsResponse.ResultCodeEnum.PARTIALLYAUTHORISED,
        AdyenApiPaymentsResponse.ResultCodeEnum.AUTHENTICATIONFINISHED,
        AdyenApiPaymentsResponse.ResultCodeEnum.AUTHENTICATIONNOTREQUIRED
    )

    val ADYEN_CREATED_CODE: AdyenApiPaymentsResponse.ResultCodeEnum = AdyenApiPaymentsResponse.ResultCodeEnum.RECEIVED

    val ADYEN_PENDING_CODES: Set<AdyenApiPaymentsResponse.ResultCodeEnum> = setOf(
        AdyenApiPaymentsResponse.ResultCodeEnum.REDIRECTSHOPPER,
        AdyenApiPaymentsResponse.ResultCodeEnum.IDENTIFYSHOPPER,
        AdyenApiPaymentsResponse.ResultCodeEnum.CHALLENGESHOPPER,
        AdyenApiPaymentsResponse.ResultCodeEnum.PENDING,
        AdyenApiPaymentsResponse.ResultCodeEnum.PRESENTTOSHOPPER
    )

    val ADYEN_FAILURE_CODES: Set<AdyenApiPaymentsResponse.ResultCodeEnum> = setOf(
        AdyenApiPaymentsResponse.ResultCodeEnum.REFUSED,
        AdyenApiPaymentsResponse.ResultCodeEnum.ERROR,
        AdyenApiPaymentsResponse.ResultCodeEnum.CANCELLED
    )

    val ADYEN_MODIFICATION_PENDING_CODES: Set<AdyenApiModificationResult.ResponseEnum> = setOf(
        AdyenApiModificationResult.ResponseEnum.CAPTURE_RECEIVED_,
        AdyenApiModificationResult.ResponseEnum.CANCEL_RECEIVED_,
        AdyenApiModificationResult.ResponseEnum.REFUND_RECEIVED_,
        AdyenApiModificationResult.ResponseEnum.CANCELORREFUND_RECEIVED_,
        AdyenApiModificationResult.ResponseEnum.ADJUSTAUTHORISATION_RECEIVED_
    )

    val ADYEN_UNKNOWN_CODE: AdyenApiPaymentsResponse.ResultCodeEnum = AdyenApiPaymentsResponse.ResultCodeEnum.UNKNOWN

    val ADYEN_REFUSAL_REASON_FRAUD = "FRAUD"
    val ADYEN_PA_REQUEST = "PaReq"
    val ADYEN_MD = "MD"

    private enum class PaymentRejection constructor(val code: String) {
        INVALID_CARD_EXPIRY_MONTH("129"),
        INVALID_CARD_NUMBER("101"),
        CVC_IS_NOT_THE_RIGHT_LENGTH("103"),
        INVALID_PA_RES_FROM_ISSUER("105"),
        RECURRING_IS_NOT_ENABLED("107"),
        INVALID_BANK_ACCOUNT_NUMBER("108"),
        THIS_BANK_COUNTRY_IS_NOT_SUPPORTED("112"),
        REQUEST_ALREADY_PROCESSED("704"),
        PAYMENT_DETAILS_ARE_NOT_SUPPORTED("905"),
        PAYMENT_DETAILS_ARE_NOT_FOUND("803"),
        US_PAYMENT_DETAILS_ARE_NOT_SUPPORTED("907")
    }

    private val PAYMENT_REJECTION_CODES = HashSet<String>()

    init {
        for (type in PaymentRejection.values()) {
            PAYMENT_REJECTION_CODES.add(type.code)
        }
    }

    fun transformAuthorizeResponse(
        operationRequest: OperationRequest,
        paymentsRequest: AdyenApiAuthorizeRequest,
        paymentsResponse: AdyenApiPaymentsResponse?,
        ex: Exception?
    ): OperationResponse =
        when {
            paymentsResponse != null -> {

                var redirectRequest: RedirectRequest? = null
                if (paymentsResponse.redirect?.url != null) {
                    redirectRequest = RedirectRequest(paymentsResponse.redirect.url)

                    if (paymentsResponse.action.data.containsKey(ADYEN_PA_REQUEST)) {
                        redirectRequest.requestParameters[ADYEN_PA_REQUEST] =
                            paymentsResponse.action.data.get(ADYEN_PA_REQUEST)!!
                    }
                    if (paymentsResponse.action.data.containsKey(ADYEN_MD)) {
                        redirectRequest.requestParameters[ADYEN_MD] = paymentsResponse.action.data.get(ADYEN_MD)!!
                    }
                }
                OperationResponse(
                    status = convertStatusAndErrorCode(paymentsResponse),
                    reference = paymentsResponse.additionalData[ADDITIONAL_DATA_RECURRING_DETAIL_REFERENCE],
                    redirectRequest = redirectRequest,
                    rawStatus = paymentsResponse.resultCode.value,
                    rawResponse = toJsonStringOrNull(paymentsResponse, "Adyen - Authorize - {} - RESPONSE: {}"),
                    type = OperationType.AUTHORIZE
                ).apply {
                    request = operationRequest
                }
//              //TODO:: where should the following go??
//                cardToken = result.getAdditionalDataByKey(ADDITIONAL_DATA_RECURRING_DETAIL_REFERENCE)
//                expiryDate = result.getAdditionalDataByKey(ApiConstants.AdditionalData.EXPIRY_DATE)
//                cardHolder = result.cardHolderName
//                scheme = result.paymentMethod
//                bin = result.cardBin
//                displayName = result.cardSummary
//            }
            }
            ex != null -> {
                //TODO log
                //logger.error(ADYEN_ERROR_LOG_PATTERN, ex.toString(), response.getRequestJson())
                applyExceptionResult(operationRequest, ex, OperationType.AUTHORIZE).let {
                    when {
                        ex is ApiException && PAYMENT_REJECTION_CODES.contains(ex.error?.errorCode) ->
                            it.copy(status = OperationStatus.DO_NOT_HONOR) //should be replaced by more specific
                        else -> it
                    }
                }
            }
            else -> OperationResponse.buildUnkownError()
        }

    private fun convertStatusAndErrorCode(result: AdyenApiPaymentsResponse) = when (result.resultCode) {
        in ADYEN_SUCCESS_CODES -> OperationStatus.OK
        ADYEN_CREATED_CODE -> OperationStatus.CREATED
        in ADYEN_PENDING_CODES -> OperationStatus.PENDING
        in ADYEN_FAILURE_CODES -> when {
            //refusalreason is set when payment is refused or an error is encountered
            ADYEN_REFUSAL_REASON_FRAUD.equals(result.refusalReason, ignoreCase = true) -> OperationStatus.FRAUD_RISK
            //should be replaced by more specific
            else -> OperationStatus.DO_NOT_HONOR
        }
        ADYEN_FAILURE_CODES -> OperationStatus.UNKNOWN_ERROR
        else -> OperationStatus.UNKNOWN_ERROR
    }

    private fun convertStatusAndErrorCodeForModificationRequests(result: AdyenApiModificationResult) =
        when (result.response) {
            in ADYEN_MODIFICATION_PENDING_CODES -> OperationStatus.PENDING
            else -> OperationStatus.UNKNOWN_ERROR

        }

    fun transformCaptureResponse(
        operationRequest: CaptureRequest,
        adyenRequest: AdyenApiCaptureRequest,
        adyenResult: AdyenApiModificationResult?,
        ex: Exception?
    ): OperationResponse = transformModificationResult(operationRequest, adyenResult, OperationType.CAPTURE, ex)

    fun transformCancelResponse(
        operationRequest: CancelRequest,
        adyenRequest: AdyenApiCancelRequest,
        adyenResult: AdyenApiModificationResult?,
        ex: Exception?
    ): OperationResponse = transformModificationResult(operationRequest, adyenResult, OperationType.CANCEL, ex)

    fun transformRefundResponse(
        oprationRequest: RefundRequest,
        request: AdyenApiCancelOrRefundRequest,
        result: AdyenApiModificationResult?,
        ex: Exception?
    ): OperationResponse = transformModificationResult(oprationRequest, result, OperationType.REFUND, ex)

    private fun transformModificationResult(
        modificationRequest: ModificationRequest,
        adyenApiResult: AdyenApiModificationResult?,
        type: OperationType,
        ex: Exception?
    ): OperationResponse = when {
        adyenApiResult != null -> OperationResponse(
            status = convertStatusAndErrorCodeForModificationRequests(adyenApiResult),
            reference = adyenApiResult.pspReference,
            rawStatus = adyenApiResult.response.toString(),
            rawResponse = toJsonStringOrNull(adyenApiResult.response, "Adyen - $type - {} - RESPONSE: {}"),
            type = type
        ).apply {
            request = modificationRequest
        }
        ex != null -> applyExceptionResult(modificationRequest, ex, type)
        else -> OperationResponse.buildUnkownError()
    }
    //TODO log
    //logger.error(ADYEN_ERROR_LOG_PATTERN, ex.toString(), response.getRequestJson())

    fun toJsonStringOrNull(source: Any, errorMessageFormat: String): String? {
        try {
            return GSON.toJson(source)
        } catch (e: Exception) {
            //TODO log
            //logger.error(errorMessageFormat, e.toString(), source.toString())
        }
        return null
    }

    fun applyExceptionResult(
        operationRequest: OperationRequest,
        ex: Exception,
        type: OperationType
    ): OperationResponse =
        when (ex) {
            is ApiException -> when (ex.error) {
                null -> OperationResponse(
                    rawStatus = "ApiException: ${ex.statusCode}",
                    rawResponse = ex.message,
                    type = type,
                    status = OperationStatus.UNKNOWN_ERROR
                )
                else -> OperationResponse(
                    rawStatus = "ApiError: ${ex.error.errorType} | ${ex.error.status}",
                    rawResponse = toJsonStringOrNull(ex.error, "Adyen - $type - {} - RESPONSE: {}"),
                    type = type,
                    status = OperationStatus.UNKNOWN_ERROR
                )
            }
            else -> OperationResponse(
                rawStatus = ex.message,
                rawResponse = ex.toString(),
                type = type,
                status = OperationStatus.UNKNOWN_ERROR
            )
        }.apply {
            request = operationRequest
        }

// The conversion functions below are for ResultCodeEnum of the older Adyen Java API library.
// The main purpose is to support the current implementation for Adyen Card Payment.

    fun toOldResultCodeEnum(text: String): AdyenApiPaymentResult.ResultCodeEnum? =
        AdyenApiPaymentResult.ResultCodeEnum.values().firstOrNull { it.toString() == text }

    fun toOldResultCodeEnum(code: AdyenApiPaymentsResponse.ResultCodeEnum): AdyenApiPaymentResult.ResultCodeEnum? =
        toOldResultCodeEnum(code.toString())

    fun toNewResultCodeEnum(text: String): AdyenApiPaymentsResponse.ResultCodeEnum? =
        AdyenApiPaymentsResponse.ResultCodeEnum.values().firstOrNull { it.toString() == text }

    fun toNewResultCodeEnum(code: AdyenApiPaymentResult.ResultCodeEnum): AdyenApiPaymentsResponse.ResultCodeEnum? =
        toNewResultCodeEnum(code.toString())
}

