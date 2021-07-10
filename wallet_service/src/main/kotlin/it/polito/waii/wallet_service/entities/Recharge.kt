package it.polito.waii.wallet_service.entities

import java.time.LocalDateTime
import javax.persistence.*

@Entity
class Recharge(
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    var rid: Long?,
    var rechargedMoneyAmount: Float,
    var timestamp: LocalDateTime
)
