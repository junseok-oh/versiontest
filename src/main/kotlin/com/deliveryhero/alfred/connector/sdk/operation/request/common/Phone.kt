package com.deliveryhero.alfred.connector.sdk.operation.request.common

import java.io.Serializable

data class Phone(
    val type: PhoneType? = null,
    val number: String
) : Serializable

