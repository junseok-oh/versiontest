package com.deliveryhero.alfred.connector.adyen.common

import com.deliveryhero.alfred.connector.*
import com.deliveryhero.alfred.connector.sdk.operation.OperationType
import com.deliveryhero.alfred.connector.sdk.operation.request.CaptureRequest
import com.deliveryhero.alfred.connector.sdk.operation.request.RefundRequest
import com.deliveryhero.alfred.connector.sdk.operation.request.common.Currency
import com.deliveryhero.alfred.connector.sdk.operation.response.OperationResponse
import com.deliveryhero.alfred.connector.sdk.operation.response.OperationStatus
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class AdyenGatewayTest {

    companion object {
        const val DEFAULT_PROVIDER_IDENTIFIER = "adyen"
        const val PAYMENT_METHOD_APPLEPAY = "adyen_applepay"
        const val PAYMENT_METHOD_GOOGLEPAY = "adyen_googlepay"
    }

    private val service = AdyenGateway()

    @Test
    fun `isApplicableFor - should always return true`() {
        val config =
            mockProviderConfig(DEFAULT_PROVIDER_IDENTIFIER)
        val applicable = service.isApplicableFor(config)

        Assertions.assertTrue(applicable)
    }

    @Test
    fun `preauthorize - payment instrument for credit card`() {
        val providerConfig = mockProviderConfig("adyen")
        val request = mockAuthorizeRequest(
            transaction = mockTransaction(
                redirectResponse = mockRedirectResponse(),
                operationType = OperationType.PREAUTHORIZE
            ),
            paymentInstrument = mockTokenizedCard(),
            providerConfig = providerConfig
        )
        val response = service.preauthorize(request)

        Assertions.assertNotNull(response)
        Assertions.assertEquals(OperationStatus.OK, response.status)
        Assertions.assertEquals(OperationType.AUTHORIZE, response.type)
        Assertions.assertNotNull(response.reference)
        //TODO:: More assertions required
    }

    @Test
    fun `authorize - payment instrument for credit card`() {
        val providerConfig = mockProviderConfig("adyen")
        val request = mockAuthorizeRequest(
            transaction = mockTransaction(
                redirectResponse = mockRedirectResponse(),
                operationType = OperationType.AUTHORIZE
            ),
            paymentInstrument = mockTokenizedCard(),
            providerConfig = providerConfig
        )
        val response = service.authorize(request)

        Assertions.assertNotNull(response)
        Assertions.assertEquals(OperationStatus.OK, response.status)
        Assertions.assertEquals(OperationType.AUTHORIZE, response.type)
        Assertions.assertNotNull(response.reference)
    }

    @Test
    fun `preauthorize - payment instrument for apple pay`() {
        val providerConfig = mockProviderConfig("adyen_applepay")
        val request = mockAuthorizeRequest(
            transaction = mockTransaction(
                redirectResponse = mockRedirectResponse(),
                operationType = OperationType.PREAUTHORIZE
            ),
            paymentInstrument = mockOneTimePayment(),
            providerConfig = providerConfig
        )
        //Will not return OK response as reference is not correct since preauthorize is not possible to be carried out automatically with a valid token yet
        val response = service.authorize(request)
        Assertions.assertNotNull(response)
        Assertions.assertEquals(OperationStatus.UNKNOWN_ERROR, response.status)
        Assertions.assertEquals(OperationType.AUTHORIZE, response.type)
        Assertions.assertNull(response.reference)
    }

    @Test
    fun `preauthorize - payment instrument for google pay`() {
        val providerConfig = mockProviderConfig("adyen_googlepay")
        val request = mockAuthorizeRequest(
            transaction = mockTransaction(
                redirectResponse = mockRedirectResponse(),
                operationType = OperationType.PREAUTHORIZE
            ),
            paymentInstrument = mockOneTimePayment(),
            providerConfig = providerConfig
        )
        //Will not return OK response as reference is not correct since preauthorize is not possible to be carried out automatically with a valid token yet
        val response = service.authorize(request)
        Assertions.assertNotNull(response)
        Assertions.assertEquals(OperationStatus.UNKNOWN_ERROR, response.status)
        Assertions.assertEquals(OperationType.AUTHORIZE, response.type)
        Assertions.assertNull(response.reference)
    }

    @Test
    fun `authorize - payment instrument for apple pay`() {
        val providerConfig = mockProviderConfig("adyen_applepay")
        val request = mockAuthorizeRequest(
            transaction = mockTransaction(
                redirectResponse = mockRedirectResponse(),
                operationType = OperationType.AUTHORIZE
            ),
            paymentInstrument = mockOneTimePayment(),
            providerConfig = providerConfig
        )
        //Will not return OK response as reference is not correct since authorize is not possible to be carried out automatically with a valid token yet
        val response = service.authorize(request)
        Assertions.assertNotNull(response)
        Assertions.assertEquals(OperationStatus.UNKNOWN_ERROR, response.status)
        Assertions.assertEquals(OperationType.AUTHORIZE, response.type)
        Assertions.assertNull(response.reference)
    }

    @Test
    fun `authorize - payment instrument for google pay`() {
        val providerConfig = mockProviderConfig("adyen_googlepay")
        val request = mockAuthorizeRequest(
            transaction = mockTransaction(
                redirectResponse = mockRedirectResponse(),
                operationType = OperationType.AUTHORIZE
            ),
            paymentInstrument = mockOneTimePayment(),
            providerConfig = providerConfig
        )
        //Will not return OK response as reference is not correct since authorize is not possible to be carried out automatically with a valid token yet
        val response = service.authorize(request)
        Assertions.assertNotNull(response)
        Assertions.assertEquals(OperationStatus.UNKNOWN_ERROR, response.status)
        Assertions.assertEquals(OperationType.AUTHORIZE, response.type)
        Assertions.assertNull(response.reference)
    }

    @Test
    fun `reverse - positive case through preauthorize, cancel for credit card`() {
        val providerConfig = mockProviderConfig("adyen")
        val preauthorizeRequest = mockAuthorizeRequest(
            transaction = mockTransaction(
                redirectResponse = mockRedirectResponse(),
                operationType = OperationType.PREAUTHORIZE
            ),
            paymentInstrument = mockTokenizedCard(),
            providerConfig = providerConfig
        )
        val preauthorizeResponse = service.preauthorize(preauthorizeRequest)
        Assertions.assertNotNull(preauthorizeResponse)
        Assertions.assertEquals(OperationStatus.OK, preauthorizeResponse.status)
        Assertions.assertEquals(OperationType.AUTHORIZE, preauthorizeResponse.type)
        Assertions.assertNotNull(preauthorizeResponse.reference)
        //cancel
        val cancelRequest = mockCancelRequest(
            preauthorizeResponse.reference!!,
            providerConfig = providerConfig
        )
        val cancelResponse = service.cancel(cancelRequest)
        Assertions.assertNotNull(cancelRequest)
        Assertions.assertNotNull(cancelResponse)
        Assertions.assertEquals(OperationStatus.PENDING, cancelResponse.status)
        Assertions.assertEquals(OperationType.CANCEL, cancelResponse.type)
        Assertions.assertNotNull(cancelResponse.reference)
    }

    @Test
    fun `reverse - positive case through authorize, cancel for credit card`() {
        val providerConfig = mockProviderConfig("adyen")
        val preauthorizeRequest = mockAuthorizeRequest(
            transaction = mockTransaction(
                redirectResponse = mockRedirectResponse(),
                operationType = OperationType.AUTHORIZE
            ),
            paymentInstrument = mockTokenizedCard(),
            providerConfig = providerConfig
        )
        val preauthorizeResponse = service.preauthorize(preauthorizeRequest)
        Assertions.assertNotNull(preauthorizeResponse)
        Assertions.assertEquals(OperationStatus.OK, preauthorizeResponse.status)
        Assertions.assertEquals(OperationType.AUTHORIZE, preauthorizeResponse.type)
        Assertions.assertNotNull(preauthorizeResponse.reference)
        //cancel
        val cancelRequest = mockCancelRequest(
            preauthorizeResponse.reference!!,
            providerConfig = providerConfig
        )
        val cancelResponse = service.cancel(cancelRequest)
        Assertions.assertNotNull(cancelRequest)
        Assertions.assertNotNull(cancelResponse)
        Assertions.assertEquals(OperationStatus.PENDING, cancelResponse.status)
        Assertions.assertEquals(OperationType.CANCEL, cancelResponse.type)
        Assertions.assertNotNull(cancelResponse.reference)
    }

    @Test
    fun `cancel for apple pay`() {
        val providerConfig = mockProviderConfig("adyen_applepay")
        val cancelRequest = mockCancelRequest(
            "invalid_reference",
            providerConfig = providerConfig
        )
        //Will not return OK response as reference is not correct since authorize is not possible to be carried out automatically with a valid token yet
        val response = service.cancel(cancelRequest)
        Assertions.assertNotNull(response)
        Assertions.assertEquals(OperationStatus.UNKNOWN_ERROR, response.status)
        Assertions.assertEquals(OperationType.CANCEL, response.type)
        Assertions.assertNull(response.reference)
    }

    @Test
    fun `cancel for google pay`() {
        val providerConfig = mockProviderConfig("adyen_googlepay")
        val cancelRequest = mockCancelRequest(
            "invalid_reference",
            providerConfig = providerConfig
        )
        //Will not return OK response as reference is not correct since authorize is not possible to be carried out automatically with a valid token yet
        val response = service.cancel(cancelRequest)
        Assertions.assertNotNull(response)
        Assertions.assertEquals(OperationStatus.UNKNOWN_ERROR, response.status)
        Assertions.assertEquals(OperationType.CANCEL, response.type)
        Assertions.assertNull(response.reference)
    }

    @Test
    fun `preauthorize capture refund  - positive for credit card`() {
        val providerConfig = mockProviderConfig("adyen")
        val preauthorizeRequest = mockAuthorizeRequest(
            transaction = mockTransaction(
                redirectResponse = mockRedirectResponse(),
                operationType = OperationType.PREAUTHORIZE
            ),
            paymentInstrument = mockTokenizedCard(),
            providerConfig = providerConfig
        )
        val preauthorizeResponse = service.preauthorize(preauthorizeRequest)
        Assertions.assertNotNull(preauthorizeResponse)
        Assertions.assertEquals(OperationStatus.OK, preauthorizeResponse.status)
        Assertions.assertEquals(OperationType.AUTHORIZE, preauthorizeResponse.type)
        Assertions.assertNotNull(preauthorizeResponse.reference)
        //capture
        var captureRequest: CaptureRequest? = null
        var captureResponse: OperationResponse? = null
        preauthorizeResponse.reference?.let { reference ->
            captureRequest = mockCaptureRequest(
                reference,
                preauthorizeRequest.transaction.amount,
                providerConfig = providerConfig
            )
        }
        captureRequest?.let { request ->
            captureResponse = service.capture(request)
        }
        Assertions.assertNotNull(captureRequest)
        Assertions.assertNotNull(captureResponse)
        captureResponse?.let { response ->
            Assertions.assertEquals(OperationType.CAPTURE, response.type)
            Assertions.assertEquals(OperationStatus.PENDING, response.status)
            Assertions.assertNotNull(response.reference)
        }
        //refund
        var refundRequest: RefundRequest? = null
        var refundResponse: OperationResponse? = null
        preauthorizeResponse.reference?.let { reference ->
            refundRequest = mockRefundRequest(
                reference,
                preauthorizeRequest.transaction.amount,
                providerConfig = providerConfig
            )
        }
        refundRequest?.let { request ->
            refundResponse = service.refund(request)
        }
        refundResponse?.let { response ->
            Assertions.assertEquals(OperationType.REFUND, response.type)
            Assertions.assertEquals(OperationStatus.PENDING, response.status)
            Assertions.assertNotNull(response.reference)
        }

    }

    @Test
    fun `authorize capture refund  - positive for credit card`() {
        val providerConfig = mockProviderConfig("adyen")
        val preauthorizeRequest = mockAuthorizeRequest(
            transaction = mockTransaction(
                redirectResponse = mockRedirectResponse(),
                operationType = OperationType.AUTHORIZE
            ),
            paymentInstrument = mockTokenizedCard(),
            providerConfig = providerConfig
        )
        val preauthorizeResponse = service.preauthorize(preauthorizeRequest)
        Assertions.assertNotNull(preauthorizeResponse)
        Assertions.assertEquals(OperationStatus.OK, preauthorizeResponse.status)
        Assertions.assertEquals(OperationType.AUTHORIZE, preauthorizeResponse.type)
        Assertions.assertNotNull(preauthorizeResponse.reference)
        //capture
        val captureRequest = mockCaptureRequest(
            preauthorizeResponse.reference!!,
            preauthorizeRequest.transaction.amount,
            providerConfig = providerConfig
        )
        val captureResponse = service.capture(captureRequest)
        Assertions.assertNotNull(captureRequest)
        Assertions.assertNotNull(captureResponse)
        Assertions.assertEquals(OperationType.CAPTURE, captureResponse.type)
        Assertions.assertEquals(OperationStatus.PENDING, captureResponse.status)
        Assertions.assertNotNull(captureResponse.reference)
        //refund
        var refundResponse: OperationResponse? = null
        val refundRequest = mockRefundRequest(
            preauthorizeResponse.reference!!,
            preauthorizeRequest.transaction.amount,
            providerConfig = providerConfig
        )
        refundResponse = service.refund(refundRequest)
        Assertions.assertEquals(OperationType.REFUND, refundResponse.type)
        Assertions.assertEquals(OperationStatus.PENDING, refundResponse.status)
        Assertions.assertNotNull(refundResponse.reference)


    }

    @Test
    fun `capture for apple pay`() {
        val providerConfig = mockProviderConfig("adyen_applepay")
        val captureRequest = mockCaptureRequest(
            "invalid_reference",
            amount = mockMoney(amount = 10),
            providerConfig = providerConfig
        )
        //Will not return OK response as reference is not correct since authorize is not possible to be carried out automatically with a valid token yet
        val response = service.capture(captureRequest)
        Assertions.assertNotNull(response)
        Assertions.assertEquals(OperationStatus.UNKNOWN_ERROR, response.status)
        Assertions.assertEquals(OperationType.CAPTURE, response.type)
        Assertions.assertNull(response.reference)
    }

    @Test
    fun `capture for google pay`() {
        val providerConfig = mockProviderConfig("adyen_googlepay")
        val captureRequest = mockCaptureRequest(
            "invalid_reference",
            amount = mockMoney(amount = 10),
            providerConfig = providerConfig
        )
        //Will not return OK response as reference is not correct since authorize is not possible to be carried out automatically with a valid token yet
        val response = service.capture(captureRequest)
        Assertions.assertNotNull(response)
        Assertions.assertEquals(OperationStatus.UNKNOWN_ERROR, response.status)
        Assertions.assertEquals(OperationType.CAPTURE, response.type)
        Assertions.assertNull(response.reference)
    }

    @Test
    fun `credit - not supported for credit card`() {
        val providerConfig = mockProviderConfig("adyen")
        val request = mockCreditRequestForCard(providerConfig = providerConfig)
        val response = service.credit(request)
        Assertions.assertNotNull(response)
        Assertions.assertEquals(OperationStatus.OPERATION_NOT_ALLOWED, response.status)
        Assertions.assertEquals(OperationType.UNKNOWN, response.type)
    }

    @Test
    fun `credit - not supported for apple pay`() {
        val providerConfig = mockProviderConfig("adyen_applepay")
        val request = mockCreditRequestForOneTimePayment(
            providerConfig = providerConfig
        )
        val response = service.credit(request)
        Assertions.assertNotNull(response)
        Assertions.assertEquals(OperationStatus.OPERATION_NOT_ALLOWED, response.status)
        Assertions.assertEquals(OperationType.UNKNOWN, response.type)
    }

    @Test
    fun `credit - not supported for google pay`() {
        val providerConfig = mockProviderConfig("adyen_googlepay")
        val request =
            mockCreditRequestForOneTimePayment(
                providerConfig = providerConfig
            )
        val response = service.credit(request)
        Assertions.assertNotNull(response)
        Assertions.assertEquals(OperationStatus.OPERATION_NOT_ALLOWED, response.status)
        Assertions.assertEquals(OperationType.UNKNOWN, response.type)
    }

    @Test
    fun `authorize for unsupported currency for credit card`() {
        val providerConfig = mockProviderConfig("adyen")
        val authorizeRequest = mockAuthorizeRequest(
            transaction = mockTransaction(
                redirectResponse = mockRedirectResponse(),
                amount = 51,
                currency = Currency.ZZZ,
                operationType = OperationType.AUTHORIZE
            ),
            paymentInstrument = mockTokenizedCard(),
            providerConfig = providerConfig
        )
        val response = service.authorize(authorizeRequest)
        Assertions.assertNotNull(response)
        Assertions.assertTrue(response.rawResponse!!.contains("Unsupported currency specified"))
        Assertions.assertEquals(OperationStatus.UNKNOWN_ERROR, response.status)
        Assertions.assertEquals(OperationType.AUTHORIZE, response.type)
        Assertions.assertNull(response.reference)
    }

    @Test
    fun `authorize for unsupported currency for apple pay`() {
        val providerConfig = mockProviderConfig("adyen_applepay")
        val authorizeRequest = mockAuthorizeRequest(
            transaction = mockTransaction(
                redirectResponse = mockRedirectResponse(),
                amount = 51,
                currency = Currency.ZZZ,
                operationType = OperationType.AUTHORIZE
            ),
            paymentInstrument = mockOneTimePayment(),
            providerConfig = providerConfig
        )
        val response = service.authorize(authorizeRequest)
        Assertions.assertNotNull(response)
        Assertions.assertTrue(response.rawResponse!!.contains("Unsupported currency specified"))
        Assertions.assertEquals(OperationStatus.UNKNOWN_ERROR, response.status)
        Assertions.assertEquals(OperationType.AUTHORIZE, response.type)
        Assertions.assertNull(response.reference)
    }

    @Test
    fun `authorize for unsupported currency for google pay`() {
        val providerConfig = mockProviderConfig("adyen_googlepay")
        val authorizeRequest = mockAuthorizeRequest(
            transaction = mockTransaction(
                redirectResponse = mockRedirectResponse(),
                amount = 51,
                currency = Currency.ZZZ,
                operationType = OperationType.AUTHORIZE
            ),
            paymentInstrument = mockOneTimePayment(),
            providerConfig = providerConfig
        )
        val response = service.authorize(authorizeRequest)
        Assertions.assertNotNull(response)
        Assertions.assertTrue(response.rawResponse!!.contains("Unsupported currency specified"))
        Assertions.assertEquals(OperationStatus.UNKNOWN_ERROR, response.status)
        Assertions.assertEquals(OperationType.AUTHORIZE, response.type)
        Assertions.assertNull(response.reference)
    }

    //TODO:: Tests like below related to incorrect credit card details should be implemented once credit card is ready in AdyenGateway.authorizeOrPreauthorize
//    @Test
//    fun `authorize - payment instrument for incorrect credit card`() {
//        val providerConfig = mockProviderConfig("adyen")
//        val request = mockAuthorizeRequestForCard(
//            transaction = mockTransaction(
//                redirectResponse = mockRedirectResponse(),
//                operationType = OperationType.AUTHORIZE
//            ),
//            paymentInstrument = mockTockenizedCardWithIncorrectExpiryMonth(),
//            providerConfig = providerConfig
//        )
//        val response = service.authorize(request)
//
//        Assertions.assertNotNull(response)
//        println(response)
//        Assertions.assertEquals(OperationStatus.DO_NOT_HONOR, response.status)
//        Assertions.assertEquals(OperationType.AUTHORIZE, response.type)
//        Assertions.assertNotNull(response.reference)
//    }
}
