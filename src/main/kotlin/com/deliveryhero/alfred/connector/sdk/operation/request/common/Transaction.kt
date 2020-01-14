package com.deliveryhero.alfred.connector.sdk.operation.request.common

import com.deliveryhero.alfred.connector.sdk.operation.OperationType
import com.deliveryhero.alfred.connector.sdk.operation.request.redirect.RedirectResponse
import com.deliveryhero.alfred.connector.sdk.operation.request.redirect.ReturnUrlInfo
import java.io.Serializable

data class Transaction(
    val id: String? = null,
    val type: OperationType? = null,
    val amount: Money,
    val billingAddress: Address? = null,
    val softDescriptor: String? = null,
    val returnUrlInfo: ReturnUrlInfo? = null,
    val redirectResponse: RedirectResponse? = null
) : Serializable
