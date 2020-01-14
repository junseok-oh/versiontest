package com.deliveryhero.alfred.connector.sdk.operation.request.paymentinstrument

import java.io.Serializable

abstract class PaymentInstrument(
    open val type: PaymentInstrumentType,
    open val displayValue: String
) : Serializable
