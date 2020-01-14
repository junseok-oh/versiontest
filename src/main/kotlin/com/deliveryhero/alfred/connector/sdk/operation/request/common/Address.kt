package com.deliveryhero.alfred.connector.sdk.operation.request.common

import java.io.Serializable

data class Address(
    val street: String?,
    val streetType: String? = null,
    val complement: String? = null,
    val doorNumber: String? = null,
    val area: String? = null,
    val city: String? = null,
    val state: String? = null,
    val country: Country? = null,
    val postalCode: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null
) : Serializable
