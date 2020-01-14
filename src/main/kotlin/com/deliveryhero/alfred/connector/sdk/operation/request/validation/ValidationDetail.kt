package com.deliveryhero.alfred.connector.sdk.operation.request.validation

import java.io.Serializable

data class ValidationDetail(
    val field: String?,
    val errorCode: String?,
    val error: String
) : Serializable
