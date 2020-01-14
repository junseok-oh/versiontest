package com.deliveryhero.alfred.connector.adyen.common

import com.adyen.Client
import com.adyen.model.checkout.PaymentsRequest
import com.adyen.model.checkout.PaymentsResponse
import com.adyen.model.modification.CancelOrRefundRequest
import com.adyen.model.modification.CancelRequest
import com.adyen.model.modification.CaptureRequest
import com.adyen.model.modification.ModificationResult
import com.adyen.model.recurring.DisableRequest
import com.adyen.model.recurring.DisableResult
import com.adyen.model.recurring.RecurringDetailsRequest
import com.adyen.model.recurring.RecurringDetailsResult
import com.adyen.service.Checkout
import com.adyen.service.Modification
import com.adyen.service.Recurring
import com.deliveryhero.alfred.connector.adyen.common.dom.AdyenConfigData

open class AdyenClient(private val config: AdyenConfigData) {

    val client: Client by lazy { getClient(config) }

    private val connectionTimeoutMillis: Int = 0
    private val readTimeoutMillis: Int = 0

    fun getCards(request: RecurringDetailsRequest, config: AdyenConfigData = this.config): RecurringDetailsResult {
        val recurring = Recurring(client)
        return recurring.listRecurringDetails(request)
    }

    fun disableCard(request: DisableRequest, config: AdyenConfigData = this.config): DisableResult {
        val recurring = Recurring(client)
        return recurring.disable(request)
    }

    fun authorize(paymentsRequest: PaymentsRequest, config: AdyenConfigData = this.config): PaymentsResponse {
        val checkout = Checkout(client)
        return checkout.payments(paymentsRequest)
    }

    fun capture(captureRequest: CaptureRequest, config: AdyenConfigData = this.config): ModificationResult {
        val modification = Modification(client)
        return modification.capture(captureRequest)
    }

    fun cancel(cancelRequest: CancelRequest, config: AdyenConfigData = this.config): ModificationResult {
        val modification = Modification(client)
        return modification.cancel(cancelRequest)
    }

    fun refund(
        cancelOrRefundRequest: CancelOrRefundRequest,
        config: AdyenConfigData = this.config
    ): ModificationResult {
        val modification = Modification(client)
        return modification.cancelOrRefund(cancelOrRefundRequest)
    }

    private fun getClient(config: AdyenConfigData): Client {
        val client = Client(config.apiKey(), config.getEnvironment())
        client.setTimeouts(connectionTimeoutMillis, readTimeoutMillis)
        return client
    }

}
