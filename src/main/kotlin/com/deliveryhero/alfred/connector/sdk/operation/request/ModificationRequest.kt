package com.deliveryhero.alfred.connector.sdk.operation.request

import com.deliveryhero.alfred.connector.sdk.operation.OperationType
import com.deliveryhero.alfred.connector.sdk.operation.request.common.ClientContext
import com.deliveryhero.alfred.connector.sdk.operation.request.config.ProviderConfig

abstract class ModificationRequest(
    open var operationId: String,
    open var originalOperationId: String,
    override var type: OperationType,
    override var providerConfig: ProviderConfig,
    override var clientContext: ClientContext? = null
) : OperationRequest(OperationType.AUTHORIZE, providerConfig)
