package com.deliveryhero.alfred.connector.sdk.operation.request.paymentinstrument

abstract class Card(
    open val bin: String?,
    open val lastDigits: String?,
    open val expiryMonth: String?,
    open val expiryYear: String?,
    open val holderName: String?,
    type: PaymentInstrumentType,
    displayValue: String
) : PaymentInstrument(type, displayValue)
