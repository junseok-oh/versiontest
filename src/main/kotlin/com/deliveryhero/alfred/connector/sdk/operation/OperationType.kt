package com.deliveryhero.alfred.connector.sdk.operation

enum class OperationType {
    //payment
    PREAUTHORIZE,
    AUTHORIZE,
    CANCEL,
    CAPTURE,
    REFUND,
    CREDIT,
    //others
    GET,
    SEARCH,
    BALANCE,
    TOKENIZE,
    UNKNOWN
}
