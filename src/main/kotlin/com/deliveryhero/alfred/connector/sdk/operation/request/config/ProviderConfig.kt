package com.deliveryhero.alfred.connector.sdk.operation.request.config

import java.io.Serializable

open class ProviderConfig(
    var identifier: String,
    var environment: Environment = Environment.TEST, // TODO Should we use `live` as a default instead?
    var config: MutableMap<String, String> = mutableMapOf()
) : Serializable {
    fun isProduction(): Boolean {
        return this.environment == Environment.LIVE
    }
}
