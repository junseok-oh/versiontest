package com.deliveryhero.alfred.connector.sdk.operation.request

import com.deliveryhero.alfred.connector.sdk.operation.OperationType
import com.deliveryhero.alfred.connector.sdk.operation.request.common.ClientContext
import com.deliveryhero.alfred.connector.sdk.operation.request.config.ProviderConfig

data class CancelRequest(
    override var operationId: String,
    override var originalOperationId: String,
    override var clientContext: ClientContext? = null,
    override var providerConfig: ProviderConfig
) : ModificationRequest(operationId, originalOperationId,
    OperationType.CANCEL, providerConfig)
