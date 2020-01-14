package com.deliveryhero.alfred.connector.adyen.common.dom

import com.adyen.enums.Environment as AdyenApiEnvironment
import com.deliveryhero.alfred.connector.sdk.operation.request.config.Environment
import com.deliveryhero.alfred.connector.sdk.operation.request.config.ProviderConfig

open class AdyenConfigData(identifier: String, environment: Environment, config: MutableMap<String, String>) :
    ProviderConfig(identifier, environment, config) {

    companion object {
        const val API_KEY = "apiKey"
        const val MERCHANT_ACCOUNT = "merchantAccount"
        const val SHOULD_SEND_BROWSER_INFO = "shouldSendBrowserInfo"
    }

    fun apiKey(): String? {
        return config[API_KEY]
    }

    fun merchantAccount(): String? {
        return config[MERCHANT_ACCOUNT]
    }

    fun shouldSendBrowserInfo(): Boolean {
        return config[SHOULD_SEND_BROWSER_INFO]?.toBoolean() ?: false
    }

    fun setConfigData(key: String, value: String): AdyenConfigData {
        config[key] = value
        return this
    }

    fun getEnvironment() : AdyenApiEnvironment{
       if(this.environment==Environment.LIVE)
            return AdyenApiEnvironment.LIVE
        return AdyenApiEnvironment.TEST
    }

}
