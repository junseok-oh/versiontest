package com.deliveryhero.alfred.connector.sdk.operation.request.common

import java.io.Serializable
import java.math.BigDecimal
import java.math.RoundingMode

data class Money(
    val currency: Currency,
    val value: BigDecimal
) : Serializable {
    fun toStringWithPrecision(): String {
        return toStringWithPrecision(null)
    }
    fun toStringWithPrecision(customPrecision: Int?): String {
        val precision = customPrecision ?: this.currency.precision ?: 0
        return this.value.setScale(precision, RoundingMode.HALF_UP).toString()
    }
}
