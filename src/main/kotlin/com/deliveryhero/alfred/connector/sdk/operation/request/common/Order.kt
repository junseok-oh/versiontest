package com.deliveryhero.alfred.connector.sdk.operation.request.common

import java.io.Serializable

data class Order(
    val id: String? = null,
    val description: String? = null,
    val brandName: String? = null,
    val deliveryAddress: Address? = null,
    val shippingType: ShippingType? = null
) : Serializable
