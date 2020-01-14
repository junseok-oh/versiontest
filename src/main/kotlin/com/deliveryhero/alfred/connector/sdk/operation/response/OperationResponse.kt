package com.deliveryhero.alfred.connector.sdk.operation.response

import com.deliveryhero.alfred.connector.sdk.operation.Operation
import com.deliveryhero.alfred.connector.sdk.operation.OperationType
import com.deliveryhero.alfred.connector.sdk.operation.response.redirect.RedirectRequest

data class OperationResponse @JvmOverloads constructor(
    var status: OperationStatus,
    var reference: String? = null,
    var redirectRequest: RedirectRequest? = null,
    var rawStatus: String? = null,
    var rawResponse: String? = null,
    var paymentInstrumentToken: String? = null,
    override var type: OperationType
) : Operation(type) {
    companion object {
        fun buildOperationNotAllowed() = OperationResponse(
            status = OperationStatus.OPERATION_NOT_ALLOWED,
            type = OperationType.UNKNOWN
        )

        fun buildUnkownError() = OperationResponse(
            status = OperationStatus.UNKNOWN_ERROR,
            type = OperationType.UNKNOWN
        )
    }
}
