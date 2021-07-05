package it.polito.waii.wallet_service.entities


import it.polito.waii.wallet_service.dtos.TransactionDTO
import java.time.LocalDateTime
import javax.persistence.*

@Entity
class Transaction(
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    var tid: Long?,
    var wid: Long,
    var transactedMoneyAmount: Float,
    var timestamp: LocalDateTime,
    var isRech: Boolean,
    var orderId: Long?,
    var rechargeId: Long?){

    fun toDto() = TransactionDTO(
        tid = tid,
        wid = wid,
        transactedMoneyAmount = transactedMoneyAmount,
        timestamp = timestamp,
        isRech = isRech,
        orderId = orderId,
        rechargeId = rechargeId
    )
}
