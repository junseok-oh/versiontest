package com.deliveryhero.alfred.connector.adyen.common.util

import com.deliveryhero.alfred.connector.adyen.common.dom.AdyenConfigData
import com.deliveryhero.alfred.connector.sdk.exception.RequestValidationException
import com.deliveryhero.alfred.connector.sdk.operation.OperationType
import com.deliveryhero.alfred.connector.sdk.operation.request.AuthorizeRequest
import com.deliveryhero.alfred.connector.sdk.operation.request.CancelRequest
import com.deliveryhero.alfred.connector.sdk.operation.request.CaptureRequest
import com.deliveryhero.alfred.connector.sdk.operation.request.RefundRequest
import com.deliveryhero.alfred.connector.sdk.operation.request.common.Currency
import com.deliveryhero.alfred.connector.sdk.operation.request.common.Customer
import com.deliveryhero.alfred.connector.sdk.operation.request.common.Money
import com.deliveryhero.alfred.connector.sdk.operation.request.common.Order
import com.deliveryhero.alfred.connector.sdk.operation.request.common.Transaction
import com.deliveryhero.alfred.connector.sdk.operation.request.config.Environment
import com.deliveryhero.alfred.connector.sdk.operation.request.config.ProviderConfig
import com.deliveryhero.alfred.connector.sdk.operation.request.validation.ValidationDetail
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail
import java.math.BigDecimal

class AdyenRequestValidatorTest {

    @Test
    fun `Authorise request should throw exception if ReturnUrlInfo is empty or wrong transaction type`() {
        val request = AuthorizeRequest(
            Customer(),
            Order(),
            Transaction(amount = Money(Currency.PLN, BigDecimal.TEN)),
            providerConfig = ProviderConfig("")
        )

        assertTrue(request.transaction.returnUrlInfo == null)
        assertTrue(request.transaction.type == null)

        try {
            AdyenRequestValidator.validateAuthorisationRequest(request)
            fail("Exception not thrown")
        } catch (error: RequestValidationException) {
            assertNotNull(error)
            assertTrue(error.request == request)
            error.validation.errors!!.apply {
                assertTrue {
                    containsAll(
                        listOf(
                            ValidationDetail(
                                "AuthorizeRequest.transaction.returnUrlInfo",
                                null,
                                "The value is null"
                            )
                        )
                    )
                }
            }
        }
    }

    @Test
    fun `should detect missing apiKey and merchantCode in AdyenConfigData`() {
        val configData = AdyenConfigData("id", Environment.TEST, mutableMapOf())

        val validationResults = AdyenRequestValidator.validateConfigData(configData)

        validationResults.contains(
            ValidationDetail("AdyenConfigData.apiKey", null, "The value is null")
        ).apply {
            assertTrue(this)
        }
        validationResults.contains(
            ValidationDetail("AdyenConfigData.merchantCode", null, "The value is null")
        ).apply {
            assertTrue(this)
        }
    }

    @Test
    fun `Refund request should throw exception if amount is null`() {
        val request = RefundRequest(
            providerConfig = ProviderConfig(""),
            operationId = "",
            originalOperationId = ""
        )

        assertTrue(request.clientContext == null)
        assertTrue(request.amount == null)

        try {
            AdyenRequestValidator.validateRefundRequest(request)
            fail("Exception not thrown")
        } catch (error: RequestValidationException) {
            assertNotNull(error)
            assertTrue(error.request == request)
            error.validation.errors!!.apply {
                assertTrue {
                    containsAll(
                        listOf(
                            ValidationDetail(
                                "RefundRequest.amount",
                                null,
                                "amount is null."
                            )
                        )
                    )
                }
            }
        }
    }

    @Test
    fun `Capture request should throw exception if amount is null`() {
        val request = CaptureRequest(
            providerConfig = ProviderConfig(""),
            operationId = "",
            originalOperationId = ""
        )

        assertTrue(request.clientContext == null)
        assertTrue(request.amount == null)

        try {
            AdyenRequestValidator.validateCaptureRequest(request)
            fail("Exception not thrown")
        } catch (error: RequestValidationException) {
            assertNotNull(error)
            assertTrue(error.request == request)
            error.validation.errors!!.apply {
                assertTrue {
                    containsAll(
                        listOf(
                            ValidationDetail(
                                "CaptureRequest.amount",
                                null,
                                "amount is null."
                            )
                        )
                    )
                }
            }
        }
    }
}
