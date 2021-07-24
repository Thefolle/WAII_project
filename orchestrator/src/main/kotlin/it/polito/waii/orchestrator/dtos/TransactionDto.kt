package it.polito.waii.orchestrator.dtos

import java.time.LocalDateTime

class TransactionDto(
    var tid: Long?,
    var wid: Long,
    var transactedMoneyAmount: Float,
    var timestamp: LocalDateTime?,
    var isRech: Boolean,
    var orderId: Long?,
    var rechargeId: Long?
)