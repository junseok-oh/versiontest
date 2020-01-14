package com.deliveryhero.alfred.connector.sdk.operation.response

enum class OperationStatus {

    //didn't call psp
    CONFIG_ERROR, //there is a missing or invalid config for calling the psp
    OPERATION_NOT_ALLOWED, //we dont allow the tx

    //called the psp
    OK, //ok
    PARAMS_ERROR, //the psp says there is an error in our params
    UNKNOWN_ERROR, //the psp says there is an error, but we don't have it mapped
    INVALID_CARD, //generic invalid card
    BLOCKED_CARD, //card is blocked
    EXPIRED_CARD, //card is expired
    FRAUD_RISK, //psp or acquirer says rejected for fraud
    INSUFFICIENT_FUNDS, //card doesnt have enough funds
    DO_NOT_HONOR, //generic rejection
    PROVIDER_ERROR, //
    TIMEOUT, //timeout in the operation
    DUPLICATE_OPERATION, //psp says it already processed the tx
    REDIRECT_REQUIRED, //psp requires redirect for finishing (should have a redirectRequest obj)

    CREATED, //created in the psp, but not yet processed
    PENDING //some action is pending (different than redirect required)
}
