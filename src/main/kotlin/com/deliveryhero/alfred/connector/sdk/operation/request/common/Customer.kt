package com.deliveryhero.alfred.connector.sdk.operation.request.common

import java.io.Serializable

data class Customer(
    val id: String? = null,
    var name: String? = null,
    var lastName: String? = null,
    val email: String? = null,
    val phone: Phone? = null,
    val birthDate: String? = null,
    val language: String? = null,
    val country: Country? = null
) : Serializable {
    var fullName: String
        get() = "$name $lastName".trim()
        set(value) {
            this.name = value
            this.lastName = null
        }
    val locale: String?
        get() = if (language != null && country != null) "${language.toLowerCase()}-${country.name.toUpperCase()}" else null
}
