package com.deliveryhero.alfred.connector.sdk.operation.request

import com.deliveryhero.alfred.connector.sdk.operation.OperationType
import com.deliveryhero.alfred.connector.sdk.operation.request.common.ClientContext
import com.deliveryhero.alfred.connector.sdk.operation.request.common.Customer
import com.deliveryhero.alfred.connector.sdk.operation.request.common.Order
import com.deliveryhero.alfred.connector.sdk.operation.request.common.Transaction
import com.deliveryhero.alfred.connector.sdk.operation.request.common.Vendor
import com.deliveryhero.alfred.connector.sdk.operation.request.config.ProviderConfig
import com.deliveryhero.alfred.connector.sdk.operation.request.paymentinstrument.PaymentInstrument

data class AuthorizeRequest(
    var customer: Customer,
    var order: Order,
    var transaction: Transaction,
    var paymentInstrument: PaymentInstrument? = null,
    var vendor: Vendor? = null,
    var shouldStorePaymentInstrument: Boolean = false,
    val additionalData: Map<String,String>? = null,
    override var clientContext: ClientContext? = null,
    override var providerConfig: ProviderConfig
) : OperationRequest(OperationType.AUTHORIZE, providerConfig)
