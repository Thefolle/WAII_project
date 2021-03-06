package it.polito.waii.wallet_service.entities


import it.polito.waii.wallet_service.dtos.TransactionDTO
import java.time.LocalDateTime
import javax.persistence.*

@Entity
class Transaction(
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    var tid: Long?,
    @ManyToOne(fetch=FetchType.LAZY)
    var wallet: Wallet,
    var transactedMoneyAmount: Float,
    var timestamp: LocalDateTime,
    var isRech: Boolean,
    var orderId: Long?,
    @OneToOne
    var recharge: Recharge?){

    fun toDto() = TransactionDTO(
        tid = tid,
        wid = wallet.wid!!,
        transactedMoneyAmount = transactedMoneyAmount,
        timestamp = timestamp,
        isRech = isRech,
        orderId = orderId,
        rechargeId = recharge?.rid
    )
}
