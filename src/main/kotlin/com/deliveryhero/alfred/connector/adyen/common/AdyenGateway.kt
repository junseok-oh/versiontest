package com.deliveryhero.alfred.connector.adyen.common

import com.deliveryhero.alfred.connector.adyen.common.dom.AdyenConfigData
import com.deliveryhero.alfred.connector.adyen.common.util.AdyenRequestBuilder
import com.deliveryhero.alfred.connector.adyen.common.util.AdyenRequestValidator
import com.deliveryhero.alfred.connector.adyen.common.util.AdyenResponseParser
import com.deliveryhero.alfred.connector.sdk.ConnectorPaymentGateway
import com.deliveryhero.alfred.connector.sdk.exception.ConnectorException
import com.deliveryhero.alfred.connector.sdk.operation.OperationType
import com.deliveryhero.alfred.connector.sdk.operation.request.*
import com.deliveryhero.alfred.connector.sdk.operation.request.config.ProviderConfig
import com.deliveryhero.alfred.connector.sdk.operation.response.OperationResponse

open class AdyenGateway() : ConnectorPaymentGateway() {

    protected open fun getClient(config: ProviderConfig): AdyenClient {
        return AdyenClient(
            when (config) {
                is AdyenConfigData -> config
                is ProviderConfig -> AdyenConfigData(
                    identifier = config.identifier,
                    environment = config.environment,
                    config = config.config
                )
                else -> throw ConnectorException("This type of ProviderConfig is not compatible: ${config.javaClass.name}")
            }
        )
    }

    override fun getIdentifierName(): String? {
        return AdyenGateway::class.simpleName
    }

    override fun getIdentifierVersion(): String? {
        return "1.0"
    }

    override fun getPaymentMethod(): String {
        return "adyen"
    }

    override fun isApplicableForConnector(config: ProviderConfig): Boolean {
        //TODO implement conditions on config
        return true
    }

    override fun connectorPreauthorize(request: AuthorizeRequest): OperationResponse {
        return request.apply {
            AdyenRequestValidator.validateAuthorisationRequest(request)
        }.run {
            authorizeOrPreauthorize(this)
        }
    }

    override fun connectorAuthorize(request: AuthorizeRequest): OperationResponse {
        return request.apply {
            AdyenRequestValidator.validateAuthorisationRequest(request)
        }.run {
            authorizeOrPreauthorize(this)
        }
    }

    private fun authorizeOrPreauthorize(request: AuthorizeRequest): OperationResponse {
        //TODO:: Add logging like PayPal
        if (request.transaction.type !in setOf(OperationType.PREAUTHORIZE, OperationType.AUTHORIZE))
            throw ConnectorException("Only PREAUTHORIZE & AUTHORIZE operations are expected")

        if (request.paymentInstrument == null) { //TODO:: change this part for cards??
            val paymentRequest = AdyenRequestBuilder.prepareAuthorizeRequests(request)
            return try {
                val paymentResult = getClient(request.providerConfig).authorize(paymentRequest)
                AdyenResponseParser.transformAuthorizeResponse(request, paymentRequest, paymentResult, null)
            } catch (ex: Exception) {
                AdyenResponseParser.transformAuthorizeResponse(request, paymentRequest, null, ex)
            }
        } else {
            val paymentsRequest = AdyenRequestBuilder.prepareAuthorizeRequests(request)
            //TODO:: This is only for testing and should be replaced when Credit Card mapping is implemented
            if (request.providerConfig.identifier.equals("adyen"))
                paymentsRequest.addCardData("4111111111111111", "03", "2030", "737", "Mathias Fonseca")
            return try {
                val paymentsResponse = getClient(request.providerConfig).authorize(paymentsRequest)
                AdyenResponseParser.transformAuthorizeResponse(request, paymentsRequest, paymentsResponse, null)
            } catch (ex: Exception) {
                AdyenResponseParser.transformAuthorizeResponse(request, paymentsRequest, null, ex)
            }
        }
    }

    override fun connectorCancel(request: CancelRequest): OperationResponse {
        request.apply {
            AdyenRequestValidator.validateCancelRequest(request)
        }
        val cancelRequest = AdyenRequestBuilder.prepareCancelRequest(request)
        return try {
            val modificationResult =
                getClient(request.providerConfig).cancel(cancelRequest)
            AdyenResponseParser.transformCancelResponse(request, cancelRequest, modificationResult, null)
        } catch (ex: Exception) {
            AdyenResponseParser.transformCancelResponse(request, cancelRequest, null, ex)
        }
    }

    override fun connectorCapture(request: CaptureRequest): OperationResponse {
        request.apply {
            AdyenRequestValidator.validateCaptureRequest(request)
        }
        val pspCaptureRequest = AdyenRequestBuilder.prepareCaptureRequest(request)
        return try {
            val modificationResult =
                getClient(request.providerConfig).capture(pspCaptureRequest)
            AdyenResponseParser.transformCaptureResponse(request, pspCaptureRequest, modificationResult, null)
        } catch (ex: Exception) {
            AdyenResponseParser.transformCaptureResponse(request, pspCaptureRequest, null, ex)
        }
    }

    override fun connectorCredit(request: CreditRequest): OperationResponse {
        return OperationResponse.buildOperationNotAllowed()
    }

    override fun connectorRefund(request: RefundRequest): OperationResponse {
        request.apply {
            AdyenRequestValidator.validateRefundRequest(request)
        }
        val cancelOrRefundRequest = AdyenRequestBuilder.prepareRefundRequest(request)
        return try {
            val modificationResult =
                getClient(request.providerConfig).refund(cancelOrRefundRequest)
            AdyenResponseParser.transformRefundResponse(request, cancelOrRefundRequest, modificationResult, null)
        } catch (ex: Exception) {
            AdyenResponseParser.transformRefundResponse(request, cancelOrRefundRequest, null, ex)
        }
    }
}
