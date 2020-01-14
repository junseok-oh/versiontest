package com.deliveryhero.alfred.connector.sdk.exception

open class ConnectorException : Exception {
    constructor(): super()
    constructor(message: String?) : super(message)
    constructor(message: String?, cause: Throwable?) : super(message, cause)
}

