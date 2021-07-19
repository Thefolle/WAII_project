package it.polito.waii.order_service.exceptions

class UnsatisfiableRequestException(
    override val message: String?
): Throwable()