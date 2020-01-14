package com.deliveryhero.alfred.connector.sdk.exception

import com.deliveryhero.alfred.connector.sdk.operation.request.OperationRequest
import com.deliveryhero.alfred.connector.sdk.operation.request.validation.Validation

class RequestValidationException(
    val request: OperationRequest,
    val validation: Validation,
    override val message: String = "[VALIDATION_FAILED]"
) : ConnectorException(message)
