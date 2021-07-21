package it.polito.waii.orchestrator.exceptions

import java.lang.RuntimeException

class UnsatisfiableRequestException(
    override val message: String?
): RuntimeException(message)