package com.deliveryhero.alfred.connector.adyen.common.util

import com.deliveryhero.alfred.connector.adyen.common.dom.AdyenConfigData
import com.deliveryhero.alfred.connector.sdk.exception.RequestValidationException
import com.deliveryhero.alfred.connector.sdk.operation.request.*
import com.deliveryhero.alfred.connector.sdk.operation.request.config.ProviderConfig
import com.deliveryhero.alfred.connector.sdk.operation.request.validation.Validation
import com.deliveryhero.alfred.connector.sdk.operation.request.validation.ValidationDetail
import com.deliveryhero.alfred.connector.sdk.operation.request.validation.ValidationResult
import org.apache.commons.lang3.StringUtils

object AdyenRequestValidator {

    fun validateAuthorisationRequest(request: AuthorizeRequest) {
        val adyenConfigData = request.providerConfig.toAdyenConfigData()
        val validationErrors = validateRequest(adyenConfigData, request)
        throwExceptionIfErrorsFound(validationErrors, request)
    }

    fun validateCancelRequest(request: CancelRequest) {
        val adyenConfigData = request.providerConfig.toAdyenConfigData()
        val validationErrors = validateRequest(adyenConfigData, request)
        throwExceptionIfErrorsFound(validationErrors, request)
    }

    fun validateCaptureRequest(request: CaptureRequest) {
        val adyenConfigData = request.providerConfig.toAdyenConfigData()
        val validationErrors = validateRequest(adyenConfigData, request)
        throwExceptionIfErrorsFound(validationErrors, request)
    }

    fun validateRefundRequest(request: RefundRequest) {
        val adyenConfigData = request.providerConfig.toAdyenConfigData()
        val validationErrors = validateRequest(adyenConfigData, request)
        throwExceptionIfErrorsFound(validationErrors, request)
    }

    private fun validateRequest(
        adyenConfigData: AdyenConfigData,
        request: AuthorizeRequest
    ): List<ValidationDetail> {
        val validationErrors = mutableListOf<ValidationDetail>()
        validateConfigData(adyenConfigData).apply {
            validationErrors.addAll(this)
        }
        if (request.transaction.returnUrlInfo == null) {
            ValidationDetail(
                "AuthorizeRequest.transaction.returnUrlInfo",
                null,
                "The value is null"
            ).apply {
                validationErrors.add(this)
            }
        }
        return validationErrors
    }

    private fun validateRequest(
        adyenConfigData: AdyenConfigData,
        request: CancelRequest
    ): List<ValidationDetail> {
        val validationErrors = mutableListOf<ValidationDetail>()
        validateConfigData(adyenConfigData).apply {
            validationErrors.addAll(this)
        }

        return validationErrors
    }

    private fun validateRequest(
        adyenConfigData: AdyenConfigData,
        request: RefundRequest
    ): List<ValidationDetail> {
        val validationErrors = mutableListOf<ValidationDetail>()
        validateConfigData(adyenConfigData).apply {
            validationErrors.addAll(this)
        }

        if (request.amount == null) {
            ValidationDetail(
                "RefundRequest.amount",
                null,
                "amount is null."
            ).apply {
                validationErrors.add(this)
            }
        }
        return validationErrors
    }

    private fun validateRequest(
        adyenConfigData: AdyenConfigData,
        request: CaptureRequest
    ): List<ValidationDetail> {
        val validationErrors = mutableListOf<ValidationDetail>()
        validateConfigData(adyenConfigData).apply {
            validationErrors.addAll(this)
        }

        if (request.amount == null) {
            ValidationDetail(
                "CaptureRequest.amount",
                null,
                "amount is null."
            ).apply {
                validationErrors.add(this)
            }
        }
        return validationErrors
    }

    internal fun validateConfigData(adyenConfigData: AdyenConfigData): List<ValidationDetail> {
        val validationDetails = mutableListOf<ValidationDetail>()
        if (StringUtils.isBlank(adyenConfigData.apiKey())) {
            ValidationDetail(
                "AdyenConfigData.apiKey",
                null,
                "The value is null"
            ).apply {
                validationDetails.add(this);
            }
        }
        if (StringUtils.isBlank(adyenConfigData.merchantAccount())) {
            ValidationDetail(
                "AdyenConfigData.merchantCode",
                null,
                "The value is null"
            ).apply {
                validationDetails.add(this);
            }
        }
        return validationDetails
    }

    private fun throwExceptionIfErrorsFound(
        validationErrors: List<ValidationDetail>,
        request: OperationRequest
    ) {
        if (validationErrors.isNotEmpty()) {
            val validation = Validation(ValidationResult.ERROR, validationErrors)
            throw RequestValidationException(request, validation)
        }
    }

    internal fun ProviderConfig.toAdyenConfigData(): AdyenConfigData = AdyenConfigData(
        identifier = this.identifier,
        environment = this.environment,
        config = this.config
    )
}
