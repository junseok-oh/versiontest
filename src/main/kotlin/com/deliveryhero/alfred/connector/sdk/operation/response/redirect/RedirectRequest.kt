package com.deliveryhero.alfred.connector.sdk.operation.response.redirect

import java.io.Serializable

data class RedirectRequest(
    var redirectUrl: String,
    var redirectMethod: RedirectMethod? = null,
    var redirectType: RedirectType? = null,
    var redirectReason: RedirectReason? = null,
    var requestParameters: MutableMap<String, String> = mutableMapOf()
) : Serializable

