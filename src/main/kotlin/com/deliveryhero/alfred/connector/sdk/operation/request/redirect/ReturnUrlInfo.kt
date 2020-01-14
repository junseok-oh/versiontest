package com.deliveryhero.alfred.connector.sdk.operation.request.redirect

import java.io.Serializable

data class ReturnUrlInfo(
    var success: String,
    var error: String? = null,
    var cancel: String? = null
) : Serializable
