package com.deliveryhero.alfred.connector.sdk

import com.deliveryhero.alfred.connector.sdk.exception.ConnectorException
import com.deliveryhero.alfred.connector.sdk.operation.OperationType
import com.deliveryhero.alfred.connector.sdk.operation.request.AuthorizeRequest
import com.deliveryhero.alfred.connector.sdk.operation.request.CancelRequest
import com.deliveryhero.alfred.connector.sdk.operation.request.CaptureRequest
import com.deliveryhero.alfred.connector.sdk.operation.request.CreditRequest
import com.deliveryhero.alfred.connector.sdk.operation.request.OperationRequest
import com.deliveryhero.alfred.connector.sdk.operation.request.RefundRequest
import com.deliveryhero.alfred.connector.sdk.operation.request.common.Customer
import com.deliveryhero.alfred.connector.sdk.operation.request.common.Order
import com.deliveryhero.alfred.connector.sdk.operation.request.config.ProviderConfig
import com.deliveryhero.alfred.connector.sdk.operation.response.OperationResponse
import com.deliveryhero.alfred.connector.sdk.util.SerializationUtils
import org.apache.commons.lang3.exception.ExceptionUtils
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.slf4j.MDC


abstract class ConnectorPaymentGateway {

    companion object {
        val logger: Logger = LoggerFactory.getLogger(ConnectorPaymentGateway::class.java)
    }

    abstract fun getIdentifierName(): String?

    abstract fun getIdentifierVersion(): String?

    abstract fun getPaymentMethod(): String

    @Throws(ConnectorException::class)
    fun isApplicableFor(config: ProviderConfig): Boolean {
        return isApplicableForConnector(config)
    }

    abstract fun isApplicableForConnector(config: ProviderConfig): Boolean

    /**
     * Encapsulate common logging and exception handling logic
     */
    private inline fun <reified T: OperationRequest> invokeConnectorOperation(request: T, operationType: OperationType, operation: (T) -> OperationResponse): OperationResponse {
        try {
            initializeMDC(request, operationType)

            logger.debug("${getIdentifierName()} invoked an $operationType operation.")

            val response = operation.invoke(request)

            populateMDCWithResponse(response)

            logger.debug("${getIdentifierName()} returned ${response.status} status when invoking an $operationType operation.")

            return response
        } catch (exception: Exception) {
            populateMDCWithException(exception)

            logger.error("${getIdentifierName()} incurred in an '${exception.localizedMessage}' exception when invoking an $operationType operation.")

            throw ConnectorException("${getIdentifierName()} incurred in exception when invoking an $operationType operation.", exception)
        }
    }

    @Throws(ConnectorException::class)
    fun preauthorize(request: AuthorizeRequest): OperationResponse {
        return invokeConnectorOperation(request, OperationType.PREAUTHORIZE, { connectorPreauthorize(request) })
    }

    @Throws(ConnectorException::class)
    fun authorize(request: AuthorizeRequest): OperationResponse {
        return invokeConnectorOperation(request, OperationType.AUTHORIZE, { connectorAuthorize(request) })
    }

    @Throws(ConnectorException::class)
    fun cancel(request: CancelRequest): OperationResponse {
        return invokeConnectorOperation(request, OperationType.AUTHORIZE, { connectorCancel(request) })
    }

    @Throws(ConnectorException::class)
    fun capture(request: CaptureRequest): OperationResponse {
        return invokeConnectorOperation(request, OperationType.CAPTURE, { connectorCapture(request) })
    }

    @Throws(ConnectorException::class)
    fun refund(request: RefundRequest): OperationResponse {
        return invokeConnectorOperation(request, OperationType.REFUND, { connectorRefund(request) })
    }

    @Throws(ConnectorException::class)
    fun credit(request: CreditRequest): OperationResponse {
        return invokeConnectorOperation(request, OperationType.CREDIT, { connectorCredit(request) })
    }

    // Psp specific logic and request/response mapping is implemented in the psp specific connector

    protected abstract fun connectorPreauthorize(request: AuthorizeRequest): OperationResponse

    protected abstract fun connectorAuthorize(request: AuthorizeRequest): OperationResponse

    protected abstract fun connectorCancel(request: CancelRequest): OperationResponse

    protected abstract fun connectorCapture(request: CaptureRequest): OperationResponse

    protected abstract fun connectorRefund(request: RefundRequest): OperationResponse

    protected abstract fun connectorCredit(request: CreditRequest): OperationResponse

    open fun extractToken(additionalData: Map<String, String>): String = additionalData["TOKEN"] ?: ""

    private inline fun <reified T: OperationRequest> initializeMDC(request: T, operationType: OperationType) {
        try {
            val clone = SerializationUtils.deepCloneWithRits(request)
            clone?.providerConfig?.config?.clear()
            clone?.request = null
            clone?.response = null
            clone?.clientContext = null

            when (clone) {
                is AuthorizeRequest -> {
                    clone.customer = Customer(
                        id = clone.customer.id,
                        email = clone.customer.email
                    )
                    clone.order = Order(
                        id = clone.order.id,
                        brandName = clone.order.brandName,
                        shippingType = clone.order.shippingType
                    )
                    clone.transaction = clone.transaction.copy(billingAddress = null)
                }
            }

            mergeMapWithMdc(mutableMapOf(
                "connector" to mutableMapOf(
                    "class" to this.javaClass.canonicalName,
                    "name" to getIdentifierName(),
                    "method" to getPaymentMethod(),
                    "version" to getIdentifierVersion(),
                    "operation" to mutableMapOf(
                        "type" to operationType.toString()
                    )
                ),
                "phase" to "invocation",
                "request" to clone
            ))
        } catch (exception : Exception) {
            logger.error("Error when initializing the MDC context", exception)
        }
    }

    private fun populateMDCWithResponse(response: OperationResponse) {
        try {
            val clone = SerializationUtils.deepCloneWithRits(response)
            clone?.request = null
            clone?.response = null
            clone?.paymentInstrumentToken = if (clone?.paymentInstrumentToken == null) null else "***"

            mergeMapWithMdc(mutableMapOf(
                "phase" to "response",
                "response" to clone
            ))
        } catch (exception: Exception) {
            logger.error("Error when populating the MDC with the response", exception)
        }
    }

    private fun populateMDCWithException(throwable: Throwable) {
        try {
            val rootCause = ExceptionUtils.getRootCause(throwable)
            val firstStackTrace = rootCause.stackTrace.getOrNull(0)
            val localizedMessage : String = rootCause.localizedMessage
            var localizedMessageMap : MutableMap<String, Any>? = null

            try {
                localizedMessageMap = SerializationUtils.toMutableMap(localizedMessage)
            } catch (exception: Exception) {
            }

            mergeMapWithMdc(mutableMapOf(
                "phase" to "exception",
                "exception" to mutableMapOf(
                    "root" to mutableMapOf(
                        "localizedMessage" to (localizedMessageMap ?: localizedMessage),
                        "className" to firstStackTrace?.className,
                        "methodName" to firstStackTrace?.methodName,
                        "fileName" to firstStackTrace?.fileName,
                        "lineNumber" to firstStackTrace?.lineNumber
                    )
                )
            ))
        } catch (exception: Exception) {
            logger.error("Error when populating the MDC with an exception.", exception)
        }
    }

    private fun mergeMapWithMdc(map: MutableMap<String, Any?>) {
        val mdc = MDC.getCopyOfContextMap()

        mdc.putAll(SerializationUtils.toFlatMdcMap(map))

        MDC.setContextMap(mdc)
    }
}
