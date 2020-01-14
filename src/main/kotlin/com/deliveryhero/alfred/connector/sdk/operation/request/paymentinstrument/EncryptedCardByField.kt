package com.deliveryhero.alfred.connector.sdk.operation.request.paymentinstrument

data class EncryptedCardByField(
    override val bin: String?,
    override val lastDigits: String?,
    override val expiryMonth: String?,
    override val expiryYear: String?,
    override val holderName: String?,
    override val displayValue: String = lastDigits ?: "<ENCRYPTED>",
    val encryptedNumber: String,
    val encryptedExpiryMonth: String,
    val encryptedExpiryYear: String,
    val encryptedSecurityCode: String,
    val encryptedHolderName: String
) : Card(bin, lastDigits, expiryMonth, expiryYear, holderName,
    PaymentInstrumentType.ENCRYPTED_CARD_BY_FIELD, displayValue)
