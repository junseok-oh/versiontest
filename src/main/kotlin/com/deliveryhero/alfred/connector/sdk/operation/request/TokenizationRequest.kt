package com.deliveryhero.alfred.connector.sdk.operation.request

import com.deliveryhero.alfred.connector.sdk.operation.OperationType
import com.deliveryhero.alfred.connector.sdk.operation.request.config.ProviderConfig
import com.deliveryhero.alfred.connector.sdk.operation.response.redirect.RedirectRequest

data class TokenizationRequest(
    var approvalToken: String?,
    override var providerConfig: ProviderConfig
) : OperationRequest(OperationType.TOKENIZE, providerConfig)
