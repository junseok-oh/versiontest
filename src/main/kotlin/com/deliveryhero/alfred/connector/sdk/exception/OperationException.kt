package com.deliveryhero.alfred.connector.sdk.exception

import com.deliveryhero.alfred.connector.sdk.operation.Operation

class OperationException(
    val operation: Operation,
    override val message: String = "[OPERATION_FAILED]",
    override val cause: Throwable?
) : ConnectorException(message, cause)
