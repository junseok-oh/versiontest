package com.deliveryhero.alfred.connector.sdk.operation.request.paymentinstrument

data class EncryptedCard(
    override val bin: String?,
    override val lastDigits: String?,
    override val expiryMonth: String?,
    override val expiryYear: String?,
    override val holderName: String?,
    override val displayValue: String = lastDigits ?: "<ENCRYPTED>",
    val encryptedData: String
) : Card(bin, lastDigits, expiryMonth, expiryYear, holderName,
    PaymentInstrumentType.ENCRYPTED_CARD, displayValue)
