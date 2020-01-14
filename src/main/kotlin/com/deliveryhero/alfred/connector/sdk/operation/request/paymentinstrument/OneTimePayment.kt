package com.deliveryhero.alfred.connector.sdk.operation.request.paymentinstrument

data class OneTimePayment constructor(
    val token : String,
    override val displayValue: String = "OTP"
) : PaymentInstrument(PaymentInstrumentType.ONE_TIME_PAYMENT, displayValue)
