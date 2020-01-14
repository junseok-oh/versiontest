package com.deliveryhero.alfred.connector.sdk.operation.request.redirect

import java.io.Serializable

class RedirectResponse(
    val success: Boolean?,
    val params: MutableMap<String, String> = mutableMapOf()
) : Serializable {
    companion object {
        const val MD = "md"
        const val PA_RES = "paRes"
    }

    fun md(): String? = params[MD]
    fun paRes(): String? = params[PA_RES]
}
