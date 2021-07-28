package it.polito.waii.warehouse_service.exceptions

import java.lang.RuntimeException

class UnsatisfiableRequestException(
    override val message: String?
): RuntimeException(message)