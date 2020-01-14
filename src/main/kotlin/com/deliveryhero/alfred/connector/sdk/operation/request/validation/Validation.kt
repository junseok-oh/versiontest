package com.deliveryhero.alfred.connector.sdk.operation.request.validation

import java.io.Serializable

data class Validation(
    val result: ValidationResult,
    val errors: List<ValidationDetail>? = null
) : Serializable
