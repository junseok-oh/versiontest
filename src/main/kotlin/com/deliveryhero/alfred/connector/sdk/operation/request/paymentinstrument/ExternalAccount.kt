package com.deliveryhero.alfred.connector.sdk.operation.request.paymentinstrument

data class ExternalAccount @JvmOverloads constructor(
    val externalUserId: String? = null,
    val externalAccountId: String? = null,
    val paymentInstrumentToken: String? = null,
    override val displayValue: String
) : PaymentInstrument(PaymentInstrumentType.EXTERNAL_ACCOUNT, displayValue)
