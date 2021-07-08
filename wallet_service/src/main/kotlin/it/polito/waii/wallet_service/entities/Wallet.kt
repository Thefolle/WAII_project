package it.polito.waii.wallet_service.entities

import it.polito.waii.wallet_service.dtos.WalletDTO
import javax.persistence.*
import javax.validation.constraints.Min

@Entity
class Wallet(
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    var wid: Long?=0,
    var ownerUsername: String,
    @Min(value = 0)
    var balance: Float = 0.0F,
    @OneToMany(mappedBy = "wallet")
    var transactions: MutableList<Transaction>?= mutableListOf<Transaction>())
{
    fun addBalance(difference: Float): Float {
        if (balance + difference  < 0.0F) {
            balance = 0.0F
        } else {
            balance += difference
        }

        return balance
    }

    fun toDTO() = WalletDTO(
        wid = wid,
        ownerUsername = ownerUsername,
        balance = balance
    )
}
