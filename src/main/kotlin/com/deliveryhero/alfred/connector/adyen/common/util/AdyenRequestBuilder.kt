package com.deliveryhero.alfred.connector.adyen.common.util

import com.adyen.model.Address
import com.adyen.model.Amount
import com.adyen.model.BrowserInfo
import com.adyen.model.Name
import com.adyen.model.checkout.DefaultPaymentMethodDetails
import com.adyen.model.modification.AbstractModificationRequest
import com.adyen.model.recurring.Recurring
import com.deliveryhero.alfred.connector.adyen.common.dom.AdyenConfigData
import com.deliveryhero.alfred.connector.sdk.operation.request.*
import com.deliveryhero.alfred.connector.sdk.operation.request.config.Environment
import com.deliveryhero.alfred.connector.sdk.operation.request.paymentinstrument.OneTimePayment
import org.apache.commons.lang3.StringUtils
import com.adyen.model.PaymentRequest3d as AdyenApiAuthorize3dsRequest
import com.adyen.model.checkout.PaymentsRequest as AdyenApiAuthorizeRequest
import com.adyen.model.modification.CancelOrRefundRequest as AdyenApiCancelOrRefundRequest
import com.adyen.model.modification.CancelRequest as AdyenApiCancelRequest
import com.adyen.model.modification.CaptureRequest as AdyenApiCaptureRequest

//builds requests from connector-sdk classes to adyen specific classes
object AdyenRequestBuilder {

    const val ADDITIONAL_DATA_RETURN_URL: String = "returnUrl"
    const val FALLBACK_BROWSER_USER_AGENT: String = "OTHER"
    const val GLOBAL_TRANSACTION_ID: String = "globalTransactionId"

    const val PAYMENT_METHOD_APPLEPAY = "adyen_applepay"
    const val PAYMENT_METHOD_GOOGLEPAY = "adyen_googlepay"
    const val PAYMENT_METHOD_CREDITCARD = "adyen_creditcard"
    const val PAYMENT_METHOD_TEST = "adyen"
    val PAYMENT_METHOD_TO_ADYEN_NAME = mapOf(
        PAYMENT_METHOD_APPLEPAY to "applepay",
        PAYMENT_METHOD_GOOGLEPAY to "paywithgoogle",
        PAYMENT_METHOD_CREDITCARD to "scheme",
        PAYMENT_METHOD_TEST to "adyen" //Temporary to test general adyen transition
    )

    private val TEST_ENVIRONMENTS = listOf(Environment.LOCAL, Environment.TEST)

    fun prepareCancelRequest(request: CancelRequest): AdyenApiCancelRequest {
        val pspCancelRequest = AdyenApiCancelRequest()
        prepareModificationRequest(pspCancelRequest, request)
        return pspCancelRequest
    }

    fun prepareRefundRequest(request: RefundRequest): AdyenApiCancelOrRefundRequest {
        val pspCancelOrRefundRequest = AdyenApiCancelOrRefundRequest()
        prepareModificationRequest(pspCancelOrRefundRequest, request)
        return pspCancelOrRefundRequest
    }

    fun prepareCaptureRequest(request: CaptureRequest): AdyenApiCaptureRequest {
        val pspCaptureRequest = AdyenApiCaptureRequest()
        prepareModificationRequest(pspCaptureRequest, request)
        if (request.amount != null) {
            pspCaptureRequest.modificationAmount = Amount()
                .currency(request.amount!!.currency.toString())
                .value(request.amount!!.value.toLong())
        }
        return pspCaptureRequest
    }

    /**
     * prepareAuthorizeRequests  takes adyen.model.PaymentsRequest as input to be able to set paymentMethodDetails.type while sending a payment request
     */
    fun prepareAuthorizeRequests(request: AuthorizeRequest): AdyenApiAuthorizeRequest {
        val pspAuthorizeRequest = AdyenApiAuthorizeRequest()
        setTransactionDetails(pspAuthorizeRequest, request)
        setUserDetails(pspAuthorizeRequest, request)
        set3dsDetails(pspAuthorizeRequest, request)
        setPaymentMethod(pspAuthorizeRequest, request)
        setAdditionalData(pspAuthorizeRequest, request)
        setMetadata(pspAuthorizeRequest, request)
        return pspAuthorizeRequest
    }

    fun prepareValidate3DsRequest(request: AuthorizeRequest): AdyenApiAuthorize3dsRequest {
        val paymentRequest = AdyenApiAuthorize3dsRequest()
        paymentRequest.merchantAccount = getAdyenConfigData(request).merchantAccount()
        paymentRequest.md = request.transaction.redirectResponse?.md()
        paymentRequest.paResponse = request.transaction.redirectResponse?.paRes()
        paymentRequest.amount = Amount()
            .currency(request.transaction.amount.currency.toString())
            .value(request.transaction.amount.value.toLong()) //TODO do specific test to check this convertion
        if (request.shouldStorePaymentInstrument && !request.customer.id.isNullOrEmpty()) {
            paymentRequest.recurring = Recurring().contract(Recurring.ContractEnum.ONECLICK_RECURRING)
            paymentRequest.shopperReference = request.customer.id
        }
        return paymentRequest
    }

    private fun prepareModificationRequest(
        modificationRequest: AbstractModificationRequest<*>,
        request: ModificationRequest
    ) {
        modificationRequest.setMerchantAccount(getAdyenConfigData(request).merchantAccount())
        modificationRequest.setReference(request.operationId)
        modificationRequest.setOriginalReference(request.originalOperationId)
    }

    fun getAdyenConfigData(request: OperationRequest): AdyenConfigData {
        return request.providerConfig as AdyenConfigData
    }

    private fun setTransactionDetails(pspAuthorizeRequest: AdyenApiAuthorizeRequest, request: AuthorizeRequest) {
        pspAuthorizeRequest.merchantAccount = getAdyenConfigData(request).merchantAccount()
        pspAuthorizeRequest.amount = Amount()
            .currency(request.transaction.amount.currency.toString())
            .value(request.transaction.amount.value.toLong()) //TODO do specific test to check this convertion
        pspAuthorizeRequest.reference = request.order.id
        pspAuthorizeRequest.shopperInteraction = AdyenApiAuthorizeRequest.ShopperInteractionEnum.ECOMMERCE
    }

    private fun setUserDetails(pspAuthorizeRequest: AdyenApiAuthorizeRequest, request: AuthorizeRequest) {
        pspAuthorizeRequest.shopperReference = request.customer.id
        pspAuthorizeRequest.shopperEmail = request.customer.email
        pspAuthorizeRequest.telephoneNumber = request.customer.phone?.number

        pspAuthorizeRequest.shopperName = Name()
            .firstName(request.customer.name)
            .lastName(request.customer.lastName)

        pspAuthorizeRequest.shopperIP = request.clientContext?.ipAddress

        request.order.deliveryAddress?.apply {
            if (StringUtils.isNotEmpty(city)
                && StringUtils.isNotEmpty(state)
                && StringUtils.isNotEmpty(street)
                && StringUtils.isNotEmpty(doorNumber)
                && StringUtils.isNotEmpty(postalCode)
            ) {
                pspAuthorizeRequest.deliveryAddress = Address()
                    .country(country.toString())
                    .city(city)
                    .stateOrProvince(state)
                    .street(street)
                    .houseNumberOrName(doorNumber)
                    .postalCode(postalCode)
            }
        }
    }

    private fun set3dsDetails(pspAuthorizeRequest: AdyenApiAuthorizeRequest, request: AuthorizeRequest) {
        if (getAdyenConfigData(request).shouldSendBrowserInfo()) {
            request.clientContext?.apply {
                if (userAgent.isNullOrBlank()) {
                    userAgent = FALLBACK_BROWSER_USER_AGENT
                }
                val browserInfo = BrowserInfo()
                browserInfo.userAgent(userAgent)
                browserInfo.acceptHeader(acceptHeader)
                pspAuthorizeRequest.browserInfo = browserInfo
            }
        }
        request.transaction.returnUrlInfo?.apply {
            pspAuthorizeRequest.putAdditionalDataItem(ADDITIONAL_DATA_RETURN_URL, success)
            pspAuthorizeRequest.returnUrl = success
        }
        // pspAuthorizeRequest.returnUrl = request.transaction.returnUrlInfo(ADDITIONAL_DATA_RETURN_URL, success)
    }

//    private fun setCardDetails(pspAuthorizeRequest: AdyenApiAuthorizeRequest, request: AuthorizeRequest) {
//        request.paymentInstrument?.apply {
//            if (type == PaymentInstrumentType.TOKENIZED_CARD) {
//                setTokenizedCardDetails(pspAuthorizeRequest, request)
//            } else {
//                setNotTokenizedCardDetails(pspAuthorizeRequest, request)
//            }
//        }
//    }
//
//        private fun setTokenizedCardDetails(pspPaymentsRequest: AdyenApiAuthorizeRequest, request: AuthorizeRequest) {
//            pspPaymentsRequest.selectedRecurringDetailReference = request.cardToken
//            paymentRequest.recurringProcessingModel = PaymentRequest.RecurringProcessingModelEnum.CARD_ON_FILE
//            paymentRequest.shopperInteraction = AbstractPaymentRequest.ShopperInteractionEnum.CONTAUTH
//            val recurring = Recurring()
//            recurring.contract = Recurring.ContractEnum.RECURRING
//            paymentRequest.recurring = recurring
//            if (request.encryptedSecurityCode != null && StringUtils.isNotBlank(request.encryptedSecurityCode)) {
//                paymentRequest.setEncryptedSecurityCode(request.encryptedSecurityCode)
//            } else if (StringUtils.isNotBlank(request.rawCardSecurityCode)) {
//                paymentRequest.card = Card().cvc(request.rawCardSecurityCode)
//            }
//            if (request.encryptedSecurityCode == null && !StringUtils.isNotBlank(request.encryptedSecurityCode)) {
//
//                pspPaymentsRequest.addCardData(
//                    request.rawCardNumber,
//                    request.rawCardExpiryMonth,
//                    request.rawCardExpiryYear,
//                    request.rawCardSecurityCode,
//                    request.cardHolderName
//                )
//            }
////            else{
////                paymentsRequest.addCardData(
////                    params.encryptedCardNumber,
////                    params.rawCardExpiryMonth,
////                    params.rawCardExpiryYear,
////                    params.rawCardSecurityCode,
////                    params.cardHolderName
////                )
////            }
//        }
//
//    private fun setTokenizedCardDetails(paymentRequest: PaymentRequest, params: AdyenAuthorizeRequest) {
//        paymentRequest.selectedRecurringDetailReference = params.cardToken
//        paymentRequest.recurringProcessingModel = PaymentRequest.RecurringProcessingModelEnum.CARD_ON_FILE
//        paymentRequest.shopperInteraction = AbstractPaymentRequest.ShopperInteractionEnum.CONTAUTH
//        val recurring = Recurring()
//        recurring.contract = Recurring.ContractEnum.RECURRING
//        paymentRequest.recurring = recurring
//        if (!params.encryptedSecurityCode.isNullOrBlank()) {
//            paymentRequest.setEncryptedSecurityCode(params.encryptedSecurityCode)
//        } else if (!params.rawCardSecurityCode.isNullOrBlank()) {
//            paymentRequest.card = Card().cvc(params.rawCardSecurityCode)
//        }
//    }
//
//    private fun setNotTokenizedCardDetails(paymentRequest: PaymentRequest, params: AdyenAuthorizeRequest) {
//        if (params.isShouldTokenizeCard && StringUtils.isNotBlank(params.userId)) {
//            val recurring = Recurring()
//            recurring.contract = Recurring.ContractEnum.ONECLICK_RECURRING
//            paymentRequest.recurring = recurring
//        }
//        if (StringUtils.isNotBlank(params.encryptedCardData)) {
//            //client-side encrypted cards with old version of sdk
//            paymentRequest.orCreateAdditionalData[Encrypted.JSON] = params.encryptedCardData
//        } else if (StringUtils.isNotBlank(params.encryptedCardNumber)
//            && StringUtils.isNotBlank(params.cardHolderName)
//            && StringUtils.isNotBlank(params.encryptedExpiryMonth)
//            && StringUtils.isNotBlank(params.encryptedExpiryYear)
//            && StringUtils.isNotBlank(params.encryptedSecurityCode)
//        ) {
//            //client-side encrypted cards with Checkout API
//            paymentRequest.setSecuredFieldsData(
//                params.encryptedCardNumber,
//                params.cardHolderName,
//                params.encryptedExpiryMonth,
//                params.encryptedExpiryYear,
//                params.encryptedSecurityCode
//            )
//        } else if (params.rawCardNumber != null
//            && StringUtils.isNotBlank(params.cardHolderName)
//            && StringUtils.isNotBlank(params.rawCardExpiryMonth)
//            && StringUtils.isNotBlank(params.rawCardExpiryYear)
//            && StringUtils.isNotBlank(params.rawCardSecurityCode)
//        ) {
//            //raw cards only for testing!
//            paymentRequest.setCardData(
//                params.rawCardNumber,
//                params.cardHolderName,
//                params.rawCardExpiryMonth,
//                params.rawCardExpiryYear,
//                params.rawCardSecurityCode
//            )
//        }
//    }

    private fun setPaymentMethod(pspAuthorizeRequest: AdyenApiAuthorizeRequest, request: AuthorizeRequest) {
        when (val identifier = request.providerConfig.identifier) {
            PAYMENT_METHOD_CREDITCARD -> throw NotImplementedError() // TODO https://docs.adyen.com/payment-methods/cards/custom-card-component
            else -> {
                DefaultPaymentMethodDetails().apply {
                    type = PAYMENT_METHOD_TO_ADYEN_NAME[identifier]
                        ?: throw IllegalArgumentException("Unknown payment identifier : $identifier")
                    when (identifier) {
                        PAYMENT_METHOD_APPLEPAY -> applepayToken =
                            (request.paymentInstrument as OneTimePayment).token
                        PAYMENT_METHOD_GOOGLEPAY -> googlepayToken =
                            (request.paymentInstrument as OneTimePayment).token
                    }
                    pspAuthorizeRequest.paymentMethod = this
                }
            }
        }
    }

    private fun setAdditionalData(pspAuthorizeRequest: AdyenApiAuthorizeRequest, request: AuthorizeRequest) {
        if (request.additionalData != null) {
            for (entry in request.additionalData.entries) {
                pspAuthorizeRequest.putAdditionalDataItem(entry.key, entry.value)
            }
        }
    }

    private fun setMetadata(pspAuthorizeRequest: AdyenApiAuthorizeRequest, request: AuthorizeRequest) {
        pspAuthorizeRequest.metadata = mutableMapOf<String, String>().apply {
            if (request.transaction.id != null)
                set(GLOBAL_TRANSACTION_ID, request.transaction.id as String)
        }
    }
}
