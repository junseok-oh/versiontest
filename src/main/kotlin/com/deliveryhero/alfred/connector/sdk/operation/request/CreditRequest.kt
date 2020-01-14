package com.deliveryhero.alfred.connector.sdk.operation.request

import com.deliveryhero.alfred.connector.sdk.operation.OperationType
import com.deliveryhero.alfred.connector.sdk.operation.request.common.Money
import com.deliveryhero.alfred.connector.sdk.operation.request.config.ProviderConfig
import com.deliveryhero.alfred.connector.sdk.operation.request.paymentinstrument.PaymentInstrument

data class CreditRequest(
    val paymentInstrument: PaymentInstrument,
    val amount: Money? = null,
    override var providerConfig: ProviderConfig
) : OperationRequest(OperationType.CREDIT, providerConfig)
