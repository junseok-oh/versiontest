package com.deliveryhero.alfred.connector.adyen.common

import com.adyen.model.Address
import com.adyen.model.Name
import com.adyen.model.PaymentResult
import com.adyen.model.checkout.DefaultPaymentMethodDetails
import com.adyen.model.checkout.PaymentsRequest
import com.adyen.model.checkout.PaymentsResponse
import com.adyen.model.modification.CancelOrRefundRequest
import com.adyen.model.modification.CancelRequest
import com.adyen.model.modification.CaptureRequest
import com.adyen.model.modification.ModificationResult
import com.adyen.model.recurring.DisableRequest
import com.adyen.model.recurring.RecurringDetailsRequest
import com.adyen.service.exception.ApiException
import com.deliveryhero.alfred.connector.adyen.common.dom.AdyenConfigData
import com.deliveryhero.alfred.connector.adyen.common.util.AdyenRequestBuilder
import com.deliveryhero.alfred.connector.mockProviderConfig
import com.deliveryhero.alfred.connector.sdk.operation.request.config.ProviderConfig
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus

class AdyenClientTest {

    var API_KEY = "apiKey"
    var MERCHANT_ACCOUNT = "merchantAccount"
    private val providerConfig =
        mockProviderConfig(AdyenGatewayTest.DEFAULT_PROVIDER_IDENTIFIER)
    private val adyenConfig = getAdyenConfig(providerConfig)
    @Test
    fun testGetCards_NoCredentials() {
        val client = AdyenClient(getAdyenConfig(adyenConfig))
        val request = RecurringDetailsRequest()
        val ex = assertThrows(ApiException::class.java) {
            client.getCards(request)
        }
        assertForbiddenException(ex)
    }

    @Test
    fun testGetCards_InvalidCredentials() {
        val config = getTestConfigFromIncorrectConfigData("11111", "22222")
        val client = AdyenClient(getAdyenConfig(config))
        val request = RecurringDetailsRequest()
        val ex = assertThrows(ApiException::class.java) { client.getCards(request) }
        assertUnauthorizedException(ex)
    }

    @Test
    fun testGetCards_SuccessEmpty() {
        val client = AdyenClient(getAdyenConfig(adyenConfig))
        val request = RecurringDetailsRequest()
        request.merchantAccount = adyenConfig.merchantAccount()
        request.selectRecurringContract()
        request.shopperReference = "thisshouldideallyneverbeareference1234567890"
        val response = client.getCards(request)
        assertNotNull(response)
        assertNotNull(response.recurringDetails)
        assertTrue(response.recurringDetails.isEmpty())
    }

    @Test
    fun testGetCards_SuccessOneCard() {
        //authorize call to tokenize card
        val client = AdyenClient(getAdyenConfig(adyenConfig))
        val requestAuthorize = prepareValidAuthorizeRequestWithOptionals()
        val paymentMethodDetails = requestAuthorize.paymentMethod as DefaultPaymentMethodDetails
        requestAuthorize.shopperReference = "user_" + System.currentTimeMillis()
        requestAuthorize.merchantAccount = adyenConfig.merchantAccount()
        val responseAuthorize = client.authorize(requestAuthorize)
        assertEquals(PaymentsResponse.ResultCodeEnum.AUTHORISED, responseAuthorize.resultCode)
        //get cards call
        val request = RecurringDetailsRequest()
        request.merchantAccount = adyenConfig.merchantAccount()
        request.selectRecurringContract()
        request.shopperReference = requestAuthorize.shopperReference
        val response = client.getCards(request)
        //asserts
        assertNotNull(response)
        assertEquals(1, response.recurringDetails.size)
        val detail = response.recurringDetails.get(0)
        assertNotNull(detail.creationDate)
        assertEquals(paymentMethodDetails.number.substring(12, 16), detail.card.number)
        assertEquals(
            responseAuthorize.getAdditionalDataByKey("recurring.recurringDetailReference"),
            detail.recurringDetailReference
        )
        assertEquals(
            paymentMethodDetails.number.substring(0, 6),
            detail.additionalData.get("cardBin")
        )
        assertEquals(paymentMethodDetails.expiryMonth, "0" + detail.card.expiryMonth)
        assertEquals(paymentMethodDetails.expiryYear, detail.card.expiryYear)
        assertEquals(paymentMethodDetails.holderName, detail.card.holderName)
    }

    @Test
    fun testDisableCard_NoCredentials() {
        val config = getTestConfigFromIncorrectConfigData("", "")
        val client = AdyenClient(config)
        val request = DisableRequest()
        val ex = assertThrows(ApiException::class.java) { client.disableCard(request) }
        assertUnauthorizedException(ex)
    }

    @Test
    fun testDisableCard_InvalidCredentials() {
        val config = getTestConfigFromIncorrectConfigData("11111", "22222")
        val client = AdyenClient(config)
        val request = DisableRequest()
        val ex = assertThrows(ApiException::class.java) { client.disableCard(request) }
        assertUnauthorizedException(ex)
    }

    @Test
    fun testDisableCard_InvalidToken() {
        val client = AdyenClient(getAdyenConfig(adyenConfig))
        val request = DisableRequest()
        request.merchantAccount = adyenConfig.merchantAccount()
        request.shopperReference = "12345"
        request.recurringDetailReference = System.currentTimeMillis().toString()
        val ex = assertThrows(ApiException::class.java) { client.disableCard(request) }
        assertValidationError(ex, "PaymentDetail not found")
    }

    @Test
    fun testDisableCard_Success() {
        //authorize call to tokenize card
        val client = AdyenClient(getAdyenConfig(adyenConfig))
        val requestAuthorize = prepareValidAuthorizeRequestWithOptionals()
        val shopperReference = "user_" + System.currentTimeMillis()
        requestAuthorize.shopperReference = shopperReference
        requestAuthorize.merchantAccount = adyenConfig.merchantAccount()
        val responseAuthorize = client.authorize(requestAuthorize)
        assertEquals(PaymentsResponse.ResultCodeEnum.AUTHORISED, responseAuthorize.resultCode)
        //get cards call
        val request = RecurringDetailsRequest()
        request.merchantAccount = adyenConfig.merchantAccount()
        request.selectRecurringContract()
        request.shopperReference = shopperReference
        val response = client.getCards(request)
        //assert context created
        assertNotNull(response)
        assertEquals(1, response.recurringDetails.size)
        val detail = response.recurringDetails.get(0)
        assertNotNull(detail.creationDate)
        val paymentMethodDetails = requestAuthorize.paymentMethod as DefaultPaymentMethodDetails
        assertEquals(paymentMethodDetails.number.substring(12, 16), detail.card.number)
        assertEquals(
            responseAuthorize.getAdditionalDataByKey("recurring.recurringDetailReference"),
            detail.recurringDetailReference
        )
        assertEquals(paymentMethodDetails.number.substring(0, 6), detail.additionalData.get("cardBin"))
        assertEquals(paymentMethodDetails.expiryMonth, "0" + detail.card.expiryMonth)
        assertEquals(paymentMethodDetails.expiryYear, detail.card.expiryYear)
        assertEquals(paymentMethodDetails.holderName, detail.card.holderName)
        //disable call
        val token = response.recurringDetails.get(0).recurringDetailReference
        val disableRequest = DisableRequest()
        disableRequest.merchantAccount = adyenConfig.merchantAccount()
        disableRequest.shopperReference = shopperReference
        disableRequest.recurringDetailReference = token
        val disableResult = client.disableCard(disableRequest)
        assertNotNull(disableResult)
        assertEquals("[detail-successfully-disabled]", disableResult.response)
        //check cards changed
        val cardsAfterRequest = RecurringDetailsRequest()
        cardsAfterRequest.merchantAccount = adyenConfig.merchantAccount()
        cardsAfterRequest.selectRecurringContract()
        cardsAfterRequest.shopperReference = shopperReference
        val cardsAfterResponse = client.getCards(cardsAfterRequest)
        //assert context created
        assertNotNull(cardsAfterResponse)
        assertEquals(0, cardsAfterResponse.recurringDetails.size)
    }

    @Test
    fun testCancelNoCredentials() {
        val client = AdyenClient(getAdyenConfig(adyenConfig))
        val request = CancelRequest()
        val ex =
            assertThrows(ApiException::class.java) { client.cancel(request) }
        assertForbiddenException(ex)
    }

    @Test
    fun testCancelNoParams() {
        val client = AdyenClient(getAdyenConfig(adyenConfig))
        val request = CancelRequest()
        request.merchantAccount = adyenConfig.merchantAccount()
        request.reference = ""
        request.originalReference = ""
        val ex = assertThrows(ApiException::class.java) { client.cancel(request) }
        assertValidationError(ex, "Original pspReference required for this operation")
    }

    @Test
    fun testCancelInvalidOriginalReference() {
        val client = AdyenClient(getAdyenConfig(adyenConfig))
        val request = CancelRequest()
        request.merchantAccount = adyenConfig.merchantAccount()
        request.reference = "222"
        request.originalReference = "111"
        val ex = assertThrows(ApiException::class.java) { client.cancel(request) }
        assertValidationError(ex, "Original pspReference required for this operation")
    }

    @Test
    fun testCancelSuccess() {
        val client = AdyenClient(getAdyenConfig(adyenConfig))
        val requestAuthorize = prepareValidAuthorizeRequestWithOptionals()
        requestAuthorize.merchantAccount = adyenConfig.merchantAccount()
        val responseAuthorize = client.authorize(requestAuthorize)
        assertEquals(PaymentsResponse.ResultCodeEnum.AUTHORISED, responseAuthorize.resultCode)
        val request = CancelRequest()
        request.merchantAccount = adyenConfig.merchantAccount()
        request.reference = "222"
        request.originalReference = responseAuthorize.pspReference
        val response = client.cancel(request)
        assertNotNull(response)

        assertNotNull(response.pspReference)
        assertEquals(ModificationResult.ResponseEnum.CANCEL_RECEIVED_, response.response)
    }

    @Test
    fun testRefund_NoCredentials() {
        val client = AdyenClient(getAdyenConfig(adyenConfig))
        val request = CancelOrRefundRequest()
        val ex = assertThrows(ApiException::class.java) { client.refund(request) }
        assertForbiddenException(ex)
    }

    @Test
    fun testRefund_NoParams() {
        val client = AdyenClient(getAdyenConfig(adyenConfig))
        val request = CancelOrRefundRequest()
        request.merchantAccount = adyenConfig.merchantAccount()
        request.reference = ""
        request.originalReference = ""
        val ex = assertThrows(ApiException::class.java) { client.refund(request) }
        assertValidationError(ex, "Original pspReference required for this operation")
    }

    @Test
    fun testRefund_InvalidOriginalReference() {
        val client = AdyenClient(getAdyenConfig(adyenConfig))
        val request = CancelOrRefundRequest()
        request.merchantAccount = adyenConfig.merchantAccount()
        request.reference = "222"
        request.originalReference = "111"
        val ex = assertThrows(ApiException::class.java) { client.refund(request) }
        assertValidationError(ex, "Original pspReference required for this operation")
    }

    @Test
    fun testRefund_Success() {
        //previous authorize call to refund
        val client = AdyenClient(getAdyenConfig(adyenConfig))
        val requestAuthorize = prepareValidAuthorizeRequestWithOptionals()
        requestAuthorize.merchantAccount = adyenConfig.merchantAccount()
        val responseAuthorize = client.authorize(requestAuthorize)
        assertEquals(PaymentsResponse.ResultCodeEnum.AUTHORISED, responseAuthorize.resultCode)
        //refund request
        val request = CancelOrRefundRequest()
        request.merchantAccount = adyenConfig.merchantAccount()
        request.reference = "222"
        request.originalReference = responseAuthorize.pspReference
        val response = client.refund(request)
        //asserts
        assertNotNull(response)
        assertNotNull(response.pspReference)
        assertEquals(ModificationResult.ResponseEnum.CANCELORREFUND_RECEIVED_, response.response)
    }

    @Test
    fun testRefund_DoubleRefund() {
        //previous authorize call to refund
        val client = AdyenClient(getAdyenConfig(adyenConfig))
        val requestAuthorize = prepareValidAuthorizeRequestWithOptionals()
        requestAuthorize.merchantAccount = adyenConfig.merchantAccount()
        val responseAuthorize = client.authorize(requestAuthorize)
        assertEquals(PaymentsResponse.ResultCodeEnum.AUTHORISED, responseAuthorize.resultCode)
        //first refund
        val request1 = CancelOrRefundRequest()
        request1.merchantAccount = adyenConfig.merchantAccount()
        request1.reference = "222"
        request1.originalReference = responseAuthorize.pspReference
        val response1 = client.refund(request1)
        assertEquals(ModificationResult.ResponseEnum.CANCELORREFUND_RECEIVED_, response1.response)
        //second refund
        val request2 = CancelOrRefundRequest()
        request2.merchantAccount = adyenConfig.merchantAccount()
        request2.reference = "222"
        request2.originalReference = responseAuthorize.pspReference
        val response2 = client.refund(request2)
        assertEquals(ModificationResult.ResponseEnum.CANCELORREFUND_RECEIVED_, response2.response)
    }

    @Test
    fun testCapture_Success() {
        //previous authorize call to refund
        val client = AdyenClient(getAdyenConfig(adyenConfig))
        val requestAuthorize = prepareValidAuthorizeRequestWithOptionals()
        requestAuthorize.merchantAccount = adyenConfig.merchantAccount()
        val responseAuthorize = client.authorize(requestAuthorize)
        assertEquals(PaymentsResponse.ResultCodeEnum.AUTHORISED, responseAuthorize.resultCode)
        //capture request
        val request = CaptureRequest()
        request.merchantAccount = adyenConfig.merchantAccount()
        request.reference = "222"
        request.originalReference = responseAuthorize.pspReference
        request.modificationAmount = requestAuthorize.amount
        val response = client.capture(request)
        //asserts
        assertNotNull(response)
        assertNotNull(response.pspReference)
        assertEquals(ModificationResult.ResponseEnum.CAPTURE_RECEIVED_, response.response)
    }

    @Test
    fun testAuthorize_NoCredentials() {
        val config = getTestConfigFromIncorrectConfigData("", "")
        val client = AdyenClient(getAdyenConfig(config))
        val request = PaymentsRequest()
        val ex = assertThrows(ApiException::class.java) { client.authorize(request) }
        assertUnauthorizedException(ex)
    }

    @Test
    fun testAuthorize_NoParams() {
        val client = AdyenClient(getAdyenConfig(adyenConfig))
        val request = PaymentsRequest()
        request.reference = ""
        val ex = assertThrows(ApiException::class.java) { client.authorize(request) }
        assertNotNull(ex)
        assertEquals("Invalid Merchant Account", ex.error.message)
        assertEquals("security", ex.error.errorType)
        assertEquals(HttpStatus.FORBIDDEN.value(), ex.statusCode)
    }

    @Test
    fun testAuthorize_Success() {
        val client = AdyenClient(getAdyenConfig(adyenConfig))
        val request = prepareValidAuthorizeRequest()
        request.merchantAccount = adyenConfig.merchantAccount()
        val response = client.authorize(request)
        assertNotNull(response)
        assertEquals(PaymentsResponse.ResultCodeEnum.AUTHORISED, response.resultCode)
        assertNotNull(response.pspReference)
    }

    @Test
    fun testAuthorize_SuccessWithOptionalsNoTokenize() {
        val client = AdyenClient(getAdyenConfig(adyenConfig))
        val request = prepareValidAuthorizeRequestWithOptionals()
        request.enableRecurring(false)
        request.merchantAccount = adyenConfig.merchantAccount()
        val response = client.authorize(request)
        assertNotNull(response)
        assertEquals(PaymentsResponse.ResultCodeEnum.AUTHORISED, response.resultCode)
        assertNotNull(response.pspReference)
        assertNull(response.getAdditionalDataByKey("recurring.recurringDetailReference"))
        assertEquals("Mathias Fonseca", response.getAdditionalDataByKey("cardHolderName"))
        assertEquals("1111", response.getAdditionalDataByKey("cardSummary"))
        assertEquals("3/2030", response.getAdditionalDataByKey("expiryDate"))
        assertEquals("411111", response.getAdditionalDataByKey("cardBin"))
    }

    @Test
    fun testAuthorize_SuccessWithOptionals() {
        val client = AdyenClient(getAdyenConfig(adyenConfig))
        val request = prepareValidAuthorizeRequestWithOptionals()
        request.merchantAccount = adyenConfig.merchantAccount()
        val response = client.authorize(request)
        assertNotNull(response)
        assertEquals(PaymentsResponse.ResultCodeEnum.AUTHORISED, response.resultCode)
        assertNotNull(response.pspReference)
        assertNotNull(response.getAdditionalDataByKey("recurring.recurringDetailReference"))
        assertEquals("Mathias Fonseca", response.getAdditionalDataByKey("cardHolderName"))
        assertEquals("1111", response.getAdditionalDataByKey("cardSummary"))
        assertEquals("3/2030", response.getAdditionalDataByKey("expiryDate"))
        assertEquals("411111", response.getAdditionalDataByKey("cardBin"))
    }

    //TODO:: Following test cases should be implemented once creditcard is ready
//    @Test
//    fun testAuthorize_SuccessWithToken() {
//        //create payment tokenizing card
//        val config = getAdyenConfig(providerConfig)
//        val request1 = prepareValidAuthorizeRequestWithOptionals()
//        val response1 = client.authorize(config, request1)
//        Assertions.assertNotNull(response1)
//        Assertions.assertEquals(PaymentResult.ResultCodeEnum.AUTHORISED, response1.getResultCode())
//        Assertions.assertNotNull(response1.getPspReference())
//        val token = response1.getAdditionalDataByKey("recurring.recurringDetailReference")
//        Assertions.assertNotNull(token)
//        //pay with tokenized card
//        val request2 = prepareValidAuthorizeRequestWithToken(token)
//        val response2 = client.authorize(config, request2)
//        Assertions.assertNotNull(response2)
//        Assertions.assertEquals(PaymentResult.ResultCodeEnum.AUTHORISED, response2.getResultCode())
//        Assertions.assertNotNull(response2.getPspReference())
//    }
//
//    @Test
//    @Throws(ApiException::class, IOException::class)
//    fun testAuthorize_SuccessWithTokenAndEmptySecurityCode() {
//        //create payment tokenizing card
//        val config = getAdyenConfig(providerConfig)
//        val request1 = prepareValidAuthorizeRequestWithOptionals()
//        val response1 = client.authorize(config, request1)
//        Assertions.assertNotNull(response1)
//        Assertions.assertEquals(PaymentResult.ResultCodeEnum.AUTHORISED, response1.getResultCode())
//        Assertions.assertNotNull(response1.getPspReference())
//        val token = response1.getAdditionalDataByKey("recurring.recurringDetailReference")
//        Assertions.assertNotNull(token)
//        //pay with tokenized card
//        val request2 = prepareValidAuthorizeRequestWithToken(token)
//        val response2 = client.authorize(config, request2)
//        Assertions.assertNotNull(response2)
//        Assertions.assertEquals(PaymentResult.ResultCodeEnum.AUTHORISED, response2.getResultCode())
//        Assertions.assertNotNull(response2.getPspReference())
//    }
//
//    @Test
//    @Throws(ApiException::class, IOException::class)
//    fun testAuthorize_WithInvalidToken() {
//        //create payment tokenizing card
//        val config = getAdyenConfig(providerConfig)
//        val request1 = prepareValidAuthorizeRequestWithOptionals()
//        val response1 = client.authorize(config, request1)
//        Assertions.assertNotNull(response1)
//        Assertions.assertEquals(PaymentResult.ResultCodeEnum.AUTHORISED, response1.getResultCode())
//        Assertions.assertNotNull(response1.getPspReference())
//        val token = response1.getAdditionalDataByKey("recurring.recurringDetailReference")
//        Assertions.assertNotNull(token)
//        //pay with tokenized card
//        val request2 = prepareValidAuthorizeRequestWithToken(token + "INVALID")
//        val ex = Assertions.assertThrows(ApiException::class.java) { adyenClient.authorize(config, request2) }
//        assertValidationError(ex, "PaymentDetail not found")
//    }

    @Disabled("because encrypted data has a short expiration, if you want to execute this test, you should get freshly encrypted data")
    @Test
    fun testAuthorize_SuccessWithEncryptedCardSeparateFields() {
        val client = AdyenClient(getAdyenConfig(adyenConfig))
        val request = prepareValidAuthorizeRequest()
        request.addCardData(null, null, null, null, null)
        val encryptedCardNumber = "YOUR_ENCRYPTED_CARD_NUMBER"
        val encryptedCardExpiryMonth = "YOUR_ENCRYPTED_EXPIRY_MONTH"
        val encryptedCardExpiryYear = "YOUR_ENCRYPTED_EXPIRY_YEAR"
        val encryptedCardSecurityCode = "YOUR_ENCRYPTED_S"
        request.addEncryptedCardData(
            encryptedCardNumber,
            encryptedCardExpiryMonth,
            encryptedCardExpiryYear,
            encryptedCardSecurityCode,
            "Test Card Holder"
        )
        val response = client.authorize(request)
        assertNotNull(response)
        assertEquals(PaymentResult.ResultCodeEnum.AUTHORISED, response.resultCode)
        assertNotNull(response.pspReference)
    }


    @Disabled("because encrypted data has a short expiration, if you want to execute this test, you should get freshly encrypted data")
    @Test
    fun testAuthorize_SuccessWithEncryptedCardUniqueField() {
        val client = AdyenClient(getAdyenConfig(adyenConfig))
        val request = prepareValidAuthorizeRequest()
        request.addCardData(null, null, null, null, null)
        val encryptedCardData = "YOUR_ENCRYPTED_DATA"
        val additionalData = HashMap<String, String>()
        additionalData["card.encrypted.json"] = encryptedCardData
        request.additionalData = additionalData
        val response = client.authorize(request)
        assertNotNull(response)
        assertEquals(PaymentResult.ResultCodeEnum.AUTHORISED, response.resultCode)
        assertNotNull(response.pspReference)
    }

    @Test
    fun testAuthorize_MissingCurrency() {
        val client = AdyenClient(getAdyenConfig(adyenConfig))
        val request = prepareValidAuthorizeRequest()
        request.merchantAccount = adyenConfig.merchantAccount()
        request.amount.currency = null
        val ex = assertThrows(ApiException::class.java) { client.authorize(request) }
        assertValidationError(ex, "Unsupported currency specified")
    }

    @Test
    fun testAuthorize_InvalidCurrency() {
        val client = AdyenClient(getAdyenConfig(adyenConfig))
        val request = prepareValidAuthorizeRequest()
        request.amount.currency = "HOLAHOLAHOLA"
        request.merchantAccount = adyenConfig.merchantAccount()
        val ex = assertThrows(ApiException::class.java) { client.authorize(request) }
        assertValidationError(ex, "Unsupported currency specified")
    }

    @Test
    fun testAuthorize_InvalidAmount() { //cannot test missing amount because Adyen assumes zero
        val client = AdyenClient(getAdyenConfig(adyenConfig))
        val request = prepareValidAuthorizeRequest()
        request.merchantAccount = adyenConfig.merchantAccount()
        request.amount.value = -10L
        val ex = assertThrows(ApiException::class.java) { client.authorize(request) }
        assertValidationError(ex, "Invalid amount specified")
    }

    @Test
    fun testAuthorize_MissingReference() {
        val client = AdyenClient(getAdyenConfig(adyenConfig))
        val request = prepareValidAuthorizeRequest()
        request.reference(null)
        val ex = assertThrows(ApiException::class.java) { client.authorize(request) }
        assertValidationError(ex, "Reference Missing")
    }

    @Test
    fun testAuthorize_EmptyReference() {
        val client = AdyenClient(getAdyenConfig(adyenConfig))
        val request = prepareValidAuthorizeRequest()
        request.reference("")
        request.merchantAccount = adyenConfig.merchantAccount()
        val ex = assertThrows(ApiException::class.java) { client.authorize(request) }
        assertValidationError(ex, "Reference Missing")
    }

    @Test
    fun testAuthorize_InvalidReference() {
        val client = AdyenClient(getAdyenConfig(adyenConfig))
        val request = prepareValidAuthorizeRequest()
        request.reference("morethan80charsmorethan80charsmorethan80charsmorethan80charsmorethan80charsmorethan80")
        val ex = assertThrows(ApiException::class.java) { client.authorize(request) }
        assertValidationError(ex, "Reference may not exceed 79 characters")
    }

    @Test
    fun testAuthorize_MissingDeliveryCountry() {
        val client = AdyenClient(getAdyenConfig(adyenConfig))
        val request = prepareValidAuthorizeRequest()
        request.deliveryAddress.country = null
        request.merchantAccount = adyenConfig.merchantAccount()
        val ex = assertThrows(ApiException::class.java) { client.authorize(request) }
        assertValidationError(ex, "Delivery address problem (Country <empty> invalid)")
    }

    @Test
    fun testAuthorize_InvalidDeliveryCountry() {
        val client = AdyenClient(getAdyenConfig(adyenConfig))
        val request = prepareValidAuthorizeRequest()
        request.merchantAccount = adyenConfig.merchantAccount()
        request.deliveryAddress.country = "HOLAHOLAHOLA"
        val ex = assertThrows(ApiException::class.java) { client.authorize(request) }
        assertValidationError(ex, "Delivery address problem (Country HOLAHOLAHOLA invalid)")
    }

    @Test
    fun testAuthorize_MissingDeliveryCity() {
        val client = AdyenClient(getAdyenConfig(adyenConfig))
        val request = prepareValidAuthorizeRequest()
        request.merchantAccount = adyenConfig.merchantAccount()
        request.deliveryAddress.city = null
        val ex = assertThrows(ApiException::class.java) { client.authorize(request) }
        assertValidationError(ex, "Delivery address problem (City)")
    }

    @Test
    fun testAuthorize_MissingDeliveryStreet() {
        val client = AdyenClient(getAdyenConfig(adyenConfig))
        val request = prepareValidAuthorizeRequest()
        request.deliveryAddress.street = null
        request.merchantAccount = adyenConfig.merchantAccount()
        val ex = assertThrows(ApiException::class.java) { client.authorize(request) }
        assertValidationError(ex, "Delivery address problem (Street)")
    }

    @Test
    fun testAuthorize_MissingDeliveryDoorNumber() {
        val client = AdyenClient(getAdyenConfig(adyenConfig))
        val request = prepareValidAuthorizeRequest()
        request.merchantAccount = adyenConfig.merchantAccount()
        request.deliveryAddress.houseNumberOrName = null
        val ex = assertThrows(ApiException::class.java) { client.authorize(request) }
        assertValidationError(ex, "Delivery address problem (HouseNumberOrName)")
    }

    @Test
    fun testAuthorize_MissingCardNumber() {
        val client = AdyenClient(getAdyenConfig(adyenConfig))
        val request = prepareValidAuthorizeRequest()
        request.merchantAccount = adyenConfig.merchantAccount()
        request.addCardData(null, null, null, null, null)
        val ex = assertThrows(ApiException::class.java) { client.authorize(request) }
        assertValidationError(ex, "Invalid card number")
    }

    @Test
    fun testAuthorize_InvalidCardNumber() {
        val client = AdyenClient(getAdyenConfig(adyenConfig))
        val request = prepareValidAuthorizeRequest()
        request.merchantAccount = adyenConfig.merchantAccount()
        request.addCardData("12345", "03", "2030", "737", "Mathias Fonseca")
        val ex = assertThrows(ApiException::class.java) { client.authorize(request) }
        assertValidationError(ex, "Invalid card number")
    }

    @Test
    fun testAuthorize_MissingCardExpiryMonth() {
        val client = AdyenClient(getAdyenConfig(adyenConfig))
        val request = prepareValidAuthorizeRequest()
        request.merchantAccount = adyenConfig.merchantAccount()
        request.addCardData("4111111111111111", null, "2030", "737", "Mathias Fonseca")
        val ex = assertThrows(ApiException::class.java) { client.authorize(request) }
        assertValidationError(ex, "Missing payment method details: expiryMonth")
    }

    @Test
    fun testAuthorize_InvalidCardExpiryMonth() {
        val client = AdyenClient(getAdyenConfig(adyenConfig))
        val request = prepareValidAuthorizeRequest()
        request.merchantAccount = adyenConfig.merchantAccount()
        request.addCardData("4111111111111111", "123", "2030", "737", "Mathias Fonseca")
        val ex = assertThrows(ApiException::class.java) { client.authorize(request) }
        assertValidationError(ex, "Expiry Date Invalid: Expiry month should be between 1 and 12 inclusive")
    }

    @Test
    fun testAuthorize_MissingCardExpiryYear() {
        val client = AdyenClient(getAdyenConfig(adyenConfig))
        val request = prepareValidAuthorizeRequest()
        request.merchantAccount = adyenConfig.merchantAccount()
        request.addCardData("4111111111111111", "03", null, "737", "Mathias Fonseca")
        val ex = assertThrows(ApiException::class.java) { client.authorize(request) }
        assertValidationError(ex, "Missing payment method details: expiryYear")
    }

    @Test
    fun testAuthorize_InvalidCardExpiryYear() {
        val client = AdyenClient(getAdyenConfig(adyenConfig))
        val request = prepareValidAuthorizeRequest()
        request.merchantAccount = adyenConfig.merchantAccount()
        request.addCardData("4111111111111111", "03", "12345", "737", "Mathias Fonseca")
        val ex = assertThrows(ApiException::class.java) { client.authorize(request) }
        assertValidationError(ex, "Expiry Date Invalid: Expiry year should be a 4 digit number greater than 2000")
    }

    @Test
    fun testAuthorize_Refused() {
        val client = AdyenClient(getAdyenConfig(adyenConfig))
        val request = prepareValidAuthorizeRequest()
        request.addCardData("4111111111111111", "03", "2030", null, "Mathias Fonseca")
        request.merchantAccount = adyenConfig.merchantAccount()
        val response = client.authorize(request)
        assertNotNull(response)
        assertEquals(PaymentsResponse.ResultCodeEnum.REFUSED, response.resultCode)
        assertNotNull(response.pspReference)
        assertNotNull(response.refusalReason)
    }

    private fun prepareValidAuthorizeRequestWithOptionals(): PaymentsRequest {
        val request = prepareValidAuthorizeRequest()
        request.shopperReference("mathi123")
        request.shopperEmail = "mathifonseca@gmail.com"
        request.telephoneNumber = "099937732"
        request.shopperIP = "192.168.1.1"
        request.shopperName = Name().firstName("Mathi").lastName("Fonseca")
        request.deliveryAddress.stateOrProvince = "Cerro Largo"
        request.deliveryAddress.postalCode = "10119"
        request.recurringProcessingModel(PaymentsRequest.RecurringProcessingModelEnum.CARD_ON_FILE)
        val metadata = HashMap<String, String>()
        metadata[AdyenRequestBuilder.GLOBAL_TRANSACTION_ID] = System.currentTimeMillis().toString()
        request.metadata = metadata
        return request
    }

    private fun prepareValidAuthorizeRequest(): PaymentsRequest {
        val request = PaymentsRequest()
        request.merchantAccount(MERCHANT_ACCOUNT)
        request.setAmountData("100", "UYU")
        request.reference("12345")
        request.deliveryAddress(
            Address()
                .country("UY")
                .city("Montevideo")
                .street("Av. 8 de Octubre")
                .houseNumberOrName("3329")
        )
        val paymentDetails = DefaultPaymentMethodDetails()
        return request.addCardData("4111111111111111", "03", "2030", "737", "Mathias Fonseca")
    }


    //TODO:: Activate these tests when credit card is fixed
//    private fun prepareValidAuthorizeRequestWithToken(token: String): PaymentsRequest {
//        val request = PaymentsRequest()
//        request.merchantAccount(AdyenTestUtils.TEST_MERCHANT_ACCOUNT)
//        request.setAmountData("100", "UYU")
//        request.reference("12345")
//        request.deliveryAddress(
//            Address()
//                .country("UY")
//                .city("Montevideo")
//                .street("Av. 8 de Octubre")
//                .houseNumberOrName("3329")
//        )
//        request.shopperReference("mathi123")
//        request.shopperEmail = "mathifonseca@gmail.com"
//        request.telephoneNumber = "099937732"
//        request.shopperIP = "192.168.1.1"
//        request.shopperName = Name().firstName("Mathi").lastName("Fonseca")
//        request.deliveryAddress.stateOrProvince = "Cerro Largo"
//        request.deliveryAddress.postalCode = "10119"
//        request.selectedRecurringDetailReference = token
//        request.shopperInteraction = AbstractPaymentRequest.ShopperInteractionEnum.CONTAUTH
//        request.recurringProcessingModel = PaymentRequest.RecurringProcessingModelEnum.CARD_ON_FILE
//        val recurring = Recurring()
//        recurring.contract = Recurring.ContractEnum.RECURRING
//        request.recurring = recurring
//        return request
//    }

    private fun getAdyenConfig(providerConfig: ProviderConfig): AdyenConfigData {
        return providerConfig as AdyenConfigData
    }

    private fun assertValidationError(ex: ApiException, expectedMessage: String) {
        assertNotNull(ex)
        assertEquals(expectedMessage, ex.error.message)
        assertEquals("validation", ex.error.errorType)
        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY.value(), ex.statusCode)
    }

    private fun assertUnauthorizedException(ex: ApiException) {
        assertNotNull(ex)
        assertEquals("security", ex.error.errorType)
        assertEquals(HttpStatus.UNAUTHORIZED.value(), ex.statusCode)
    }

    private fun assertForbiddenException(ex: ApiException) {
        assertNotNull(ex)
        assertEquals("security", ex.error.errorType)
        assertEquals(HttpStatus.FORBIDDEN.value(), ex.statusCode)
    }

    private fun getTestConfigFromIncorrectConfigData(apiKey: String, merchantAccount: String): AdyenConfigData {
        val config = getAdyenConfig(providerConfig)
        return config.setConfigData(API_KEY, apiKey).setConfigData(MERCHANT_ACCOUNT, merchantAccount)
    }

}
