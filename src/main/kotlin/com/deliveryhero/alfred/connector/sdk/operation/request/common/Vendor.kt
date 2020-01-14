package com.deliveryhero.alfred.connector.sdk.operation.request.common

import java.io.Serializable

data class Vendor(
    val id: String? = null,
    val name: String? = null,
    val address: Address? = null
) : Serializable
