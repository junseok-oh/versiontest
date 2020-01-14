package com.deliveryhero.alfred.connector.sdk.operation.request

import com.deliveryhero.alfred.connector.sdk.operation.Operation
import com.deliveryhero.alfred.connector.sdk.operation.OperationType
import com.deliveryhero.alfred.connector.sdk.operation.request.common.ClientContext
import com.deliveryhero.alfred.connector.sdk.operation.request.config.ProviderConfig

abstract class OperationRequest(
    override var type: OperationType,
    open var providerConfig: ProviderConfig,
    open var clientContext: ClientContext? = null,
    val rawRequest: String? = null
) : Operation(type)
