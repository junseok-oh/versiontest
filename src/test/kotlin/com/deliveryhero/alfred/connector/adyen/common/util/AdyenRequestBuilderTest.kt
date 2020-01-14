package com.deliveryhero.alfred.connector.adyen.common.util

import com.adyen.model.checkout.PaymentsRequest
import com.deliveryhero.alfred.connector.*
import com.deliveryhero.alfred.connector.adyen.common.util.AdyenRequestBuilder.ADDITIONAL_DATA_RETURN_URL
import com.deliveryhero.alfred.connector.adyen.common.util.AdyenRequestBuilder.GLOBAL_TRANSACTION_ID
import com.deliveryhero.alfred.connector.adyen.common.util.AdyenRequestBuilder.PAYMENT_METHOD_TEST
import com.deliveryhero.alfred.connector.adyen.common.util.AdyenRequestBuilder.getAdyenConfigData
import com.deliveryhero.alfred.connector.sdk.operation.OperationType
import com.deliveryhero.alfred.connector.sdk.operation.request.AuthorizeRequest
import com.deliveryhero.alfred.connector.sdk.operation.request.common.Country
import com.deliveryhero.alfred.connector.sdk.operation.request.common.Currency
import com.deliveryhero.alfred.connector.sdk.operation.request.redirect.ReturnUrlInfo
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.junit.jupiter.MockitoExtension

/**
 * Tests for [com.deliveryhero.alfred.server.impl.payment.gateway.adyen.util.AdyenRequestBuilder].
 *
 * @author julius.joosten
 */
@ExtendWith(MockitoExtension::class)
class AdyenRequestBuilderTest {

//    @Test
//    fun `should create PaymentRequest successfully (not tokenized card details, encrypted 1, no tokenize)`() {
//        val adyenServiceRequest = AdyenTestUtils.createAuthorizeRequest()
//        adyenServiceRequest.encryptedCardData = DEFAULT_ENCRYPTED_CARD_DATA
//        val request = AdyenRequestBuilder.prepareAuthorizeRequests(adyenServiceRequest)
//        assertMandatoryAdyenMappings(adyenServiceRequest, request)
//        assertEquals(
//            adyenServiceRequest.encryptedCardData,
//            request.additionalData[ApiConstants.AdditionalData.Card.Encrypted.JSON]
//        )
//    }
//
//    @Test
//    fun `should create PaymentRequest successfully (not tokenized card details, encrypted 2, no tokenize)`() {
//        val adyenServiceRequest = AdyenTestUtils.createAdyenAuthorizeRequest()
//        adyenServiceRequest.encryptedSecurityCode = DEFAULT_ENCRYPTED_CVV
//        adyenServiceRequest.encryptedCardNumber = DEFAULT_ENCRYPTED_CARD_NUMBER
//        adyenServiceRequest.encryptedExpiryMonth = DEFAULT_ENCRYPTED_EXPIRY_MONTH
//        adyenServiceRequest.encryptedExpiryYear = DEFAULT_ENCRYPTED_EXPIRY_YEAR
//        adyenServiceRequest.cardHolderName = DEFAULT_CARD_HOLDER_NAME
//        val request = AdyenRequestBuilder.prepareAuthorizeRequest(adyenServiceRequest)
//        assertMandatoryAdyenMappings(adyenServiceRequest, request)
//        assertEquals(adyenServiceRequest.encryptedCardNumber, request.additionalData["encryptedCardNumber"])
//        assertEquals(adyenServiceRequest.encryptedExpiryMonth, request.additionalData["encryptedExpiryMonth"])
//        assertEquals(adyenServiceRequest.encryptedExpiryYear, request.additionalData["encryptedExpiryYear"])
//        assertEquals(
//            adyenServiceRequest.encryptedSecurityCode,
//            request.additionalData[ADYEN_ADDITIONAL_DATA_ENCRYPTED_CVV]
//        )
//        assertEquals(adyenServiceRequest.cardHolderName, request.card.holderName)
//    }

    @Test
    fun `should create PaymentRequest successfully for card (only with mandatory data)`() {
        val authorizeRequest = mockAuthorizeRequest(
            paymentInstrument = mockTokenizedCard(), order = mockOrder(
                mockAddress(
                    street = null,
                    complement = null,
                    doorNumber = null,
                    area = null,
                    city = null,
                    state = null,
                    country = null,
                    postalCode = null
                )
            )
        )
        val paymentsRequest = AdyenRequestBuilder.prepareAuthorizeRequests(authorizeRequest)
        assertMandatoryAdyenMappings(authorizeRequest, paymentsRequest)
        assertNull(paymentsRequest.deliveryAddress)
    }

    @Test
    fun `should create PaymentsRequest successfully for other payment methods than card (only with mandatory data)`() {
        val authorizeRequest = mockAuthorizeRequest(
            paymentInstrument = mockTokenizedCard(), order = mockOrder(
                mockAddress(
                    street = null,
                    complement = null,
                    doorNumber = null,
                    area = null,
                    city = null,
                    state = null,
                    country = null,
                    postalCode = null
                )
            )
        )
        val paymentsRequest = AdyenRequestBuilder.prepareAuthorizeRequests(authorizeRequest)
        assertMandatoryAdyenMappings(authorizeRequest, paymentsRequest)
        assertNull(paymentsRequest.deliveryAddress)
    }

//    @Test
//    fun `should create PaymentRequest successfully for card (with tokenized card but no CVV`() {
//        val adyenServiceRequest = AdyenTestUtils.createAdyenAuthorizeRequest()
//        adyenServiceRequest.cardToken = DEFAULT_CARD_TOKEN
//        val request = AdyenRequestBuilder.prepareAuthorizeRequest(adyenServiceRequest)
//        assertMandatoryAdyenMappings(
//            adyenServiceRequest,
//            request,
//            AbstractPaymentRequest.ShopperInteractionEnum.CONTAUTH
//        )
//        assertTokenizedCardDetails(adyenServiceRequest, request)
//        assertNull(request.card)
//    }

//    @Test
//    fun `should create PaymentRequest successfully for card (with tokenized card and raw CVV`() {
//        val adyenServiceRequest = AdyenTestUtils.createAdyenAuthorizeRequest()
//        adyenServiceRequest.cardToken = DEFAULT_CARD_TOKEN
//        adyenServiceRequest.rawCardSecurityCode = DEFAULT_RAW_CVV
//        val request = AdyenRequestBuilder.prepareAuthorizeRequest(adyenServiceRequest)
//        assertMandatoryAdyenMappings(
//            adyenServiceRequest,
//            request,
//            AbstractPaymentRequest.ShopperInteractionEnum.CONTAUTH
//        )
//        assertTokenizedCardDetails(adyenServiceRequest, request)
//        assertEquals(adyenServiceRequest.rawCardSecurityCode, request.card.cvc)
//    }
//
//    @Test
//    fun `should create PaymentRequest successfully for card (with tokenized card and encrypted CVV`() {
//        val adyenServiceRequest = AdyenTestUtils.createAdyenAuthorizeRequest()
//        adyenServiceRequest.cardToken = DEFAULT_CARD_TOKEN
//        adyenServiceRequest.encryptedSecurityCode = DEFAULT_ENCRYPTED_CVV
//        val request = AdyenRequestBuilder.prepareAuthorizeRequest(adyenServiceRequest)
//        assertMandatoryAdyenMappings(
//            adyenServiceRequest,
//            request,
//            AbstractPaymentRequest.ShopperInteractionEnum.CONTAUTH
//        )
//        assertTokenizedCardDetails(adyenServiceRequest, request)
//        assertEquals(
//            adyenServiceRequest.encryptedSecurityCode,
//            request.additionalData[ADYEN_ADDITIONAL_DATA_ENCRYPTED_CVV]
//        )
//        assertNull(request.card)
//    }
//
//    @Test
//    fun `should create PaymentRequest successfully which doesn't contain address because of incomplete address input`() {
//        val adyenServiceRequest = AdyenTestUtils.createAdyenAuthorizeRequest()
//        adyenServiceRequest.deliveryAddressCity = DEFAULT_ADDRESS_CITY
//        adyenServiceRequest.deliveryAddressStreet = DEFAULT_ADDRESS_STREET
//        val request = AdyenRequestBuilder.prepareAuthorizeRequest(adyenServiceRequest)
//        assertMandatoryAdyenMappings(adyenServiceRequest, request)
//        assertNull(request.deliveryAddress)
//    }

    @Test
    fun `should create PaymentsRequest successfully which doesn't contain address because of incomplete address input for other payment methods than card`() {
        val authorizeRequest = mockAuthorizeRequest(
            order = mockOrder(
                deliveryAddress = mockAddress(
                    city = DEFAULT_ADDRESS_CITY,
                    street = DEFAULT_ADDRESS_STREET,
                    postalCode = null,
                    country = null,
                    state = null,
                    area = null,
                    doorNumber = null,
                    complement = null
                )
            ),
            paymentInstrument = mockTokenizedCard()
        )
        val paymentsRequest = AdyenRequestBuilder.prepareAuthorizeRequests(authorizeRequest)
        assertMandatoryAdyenMappings(authorizeRequest, paymentsRequest)
        assertNull(paymentsRequest.deliveryAddress)
    }

//    @Test
//    fun `should create PaymentRequest successfully with full address`() {
//        val adyenServiceRequest = AdyenTestUtils.createAdyenAuthorizeRequest()
//        adyenServiceRequest.deliveryAddressCity = DEFAULT_ADDRESS_CITY
//        adyenServiceRequest.deliveryAddressStreet = DEFAULT_ADDRESS_STREET
//        adyenServiceRequest.deliveryAddressCountry = DEFAULT_ADDRESS_COUNTRY
//        adyenServiceRequest.deliveryAddressDoorNumber = DEFAULT_ADDRESS_DOOR_NUMBER
//        adyenServiceRequest.deliveryAddressState = DEFAULT_ADDRESS_STATE
//        adyenServiceRequest.deliveryAddressZipCode = DEFAULT_ADDRESS_ZIP_CODE
//        val request = AdyenRequestBuilder.prepareAuthorizeRequest(adyenServiceRequest)
//        assertMandatoryAdyenMappings(adyenServiceRequest, request)
//        assertEquals(adyenServiceRequest.deliveryAddressCity, request.deliveryAddress.city)
//        assertEquals(adyenServiceRequest.deliveryAddressStreet, request.deliveryAddress.street)
//        assertEquals(adyenServiceRequest.deliveryAddressCountry, request.deliveryAddress.country)
//        assertEquals(adyenServiceRequest.deliveryAddressDoorNumber, request.deliveryAddress.houseNumberOrName)
//        assertEquals(adyenServiceRequest.deliveryAddressState, request.deliveryAddress.stateOrProvince)
//        assertEquals(adyenServiceRequest.deliveryAddressZipCode, request.deliveryAddress.postalCode)
//    }

    @Test
    fun `should create PaymentsRequest successfully with full address for other payment methods than card`() {
        val authorizeRequest = mockAuthorizeRequest(
            order = mockOrder(
                deliveryAddress = mockAddress(
                    street = DEFAULT_ADDRESS_STREET,
                    doorNumber = DEFAULT_ADDRESS_DOOR_NUMBER,
                    city = DEFAULT_ADDRESS_CITY,
                    state = DEFAULT_ADDRESS_STATE,
                    country = DEFAULT_ADDRESS_COUNTRY,
                    postalCode = DEFAULT_ADDRESS_ZIP_CODE
                )
            ),
            paymentInstrument = mockTokenizedCard()
        )
        val paymentRequest = AdyenRequestBuilder.prepareAuthorizeRequests(authorizeRequest)
        assertMandatoryAdyenMappings(authorizeRequest, paymentRequest)
        assertEquals(authorizeRequest.order.deliveryAddress?.city, paymentRequest.deliveryAddress.city)
        assertEquals(authorizeRequest.order.deliveryAddress?.street, paymentRequest.deliveryAddress.street)
        assertEquals(authorizeRequest.order.deliveryAddress?.country.toString(), paymentRequest.deliveryAddress.country)
        assertEquals(
            authorizeRequest.order.deliveryAddress?.doorNumber,
            paymentRequest.deliveryAddress.houseNumberOrName
        )
        assertEquals(authorizeRequest.order.deliveryAddress?.state, paymentRequest.deliveryAddress.stateOrProvince)
        assertEquals(authorizeRequest.order.deliveryAddress?.postalCode, paymentRequest.deliveryAddress.postalCode)
    }

//    @Test
//    fun `should create PaymentRequest successfully having AcceptHeader sent`() {
//        val adyenServiceRequest = AdyenTestUtils.createAdyenAuthorizeRequest()
//        adyenServiceRequest.browserAcceptHeader = DEFAULT_BROWSER_ACCEPT_HEADER
//        adyenServiceRequest.isShouldSendBrowserInfo = false
//
//        val request = AdyenRequestBuilder.prepareAuthorizeRequest(adyenServiceRequest)
//        assertMandatoryAdyenMappings(adyenServiceRequest, request)
//        assertNull(request.browserInfo)
//    }

    @Test
    fun `should create PaymentsRequest successfully having AcceptHeader sent for other payment methods than card`() {
        val authorizeRequest = mockAuthorizeRequest(
            clientContext = mockClientContext(
                ipAddress = null,
                userAgent = null,
                acceptHeader = DEFAULT_BROWSER_ACCEPT_HEADER
            ),
            providerConfig = mockProviderConfig(identifier = PAYMENT_METHOD_TEST, shouldSendBrowserInfo = false),
            paymentInstrument = mockTokenizedCard()
        )
        val paymentsRequest = AdyenRequestBuilder.prepareAuthorizeRequests(authorizeRequest)
        assertMandatoryAdyenMappings(authorizeRequest, paymentsRequest)
        assertNull(paymentsRequest.browserInfo)
    }

//    @Test
//    fun `should create PaymentRequest successfully having UserAgent sent`() {
//        val adyenServiceRequest = AdyenTestUtils.createAdyenAuthorizeRequest()
//        adyenServiceRequest.browserUserAgent = DEFAULT_BROWSER_USER_AGENT
//        adyenServiceRequest.isShouldSendBrowserInfo = false
//
//        val request = AdyenRequestBuilder.prepareAuthorizeRequest(adyenServiceRequest)
//        assertMandatoryAdyenMappings(adyenServiceRequest, request)
//        assertNull(request.browserInfo)
//    }

    @Test
    fun `should create PaymentsRequest successfully having UserAgent sent for other payment methods than card`() {
        val authorizeRequest = mockAuthorizeRequest(
            clientContext = mockClientContext(
                ipAddress = null,
                userAgent = DEFAULT_BROWSER_USER_AGENT,
                acceptHeader = DEFAULT_BROWSER_ACCEPT_HEADER
            ),
            providerConfig = mockProviderConfig(identifier = PAYMENT_METHOD_TEST, shouldSendBrowserInfo = false),
            paymentInstrument = mockTokenizedCard()
        )
        val request = AdyenRequestBuilder.prepareAuthorizeRequests(authorizeRequest)
        assertMandatoryAdyenMappings(authorizeRequest, request)
        assertNull(request.browserInfo)
    }

//    @Test
//    fun `should create PaymentRequest successfully having no AcceptHeader sent`() {
//        val adyenServiceRequest = AdyenTestUtils.createAdyenAuthorizeRequest()
//        adyenServiceRequest.isShouldSendBrowserInfo = false
//
//        val request = AdyenRequestBuilder.prepareAuthorizeRequest(adyenServiceRequest)
//        assertMandatoryAdyenMappings(adyenServiceRequest, request)
//        assertNull(request.browserInfo)
//    }

    @Test
    fun `should create PaymentsRequest successfully having no BrowserInfo sent for other payment methods than card`() {
        val authorizeRequest = mockAuthorizeRequest(
            clientContext = mockClientContext(null, null, null),
            paymentInstrument = mockTokenizedCard(),
            providerConfig = mockProviderConfig(identifier = PAYMENT_METHOD_TEST, shouldSendBrowserInfo = false)
        )

        val request = AdyenRequestBuilder.prepareAuthorizeRequests(authorizeRequest)
        assertMandatoryAdyenMappings(authorizeRequest, request)
        assertNull(request.browserInfo)
    }

    @Test
    fun `should create PaymentsRequest successfully having no BrowserInfo sent for other payment methods than card #2`() {
        val authorizeRequest = mockAuthorizeRequest(
            clientContext = mockClientContext(null, null, null),
            providerConfig = mockProviderConfig(identifier = PAYMENT_METHOD_TEST),
            paymentInstrument = mockTokenizedCard()
        )

        val request = AdyenRequestBuilder.prepareAuthorizeRequests(authorizeRequest)
        assertMandatoryAdyenMappings(authorizeRequest, request)
        assertNull(request.browserInfo)
    }

//    @Test
//    fun `should create PaymentRequest successfully which browser info should be set`() {
//        val adyenServiceRequest = AdyenTestUtils.createAdyenAuthorizeRequest()
//        adyenServiceRequest.isShouldSendBrowserInfo = true
//        adyenServiceRequest.returnUrl = DEFAULT_RETURN_URL
//        adyenServiceRequest.browserAcceptHeader = DEFAULT_BROWSER_ACCEPT_HEADER
//        adyenServiceRequest.browserUserAgent = DEFAULT_BROWSER_USER_AGENT
//
//        val request = AdyenRequestBuilder.prepareAuthorizeRequest(adyenServiceRequest)
//        assertMandatoryAdyenMappings(adyenServiceRequest, request)
//        assertEquals(DEFAULT_BROWSER_ACCEPT_HEADER, request.browserInfo.acceptHeader)
//        assertEquals(DEFAULT_BROWSER_USER_AGENT, request.browserInfo.userAgent)
//        assertEquals(adyenServiceRequest.returnUrl, request.additionalData[ADDITIONAL_DATA_RETURN_URL])
//    }
//

    @Test
    fun `should create PaymentsRequest successfully which browser info should be set for other payment methods than card`() {
        val authorizeRequest = mockAuthorizeRequest(
            transaction = mockTransaction(
                operationType = OperationType.AUTHORIZE,
                returnUrlInfo = ReturnUrlInfo(success = DEFAULT_RETURN_URL)
            ),
            clientContext = mockClientContext(
                ipAddress = DEFAULT_USER_IP_ADDRESS,
                userAgent = DEFAULT_BROWSER_USER_AGENT,
                acceptHeader = DEFAULT_BROWSER_ACCEPT_HEADER
            ),
            providerConfig = mockProviderConfig(identifier = PAYMENT_METHOD_TEST, shouldSendBrowserInfo = true),
            paymentInstrument = mockTokenizedCard()
        )

        val request = AdyenRequestBuilder.prepareAuthorizeRequests(authorizeRequest)
        assertMandatoryAdyenMappings(authorizeRequest, request)
        assertEquals(DEFAULT_BROWSER_ACCEPT_HEADER, request.browserInfo.acceptHeader)
        assertEquals(DEFAULT_BROWSER_USER_AGENT, request.browserInfo.userAgent)
        assertEquals(
            authorizeRequest.transaction.returnUrlInfo?.success,
            request.additionalData[ADDITIONAL_DATA_RETURN_URL]
        )
    }

//    @Test
//    fun `should create PaymentRequest successfully with additional data`() {
//        val additionalDataMap = HashMap<String, String>()
//        additionalDataMap[DEFAULT_ADDITIONAL_PARAM_KEY_1] = DEFAULT_ADDITIONAL_PARAM_VALUE_1
//        additionalDataMap[DEFAULT_ADDITIONAL_PARAM_KEY_2] = DEFAULT_ADDITIONAL_PARAM_VALUE_2
//        val adyenServiceRequest = AdyenTestUtils.createAdyenAuthorizeRequest()
//        adyenServiceRequest.additionalData = additionalDataMap
//
//        val request = AdyenRequestBuilder.prepareAuthorizeRequest(adyenServiceRequest)
//        assertMandatoryAdyenMappings(adyenServiceRequest, request)
//        for (entry in adyenServiceRequest.additionalData!!.entries) {
//            assertEquals(entry.value, request.additionalData[entry.key])
//        }
//    }

    @Test
    fun `should create PaymentsRequest successfully with additional data for other payment methods than card`() {
        val authorizeRequest = mockAuthorizeRequest(
            additionalData = mapOf(
                DEFAULT_ADDITIONAL_PARAM_KEY_1 to DEFAULT_ADDITIONAL_PARAM_VALUE_1,
                DEFAULT_ADDITIONAL_PARAM_KEY_2 to DEFAULT_ADDITIONAL_PARAM_VALUE_2
            ),
            paymentInstrument = mockTokenizedCard()
        )

        val request = AdyenRequestBuilder.prepareAuthorizeRequests(authorizeRequest)
        assertMandatoryAdyenMappings(authorizeRequest, request)
        for (entry in authorizeRequest.additionalData!!.entries) {
            assertEquals(entry.value, request.additionalData[entry.key])
        }
    }

//    @Test
//    fun `should create CancelOrRefundRequest successfully`() {
//        val adyenRefundRequest = AdyenTestUtils.createAdyenRefundRequest()
//        val request = AdyenRequestBuilder.prepareRefundRequest(adyenRefundRequest)
//        assertNotNull(request)
//        assertEquals(adyenRefundRequest.config.merchantAccount(), request.merchantAccount)
//        assertEquals(adyenRefundRequest.originalReference, request.originalReference)
//    }

    @Test
    fun `should create RefundRequest successfully`() {
        val refundRequest = mockRefundRequest(originalOperationId = DEFAULT_ORIGINAL_REFERENCE, amount = mockMoney(currency = Currency.valueOf(DEFAULT_CURRENCY), amount = DEFAULT_AMOUNT))
        val request = AdyenRequestBuilder.prepareRefundRequest(refundRequest)
        assertNotNull(request)
        assertEquals(getAdyenConfigData(refundRequest).merchantAccount(), request.merchantAccount)
        assertEquals(refundRequest.originalOperationId, request.originalReference)
    }

    @Test
    fun `should create CancelRequest successfully`() {
        val cancelRequest = mockCancelRequest()
        val request = AdyenRequestBuilder.prepareCancelRequest(cancelRequest)
        assertNotNull(request)
        assertEquals(getAdyenConfigData(cancelRequest).merchantAccount(), request.merchantAccount)
        assertEquals(cancelRequest.originalOperationId, request.originalReference)
    }

    @Test
    fun `should create CaptureRequest successfully`() {
        val captureRequest = mockCaptureRequest(amount = null)
        val request = AdyenRequestBuilder.prepareCaptureRequest(captureRequest)
        assertNotNull(request)
        assertEquals(getAdyenConfigData(captureRequest).merchantAccount(), request.merchantAccount)
        assertEquals(captureRequest.originalOperationId, request.originalReference)
        assertNull(request.modificationAmount)
    }

    @Test
    fun `should create CaptureRequest (partial) successfully`() {
        val captureRequest = mockCaptureRequest(
            amount = mockMoney(currency = Currency.MVR, amount = 100)
        )
        val request = AdyenRequestBuilder.prepareCaptureRequest(captureRequest)
        assertNotNull(request)
        assertEquals(getAdyenConfigData(captureRequest).merchantAccount(), request.merchantAccount)
        assertEquals(captureRequest.originalOperationId, request.originalReference)
        assertEquals(captureRequest.amount?.currency?.toString(), request.modificationAmount.currency)
        assertEquals(captureRequest.amount?.value?.toLong(), request.modificationAmount.value)
    }

    //TODO::Test related to adyencreditcard which has a dependency AdyenGetCardsRequest which is not transitioned to COMMON LIB
//    @Test
//    fun testCreateGetCardsRequest_Success() {
//        val adyenGetCardsRequest = AdyenTestUtils.createAdyenGetCardsRequest()
//        val request = AdyenRequestBuilder.prepareGetCardsRequest(adyenGetCardsRequest)
//        assertNotNull(request)
//        assertEquals(adyenGetCardsRequest.getConfig().getMerchantAccount(), request.getMerchantAccount())
//        assertEquals(adyenGetCardsRequest.getUserId(), request.getShopperReference())
//        assertEquals(Recurring.ContractEnum.RECURRING, request.getRecurring().getContract())
//    }

    //TODO::Test related to adyencreditcard which has methods not implemented in COMMON LIB
//    @Test
//    fun testCreateDisableCardsRequest_Success() {
//        val adyenDisableCardRequest = AdyenTestUtils.createAdyenDisableCardsRequest()
//        val request = AdyenRequestBuilder.prepareDisableCardRequest(adyenDisableCardRequest)
//        assertNotNull(request)
//        assertEquals(adyenDisableCardRequest.getConfig().getMerchantAccount(), request.getMerchantAccount())
//        assertEquals(adyenDisableCardRequest.getUserId(), request.getShopperReference())
//        assertEquals(adyenDisableCardRequest.getToken(), request.getRecurringDetailReference())
//    }

    //TODO::Test related to adyencreditcard which has methods not implemented in COMMON LIB
//    @Test
//    fun testCreateValidate3DsRequest_Success() {
//        val authorizeRequest = AdyenTestUtils.createAdyenValidate3DsRequest()
//        val request = AdyenRequestBuilder.prepareValidate3DsRequest(authorizeRequest)
//        assertNotNull(request)
//        assertEquals(authorizeRequest.md, request.md)
//        assertEquals(authorizeRequest.paRes, request.paResponse)
//        assertEquals(authorizeRequest.currency, request.amount.currency)
//        assertEquals(authorizeRequest.amount, request.amount.value)
//        assertEquals(authorizeRequest.userId, request.shopperReference)
//        assertEquals(Recurring.ContractEnum.ONECLICK_RECURRING, request.recurring.contract)
//    }
//
    //TODO::Test related to adyencreditcard which has methods not implemented in COMMON LIB
//    @Test
//    fun testCreateValidate3DsRequest_NonRecurring() {
//        val authorizeRequest = AdyenTestUtils.createAdyenValidate3DsRequest()
//        authorizeRequest.isShouldTokenizeCard = false
//        val request = AdyenRequestBuilder.prepareValidate3DsRequest(authorizeRequest)
//        assertNotNull(request)
//        assertEquals(authorizeRequest.md, request.md)
//        assertEquals(authorizeRequest.paRes, request.paResponse)
//        assertEquals(authorizeRequest.currency, request.amount.currency)
//        assertEquals(authorizeRequest.amount, request.amount.value)
//        assertNull(request.shopperReference)
//        assertNull(request.recurring)
//    }
//
    //TODO::Test related to adyencreditcard which has methods not implemented in COMMON LIB
//    @Test
//    fun testCreateValidate3DsRequest_NoUserId() {
//        val authorizeRequest = AdyenTestUtils.createAdyenValidate3DsRequest()
//        authorizeRequest.userId = null
//        val request = AdyenRequestBuilder.prepareValidate3DsRequest(authorizeRequest)
//        assertNotNull(request)
//        assertEquals(authorizeRequest.md, request.md)
//        assertEquals(authorizeRequest.paRes, request.paResponse)
//        assertEquals(authorizeRequest.currency, request.amount.currency)
//        assertEquals(authorizeRequest.amount, request.amount.value)
//        assertNull(request.shopperReference)
//        assertNull(request.recurring)
//    }
//
//    private fun assertTokenizedCardDetails(adyenServiceRequest: AdyenAuthorizeRequest, request: PaymentRequest) {
//        assertEquals(PaymentRequest.RecurringProcessingModelEnum.CARD_ON_FILE, request.recurringProcessingModel)
//        assertEquals(
//            adyenServiceRequest.cardToken,
//            request.selectedRecurringDetailReference
//        )
//        assertEquals(AbstractPaymentRequest.ShopperInteractionEnum.CONTAUTH, request.shopperInteraction)
//        assertEquals(Recurring.ContractEnum.RECURRING, request.recurring.contract)
//    }

//    private fun assertMandatoryAdyenMappings(
//        adyenServiceRequest: AdyenAuthorizeRequest,
//        request: PaymentRequest,
//        shopperInteractionEnum: AbstractPaymentRequest.ShopperInteractionEnum = AbstractPaymentRequest.ShopperInteractionEnum.ECOMMERCE
//    ) {
//        assertNotNull(request)
//        assertEquals(adyenServiceRequest.config.merchantAccount(), request.merchantAccount)
//        assertEquals(adyenServiceRequest.amount, request.amount.value)
//        assertEquals(adyenServiceRequest.currency, request.amount.currency)
//        assertEquals(adyenServiceRequest.globalPaymentTransactionId, request.metadata[GLOBAL_TRANSACTION_ID])
//        assertEquals(adyenServiceRequest.reference, request.reference)
//        assertEquals(adyenServiceRequest.config.merchantAccount(), request.merchantAccount)
//        assertEquals(adyenServiceRequest.userId, request.shopperReference)
//        assertEquals(adyenServiceRequest.userEmail, request.shopperEmail)
//        assertEquals(adyenServiceRequest.userPhone, request.telephoneNumber)
//        assertEquals(adyenServiceRequest.userFirstName, request.shopperName.firstName)
//        assertEquals(adyenServiceRequest.userLastName, request.shopperName.lastName)
//        assertEquals(adyenServiceRequest.userIpAddress, request.shopperIP)
//        assertEquals(shopperInteractionEnum, request.shopperInteraction)
//    }

    private fun assertMandatoryAdyenMappings(
        authorizeRequest: AuthorizeRequest,
        paymentsRequest: PaymentsRequest,
        shopperInteractionEnum: PaymentsRequest.ShopperInteractionEnum = PaymentsRequest.ShopperInteractionEnum.ECOMMERCE
    ) {
        assertNotNull(paymentsRequest)
        assertEquals(authorizeRequest.providerConfig.config[MERCHANT_ACCOUNT], paymentsRequest.merchantAccount)
        assertEquals(authorizeRequest.transaction.amount.value.longValueExact(), paymentsRequest.amount.value)
        assertEquals(authorizeRequest.transaction.amount.currency.toString(), paymentsRequest.amount.currency)
        assertEquals(authorizeRequest.transaction.id, paymentsRequest.metadata[GLOBAL_TRANSACTION_ID])
        assertEquals(authorizeRequest.order.id, paymentsRequest.reference)
        assertEquals(authorizeRequest.customer.id, paymentsRequest.shopperReference)
        assertEquals(authorizeRequest.customer.email, paymentsRequest.shopperEmail)
        assertEquals(authorizeRequest.customer.phone?.number, paymentsRequest.telephoneNumber)
        assertEquals(authorizeRequest.customer.name, paymentsRequest.shopperName.firstName)
        assertEquals(authorizeRequest.customer.lastName, paymentsRequest.shopperName.lastName)
        assertEquals(authorizeRequest.clientContext?.ipAddress, paymentsRequest.shopperIP)
        assertEquals(shopperInteractionEnum, paymentsRequest.shopperInteraction)
    }

    companion object {
        private val DEFAULT_ADDRESS_CITY = "Test"
        private val DEFAULT_ADDRESS_STREET = "Street"
        private val DEFAULT_ADDRESS_COUNTRY = Country.CA
        private val DEFAULT_ADDRESS_DOOR_NUMBER = "1"
        private val DEFAULT_ADDRESS_STATE = "Alabama"
        private val DEFAULT_ADDRESS_ZIP_CODE = "12345"
        private val DEFAULT_RETURN_URL = "abd"
        private val DEFAULT_BROWSER_ACCEPT_HEADER = "all"
        private val DEFAULT_BROWSER_USER_AGENT = "agent"
        private val DEFAULT_USER_IP_ADDRESS = "8.8.8.8"
        private val DEFAULT_ADDITIONAL_PARAM_KEY_1 = "key1"
        private val DEFAULT_ADDITIONAL_PARAM_VALUE_1 = "value1"
        private val DEFAULT_ADDITIONAL_PARAM_KEY_2 = "key2"
        private val DEFAULT_ADDITIONAL_PARAM_VALUE_2 = "value2"
        private val DEFAULT_CARD_TOKEN = "token123"
        private val DEFAULT_RAW_CVV = "123"
        private val DEFAULT_ENCRYPTED_CVV = "k423k4"
        private val DEFAULT_ENCRYPTED_CARD_DATA = "kalj43"
        private val DEFAULT_ENCRYPTED_CARD_NUMBER = "dfa3"
        private val DEFAULT_ENCRYPTED_EXPIRY_MONTH = "434sd"
        private val DEFAULT_ENCRYPTED_EXPIRY_YEAR = "424dw"
        private val DEFAULT_CARD_HOLDER_NAME = "Holder Name"
        private val ADYEN_ADDITIONAL_DATA_ENCRYPTED_CVV = "encryptedSecurityCode"
        private val MERCHANT_ACCOUNT = "merchantAccount"
        val DEFAULT_CURRENCY = "UYU"
        val DEFAULT_AMOUNT = 100L
        val DEFAULT_ORIGINAL_REFERENCE = "oRef123"
    }
}
