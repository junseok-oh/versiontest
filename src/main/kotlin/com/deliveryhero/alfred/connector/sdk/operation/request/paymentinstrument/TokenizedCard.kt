package com.deliveryhero.alfred.connector.sdk.operation.request.paymentinstrument

data class TokenizedCard(
    override val bin: String?,
    override val lastDigits: String,
    override val expiryMonth: String?,
    override val expiryYear: String?,
    override val holderName: String?,
    override val displayValue: String = lastDigits,
    val token: String,
    val securityCode: String? = null
) : Card(bin, lastDigits, expiryMonth, expiryYear, holderName,
    PaymentInstrumentType.TOKENIZED_CARD, displayValue)
