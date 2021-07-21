package it.polito.waii.order_service.exceptions

import java.lang.RuntimeException

class UnsatisfiableRequestException(
    override val message: String?
): RuntimeException(message)