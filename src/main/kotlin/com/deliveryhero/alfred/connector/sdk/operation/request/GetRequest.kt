package com.deliveryhero.alfred.connector.sdk.operation.request

import com.deliveryhero.alfred.connector.sdk.operation.OperationType
import com.deliveryhero.alfred.connector.sdk.operation.request.config.ProviderConfig

data class GetRequest(
    var reference: String,
    override var providerConfig: ProviderConfig
) : OperationRequest(OperationType.GET, providerConfig)
