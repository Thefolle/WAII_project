package it.polito.waii.wallet_service.dtos

import java.time.LocalDateTime
import javax.validation.constraints.Min

data class TransactionDTO(
    var tid: Long?,
    var wid: Long,
    @Min(value = 0, message = "The transacted money amount must be non-negative.")
    var transactedMoneyAmount: Float,
    var timestamp: LocalDateTime?,
    var isRech: Boolean,
    var orderId: Long?,
    var rechargeId: Long?
)
