package com.deliveryhero.alfred.connector.sdk.operation.request.common

import java.io.Serializable

data class ClientContext(
    val ipAddress: String? = null,
    var userAgent: String? = null,
    val acceptHeader: String? = null
) : Serializable
