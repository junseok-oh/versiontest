package com.deliveryhero.alfred.connector.adyen.common.util

import com.adyen.model.checkout.PaymentsRequest
import com.adyen.model.checkout.PaymentsResponse
import com.deliveryhero.alfred.connector.*
import com.deliveryhero.alfred.connector.adyen.common.util.AdyenResponseParser.ADDITIONAL_DATA_RECURRING_DETAIL_REFERENCE
import com.deliveryhero.alfred.connector.sdk.operation.request.common.Currency
import com.deliveryhero.alfred.connector.sdk.operation.response.OperationResponse
import com.deliveryhero.alfred.connector.sdk.operation.response.OperationStatus
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.junit.jupiter.MockitoExtension
import java.util.*

/**
 * Tests for [AdyenResponseParser].
 *
 * @author mathifonseca
 * Ported to Kotlin by junseok.oh
 */
@ExtendWith(MockitoExtension::class)
class AdyenResponseParserTest {

    @Test
    fun `should the result code categories contain all the values of ResultCodeEnum`() {
        val union = ArrayList<PaymentsResponse.ResultCodeEnum>()
        union.addAll(AdyenResponseParser.ADYEN_SUCCESS_CODES)
        union.addAll(AdyenResponseParser.ADYEN_FAILURE_CODES)
        union.addAll(AdyenResponseParser.ADYEN_PENDING_CODES)
        union.add(AdyenResponseParser.ADYEN_CREATED_CODE)
        union.add(AdyenResponseParser.ADYEN_UNKNOWN_CODE)
        assertTrue(union.containsAll(EnumSet.allOf(PaymentsResponse.ResultCodeEnum::class.java)))
    }

    //TODO::Test related to adyencreditcard which has methods not implemented in COMMON LIB
//    @Test
//    fun testTransformDisableCardResponse_Success() {
//        val adyenDisableCardRequest = AdyenTestUtils.createAdyenDisableCardsRequest()
//        val request = AdyenRequestBuilder.prepareDisableCardRequest(adyenDisableCardRequest)
//        val result = AdyenTestUtils.createAdyenDisableResult(true)
//
//        val response = AdyenResponseParser.transformDisableCardResponse(request, result, null)
//
//        assertNotNull(response)
//        assertTrue(response.isSuccess())
//        assertNull(response.getOriginalResult())
//        assertEquals(AdyenResponseParser.toJsonStringOrNull(request, ""), response.getRequestJson())
//        assertEquals(AdyenResponseParser.toJsonStringOrNull(result, ""), response.getResponseJson())
//    }

    //TODO::Test related to adyencreditcard which has methods not implemented in COMMON LIB
//    @Test
//    fun testTransformDisableCardResponse_Failure() {
//        val adyenDisableCardRequest = AdyenTestUtils.createAdyenDisableCardsRequest()
//        val request = AdyenRequestBuilder.prepareDisableCardRequest(adyenDisableCardRequest)
//        val result = AdyenTestUtils.createAdyenDisableResult(false)
//
//        val response = AdyenResponseParser.transformDisableCardResponse(request, result, null)
//
//        assertNotNull(response)
//        assertFalse(response.isSuccess())
//        assertNull(response.getOriginalResult())
//        assertEquals(AdyenResponseParser.toJsonStringOrNull(request, ""), response.getRequestJson())
//        assertEquals(AdyenResponseParser.toJsonStringOrNull(result, ""), response.getResponseJson())
//    }

    //TODO::Test related to adyencreditcard which has methods not implemented in COMMON LIB
//    @Test
//    fun testTransformDisableCardResponse_Exception() {
//        val adyenDisableCardRequest = AdyenTestUtils.createAdyenDisableCardsRequest()
//        val request = AdyenRequestBuilder.prepareDisableCardRequest(adyenDisableCardRequest)
//        val ex = Exception("test")
//
//        val response = AdyenResponseParser.transformDisableCardResponse(request, null, ex)
//
//        assertNotNull(response)
//        assertFalse(response.isSuccess())
//        assertEquals(AdyenResponseParser.toJsonStringOrNull(request, ""), response.getRequestJson())
//        assertEquals(ex.message, response.getOriginalResult())
//        assertEquals(ex.message, response.getResponseJson())
//    }

    //TODO::Test related to adyencreditcard which has methods not implemented in COMMON LIB
//    @Test
//    fun `transformAuthorizeResponse - success with deprecated functions for card payments`() {
//        val adyenAuthorizeRequest = AdyenTestUtils.createAuthorizeRequest()
//        val request = AdyenRequestBuilder.prepareAuthorizeRequest(adyenAuthorizeRequest)
//
//        for (status in AdyenResponseParser.ADYEN_SUCCESS_CODES.map(::toOldResultCodeEnum).filterNotNull()) {
//            val result = AdyenTestUtils.createAdyenPaymentResult(status!!)
//            val response = AdyenResponseParser.transformAuthorizeResponse(request, result, null)
//
//            assertAuthorizeResponse(request, result, response)
//            assertEquals(OperationStatus.OK, response.status)
//        }
//    }

    @Test
    fun `transformAuthorizeResponse - success for one time payment methods`() {
        val authorizeRequest =
            mockAuthorizeRequest(paymentInstrument = mockOneTimePayment())
        val paymentsRequest = AdyenRequestBuilder.prepareAuthorizeRequests(authorizeRequest)

        for (status in AdyenResponseParser.ADYEN_SUCCESS_CODES) {
            val paymentsResponse = mockPaymentsResponse(status)
            val operationResponse = AdyenResponseParser.transformAuthorizeResponse(
                authorizeRequest, paymentsRequest, paymentsResponse, null
            )

            assertAuthorizeResponse(paymentsRequest, paymentsResponse, operationResponse)
            assertEquals(OperationStatus.OK, operationResponse.status)
        }
    }

    //TODO::Test related to adyencreditcard which has methods not implemented in COMMON LIB
//    @Test
//    fun `transformAuthorizeResponse - pending with deprecated functions for card payments`() {
//        val adyenAuthorizeRequest = AdyenTestUtils.createAdyenAuthorizeRequest()
//        val request: PaymentRequest = AdyenRequestBuilder.prepareAuthorizeRequest(adyenAuthorizeRequest)
//
//        for (status in AdyenResponseParser.ADYEN_PENDING_CODES.map(::toOldResultCodeEnum).filterNotNull()) {
//            val result: PaymentResult = AdyenTestUtils.createAdyenPaymentResult(status!!)
//            val response: AdyenAuthorizeResponse = AdyenResponseParser.transformAuthorizeResponse(request, result, null)
//
//            assertAuthorizeResponse(request, result, response)
//            assertEquals(OperationStatus.PENDING, response.status)
//        }
//    }

    @Test
    fun `transformAuthorizeResponse - pending for one time payment methods`() {
        val authorizeRequest =
            mockAuthorizeRequest(paymentInstrument = mockOneTimePayment())
        val request = AdyenRequestBuilder.prepareAuthorizeRequests(authorizeRequest)

        for (status in AdyenResponseParser.ADYEN_PENDING_CODES) {
            val result = mockPaymentsResponse(status)
            val response = AdyenResponseParser.transformAuthorizeResponse(authorizeRequest, request, result, null)

            assertAuthorizeResponse(request, result, response)
            assertEquals(OperationStatus.PENDING, response.status)
        }
    }

    //TODO::Test related to adyencreditcard which has methods not implemented in COMMON LIB
//    @Test
//    fun `transformAuthorizeResponse - failure (rejected) with deprecated functions for card payments`() {
//        val adyenAuthorizeRequest = AdyenTestUtils.createAdyenAuthorizeRequest()
//        val request: PaymentRequest = AdyenRequestBuilder.prepareAuthorizeRequest(adyenAuthorizeRequest)
//
//        for (status in AdyenResponseParser.ADYEN_FAILURE_CODES.map(::toOldResultCodeEnum).filterNotNull()) {
//            val result: PaymentResult = AdyenTestUtils.createAdyenPaymentResult(status!!)
//            val response: AdyenAuthorizeResponse = AdyenResponseParser.transformAuthorizeResponse(request, result, null)
//
//            assertAuthorizeResponse(request, result, response)
//            assertEquals(OperationStatus.DO_NOT_HONOR, response.status) // should be more specific
//        }
//    }

    @Test
    fun `transformAuthorizeResponse - failure (rejected) for one time payment methods`() {
        val authorizeRequest =
            mockAuthorizeRequest(paymentInstrument = mockOneTimePayment())
        val request = AdyenRequestBuilder.prepareAuthorizeRequests(authorizeRequest)

        for (status in AdyenResponseParser.ADYEN_FAILURE_CODES) {
            val result = mockPaymentsResponse(status)
            val response = AdyenResponseParser.transformAuthorizeResponse(authorizeRequest, request, result, null)

            assertAuthorizeResponse(request, result, response)
            assertEquals(OperationStatus.DO_NOT_HONOR, response.status) // should be more specific
        }
    }

    //TODO::Test related to adyencreditcard which has methods not implemented in COMMON LIB
//    @Test
//    fun `transformAuthorizeResponse - failure (fraud) with deprecated functions for card payments`() {
//        val adyenAuthorizeRequest = AdyenTestUtils.createAdyenAuthorizeRequest()
//        val request: PaymentRequest = AdyenRequestBuilder.prepareAuthorizeRequest(adyenAuthorizeRequest)
//        val result: PaymentResult = AdyenTestUtils.createAdyenPaymentResult(PaymentResult.ResultCodeEnum.REFUSED, "FRAUD")
//        val response: AdyenAuthorizeResponse = AdyenResponseParser.transformAuthorizeResponse(request, result, null)
//
//        assertAuthorizeResponse(request, result, response)
//        assertEquals(OperationStatus.FRAUD_RISK, response.status)
//    }

    @Test
    fun `transformAuthorizeResponse - failure (fraud) for one time payment methods`() {
        val authorizeRequest =
            mockAuthorizeRequest(paymentInstrument = mockOneTimePayment())
        val request = AdyenRequestBuilder.prepareAuthorizeRequests(authorizeRequest)
        val result = mockPaymentsResponse(PaymentsResponse.ResultCodeEnum.REFUSED, "FRAUD")
        val response = AdyenResponseParser.transformAuthorizeResponse(authorizeRequest, request, result, null)

        assertAuthorizeResponse(request, result, response)
        assertEquals(OperationStatus.FRAUD_RISK, response.status)
    }

    //TODO::Test related to adyencreditcard which has methods not implemented in COMMON LIB
//    @Test
//    fun `transformAuthorizeResponse - created with deprecated functions for card payments`() {
//        val adyenAuthorizeRequest = AdyenTestUtils.createAdyenAuthorizeRequest()
//        val request: PaymentRequest = AdyenRequestBuilder.prepareAuthorizeRequest(adyenAuthorizeRequest)
//        val result: PaymentResult = AdyenTestUtils.createAdyenPaymentResult(toOldResultCodeEnum(AdyenResponseParser.ADYEN_CREATED_CODE)!!)
//        val response: AdyenAuthorizeResponse = AdyenResponseParser.transformAuthorizeResponse(request, result, null)
//
//        assertAuthorizeResponse(request, result, response)
//        assertEquals(OperationStatus.CREATED, response.status)
//    }

    @Test
    fun `transformAuthorizeResponse - created for one time payment methods`() {
        val authorizeRequest =
            mockAuthorizeRequest(paymentInstrument = mockOneTimePayment())
        val request = AdyenRequestBuilder.prepareAuthorizeRequests(authorizeRequest)
        val result = mockPaymentsResponse(AdyenResponseParser.ADYEN_CREATED_CODE)
        val response = AdyenResponseParser.transformAuthorizeResponse(authorizeRequest, request, result, null)

        assertAuthorizeResponse(request, result, response)
        assertEquals(OperationStatus.CREATED, response.status)
    }

    //TODO::Test related to adyencreditcard which has methods not implemented in COMMON LIB
//    @Test
//    fun `transformAuthorizeResponse - exception with deprecated functions for card payments`() {
//        val adyenAuthorizeRequest= AdyenTestUtils.createAdyenAuthorizeRequest()
//        val request: PaymentRequest = AdyenRequestBuilder.prepareAuthorizeRequest(adyenAuthorizeRequest)
//        val ex = java.lang.Exception("test")
//        val response: AdyenAuthorizeResponse = AdyenResponseParser.transformAuthorizeResponse(request, null, ex)
//        assertNotNull(response)
//        assertEquals(AdyenResponseParser.toJsonStringOrNull(request, ""), response.requestJson)
//        assertEquals(ex.message, response.originalResult)
//        assertEquals(ex.message, response.responseJson)
//    }

    @Test
    fun `transformAuthorizeResponse - exception for one time payment methods`() {
        val authorizeRequest =
            mockAuthorizeRequest(paymentInstrument = mockOneTimePayment())
        val request = AdyenRequestBuilder.prepareAuthorizeRequests(authorizeRequest)
        val ex = Exception("test")

        val response = AdyenResponseParser.transformAuthorizeResponse(authorizeRequest, request, null, ex)

        assertNotNull(response)
//        assertEquals(AdyenResponseParser.toJsonStringOrNull(request, ""), response.responseJson())
        assertEquals(ex.message, response.rawStatus)
        assertEquals(ex.toString(), response.rawResponse)
    }

    //TODO::Test related to adyencreditcard which has methods not implemented in COMMON LIB
//    @Test
//    fun testTransformGetCardsResponse_Success() {
//        val adyenGetCardsRequest = AdyenTestUtils.createAdyenGetCardsRequest()
//        val request = AdyenRequestBuilder.prepareGetCardsRequest(adyenGetCardsRequest)
//        val result = AdyenTestUtils.createAdyenRecurringDetailsResult()
//
//        val response = AdyenResponseParser.transformGetCardsResponse(request, result, null)
//
//        assertNotNull(response)
//        assertNull(response.getOriginalResult())
//        assertEquals(result.details.size, response.getCards().size())
//        val adyenCardData = response.getCards().get(0)
//        val recurringDetail = result.recurringDetails[0]
//        assertEquals(recurringDetail.creationDate, adyenCardData.getCreationDate())
//        assertEquals(
//            recurringDetail.recurringDetailReference,
//            adyenCardData.getToken().getValue(SecureString.ACCESS_KEY)
//        )
//        assertEquals(recurringDetail.additionalData[ApiConstants.AdditionalData.CARD_BIN], adyenCardData.getBin())
//        assertEquals(recurringDetail.card.number, adyenCardData.getLastDigits())
//        assertEquals(recurringDetail.card.expiryMonth, adyenCardData.getExpiryMonth())
//        assertEquals(recurringDetail.card.expiryYear, adyenCardData.getExpiryYear())
//        assertEquals(recurringDetail.card.holderName, adyenCardData.getHolderName().getValue(SecureString.ACCESS_KEY))
//        assertEquals(recurringDetail.paymentMethodVariant, adyenCardData.getVariant())
//        assertEquals(AdyenResponseParser.toJsonStringOrNull(request, ""), response.getRequestJson())
//        assertEquals(AdyenResponseParser.toJsonStringOrNull(result, ""), response.getResponseJson())
//    }

    //TODO::Test related to adyencreditcard which has methods not implemented in COMMON LIB
//    @Test
//    fun testTransformGetCardsResponse_Exception() {
//        val adyenGetCardsRequest = AdyenTestUtils.createAdyenGetCardsRequest()
//        val request = AdyenRequestBuilder.prepareGetCardsRequest(adyenGetCardsRequest)
//        val ex = Exception("test")
//
//        val response = AdyenResponseParser.transformGetCardsResponse(request, null, ex)
//
//        assertNotNull(response)
//        assertEquals(AdyenResponseParser.toJsonStringOrNull(request, ""), response.getRequestJson())
//        assertEquals(ex.message, response.getOriginalResult())
//        assertEquals(ex.message, response.getResponseJson())
//    }

//    fun testTransformGetGooglePayResponse_Exception()
//    fun testTransformGetApplePayResponse_Exception()

//    @Test
//    fun `transformRefundResponse - success for card payments`() {
//        val adyenRefundRequest = AdyenTestUtils.createAdyenRefundRequest()
//        val request = AdyenRequestBuilder.prepareRefundRequest(adyenRefundRequest)
//        val result = AdyenTestUtils.createAdyenModificationResult()
//
//        val response = AdyenResponseParser.transformRefundResponse(request, result, null)
//
//        assertNotNull(response)
//        assertEquals(result.response.toString(), response.originalResult)
//        assertEquals(result.pspReference, response.pspReference)
//        assertEquals(AdyenResponseParser.toJsonStringOrNull(request, ""), response.requestJson)
//        assertEquals(AdyenResponseParser.toJsonStringOrNull(result, ""), response.responseJson)
//    }

    @Test
    fun `transformRefundResponse - success for one time payment methods`() {
        val refundRequest = mockRefundRequest(
            originalOperationId = DEFAULT_ORIGINAL_REFERENCE,
            amount = mockMoney(currency = Currency.valueOf(DEFAULT_CURRENCY), amount = DEFAULT_AMOUNT)
        )
        val request = AdyenRequestBuilder.prepareRefundRequest(refundRequest)
        val result = mockModificationResult()

        val response = AdyenResponseParser.transformRefundResponse(refundRequest, request, result, null)

        assertNotNull(response)
        assertEquals(AdyenResponseParser.toJsonStringOrNull(result.response, ""), response.rawResponse)
        assertEquals(result.pspReference, response.reference)
//        assertEquals(AdyenResponseParser.toJsonStringOrNull(request, ""), response.requestJson())
//        assertEquals(AdyenResponseParser.toJsonStringOrNull(result, ""), response.responseJson())
    }

    //TODO::Test related to adyencreditcard which has methods not implemented in COMMON LIB
//    @Test
//    fun `transformRefundResponse - exception for card payments`() {
//        val adyenRefundRequest = AdyenTestUtils.createAdyenRefundRequest()
//        val request = AdyenRequestBuilder.prepareRefundRequest(adyenRefundRequest)
//        val ex = Exception("test")
//
//        val response = AdyenResponseParser.transformRefundResponse(request, null, ex)
//
//        assertNotNull(response)
//        assertEquals(AdyenResponseParser.toJsonStringOrNull(request, ""), response.requestJson)
//        assertEquals(ex.message, response.originalResult)
//        assertEquals(ex.message, response.responseJson)
//    }

    @Test
    fun `transformRefundResponse - exception for one time payment methods`() {
        val refundRequest = mockRefundRequest(
            DEFAULT_ORIGINAL_REFERENCE,
            amount = mockMoney(currency = Currency.valueOf(DEFAULT_CURRENCY), amount = DEFAULT_AMOUNT)
        )
        val request = AdyenRequestBuilder.prepareRefundRequest(refundRequest)
        val ex = Exception("test")

        val response = AdyenResponseParser.transformRefundResponse(refundRequest, request, null, ex)

        assertNotNull(response)
//        assertEquals(AdyenResponseParser.toJsonStringOrNull(request, ""), response.requestJson())
        assertEquals(ex.message, response.rawStatus)
        assertEquals(ex.toString(), response.rawResponse)
//        assertEquals(ex.message, response.responseJson())
    }

    //TODO::Test related to adyencreditcard which has methods not implemented in COMMON LIB
//    @Test
//    fun `transformCancelResponse - success for card payments`() {
//        val adyenCancelRequest = AdyenTestUtils.createAdyenCancelRequest()
//        val request = AdyenRequestBuilder.prepareCancelRequest(adyenCancelRequest)
//        val result = AdyenTestUtils.createAdyenModificationResult()
//
//        val response = AdyenResponseParser.transformCancelResponse(request, result, null)
//
//        assertNotNull(response)
//        assertEquals(result.response.toString(), response.originalResult)
//        assertEquals(result.pspReference, response.pspReference)
//        assertEquals(AdyenResponseParser.toJsonStringOrNull(request, ""), response.requestJson)
//        assertEquals(AdyenResponseParser.toJsonStringOrNull(result, ""), response.responseJson)
//    }

    @Test
    fun `transformCancelResponse - success for one time payment methods`() {
        val cancelRequest = mockCancelRequest()
        val request = AdyenRequestBuilder.prepareCancelRequest(cancelRequest)
        val result = mockModificationResult()

        val response = AdyenResponseParser.transformCancelResponse(cancelRequest, request, result, null)

        assertNotNull(response)
        assertEquals(AdyenResponseParser.toJsonStringOrNull(result.response, ""), response.rawResponse)
        assertEquals(result.pspReference, response.reference)
//        assertEquals(AdyenResponseParser.toJsonStringOrNull(request, ""), response.requestJson())
//        assertEquals(AdyenResponseParser.toJsonStringOrNull(result, ""), response.responseJson())
    }

    //TODO::Test related to adyencreditcard which has methods not implemented in COMMON LIB
//    @Test
//    fun `transformCancelResponse - exception for card payments`() {
//        val adyenCancelRequest = AdyenTestUtils.createAdyenCancelRequest()
//        val request = AdyenRequestBuilder.prepareCancelRequest(adyenCancelRequest)
//        val ex = Exception("test")
//
//        val response = AdyenResponseParser.transformCancelResponse(request, null, ex)
//
//        assertNotNull(response)
//        assertEquals(AdyenResponseParser.toJsonStringOrNull(request, ""), response.requestJson)
//        assertEquals(ex.message, response.originalResult)
//        assertEquals(ex.message, response.responseJson)
//    }

    @Test
    fun `transformCancelResponse - exception for one time payment methods`() {
        val cancelRequest = mockCancelRequest()
        val request = AdyenRequestBuilder.prepareCancelRequest(cancelRequest)
        val ex = Exception("test")

        val response = AdyenResponseParser.transformCancelResponse(cancelRequest, request, null, ex)

        assertNotNull(response)
//        assertEquals(AdyenResponseParser.toJsonStringOrNull(request, ""), response.requestJson())
        assertEquals(ex.message, response.rawStatus)
        assertEquals(ex.toString(), response.rawResponse)
//        assertEquals(ex.message, response.responseJson())
    }

//    private fun assertAuthorizeResponse(
//        request: PaymentRequest,
//        result: PaymentResult,
//        response: AdyenAuthorizeResponse
//    ) {
//        assertNotNull(response)
//        assertEquals(result.resultCode.toString(), response.originalResult)
//        assertEquals(result.pspReference, response.pspReference)
//        assertEquals(result.paRequest, response.paRequest)
//        assertEquals(result.md, response.md)
//        assertEquals(result.issuerUrl, response.redirectUrl)
//        assertEquals(
//            result.getAdditionalDataByKey(ADDITIONAL_DATA_RECURRING_DETAIL_REFERENCE),
//            response.cardToken
//        )
//        assertEquals(result.getAdditionalDataByKey(ApiConstants.AdditionalData.EXPIRY_DATE), response.expiryDate)
//        assertEquals(result.cardHolderName, response.cardHolder)
//        assertEquals(result.paymentMethod, response.scheme)
//        assertEquals(result.cardBin, response.bin)
//        assertEquals(result.cardSummary, response.displayName)
//        assertEquals(AdyenResponseParser.toJsonStringOrNull(request, ""), response.requestJson)
//        assertEquals(AdyenResponseParser.toJsonStringOrNull(result, ""), response.responseJson)
//    }

    private fun assertAuthorizeResponse(
        paymentsRequest: PaymentsRequest,
        paymentsResponse: PaymentsResponse,
        operationResponse: OperationResponse
    ) {
        assertNotNull(operationResponse)
        assertEquals(paymentsResponse.resultCode.toString(), operationResponse.rawStatus)
        assertEquals(
            paymentsResponse.additionalData[ADDITIONAL_DATA_RECURRING_DETAIL_REFERENCE],
            operationResponse.reference
        )
        assertEquals(
            paymentsResponse.action?.data?.get("PaReq"),
            operationResponse.redirectRequest?.requestParameters?.get(AdyenResponseParser.ADYEN_PA_REQUEST)
        )
        assertEquals(
            paymentsResponse.action?.data?.get("MD"),
            operationResponse.redirectRequest?.requestParameters?.get(AdyenResponseParser.ADYEN_MD)
        )
        assertEquals(paymentsResponse.redirect?.url, operationResponse.redirectRequest?.redirectUrl)
        assertEquals(
            paymentsResponse.getAdditionalDataByKey(ADDITIONAL_DATA_RECURRING_DETAIL_REFERENCE),
            operationResponse.reference
        )
        //TODO:: Assertions for card mapping
//        if(paymentsRequest.paymentMethod!="card")
//        assertEquals(paymentsResponse.getAdditionalDataByKey(ApiConstants.AdditionalData.EXPIRY_DATE), paymentsRequest.expiryDate)
//        assertEquals(pa.cardHolderName, response.cardHolder)
//        assertEquals(result.paymentMethod, response.scheme)
//        assertEquals(result.cardBin, response.bin)
//        assertEquals(result.cardSummary, response.displayName)
//        assertEquals(AdyenResponseParser.toJsonStringOrNull(paymentsRequest, ""), operationResponse.request?.rawRequest)
        assertEquals(AdyenResponseParser.toJsonStringOrNull(paymentsResponse, ""), operationResponse.rawResponse)
    }

    companion object {
        val DEFAULT_ORIGINAL_REFERENCE = "oRef123"
        val DEFAULT_CURRENCY = "UYU"
        val DEFAULT_AMOUNT = 100L
    }
}
