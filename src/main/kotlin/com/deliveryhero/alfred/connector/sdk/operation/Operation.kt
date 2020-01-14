package com.deliveryhero.alfred.connector.sdk.operation

import com.deliveryhero.alfred.connector.sdk.operation.request.OperationRequest
import com.deliveryhero.alfred.connector.sdk.operation.response.OperationResponse
import java.io.Serializable

abstract class Operation(
    open var type: OperationType,
    var request: OperationRequest? = null,
    var response: OperationResponse? = null
) : Serializable {
    fun requestJson(): String {
        //TODO toJson
        return this.request.toString()
    }

    fun responseJson(): String {
        //TODO toJson
        return this.response.toString()
    }
}
