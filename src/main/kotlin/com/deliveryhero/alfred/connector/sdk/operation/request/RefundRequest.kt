package com.deliveryhero.alfred.connector.sdk.operation.request

import com.deliveryhero.alfred.connector.sdk.operation.OperationType
import com.deliveryhero.alfred.connector.sdk.operation.request.common.ClientContext
import com.deliveryhero.alfred.connector.sdk.operation.request.common.Money
import com.deliveryhero.alfred.connector.sdk.operation.request.config.ProviderConfig

data class RefundRequest(
    override var operationId: String,
    override var originalOperationId: String,
    var amount: Money? = null,
    override var clientContext: ClientContext? = null,
    override var providerConfig: ProviderConfig
) : ModificationRequest(operationId, originalOperationId,
    OperationType.REFUND, providerConfig)
