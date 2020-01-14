package com.deliveryhero.alfred.connector.sdk.operation.request.paymentinstrument

data class RawCard(
    override val bin: String?,
    override val lastDigits: String?,
    override val expiryMonth: String?,
    override val expiryYear: String?,
    override val holderName: String?,
    override val displayValue: String = lastDigits ?: "<ENCRYPTED>",
    val number: String,
    val securityCode: String? = null
) : Card(bin, lastDigits, expiryMonth, expiryYear, holderName,
    PaymentInstrumentType.RAW_CARD, displayValue)
