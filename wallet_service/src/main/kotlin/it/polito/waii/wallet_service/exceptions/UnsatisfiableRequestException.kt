package it.polito.waii.wallet_service.exceptions

import java.lang.RuntimeException

class UnsatisfiableRequestException(
    override val message: String?
): RuntimeException(message)