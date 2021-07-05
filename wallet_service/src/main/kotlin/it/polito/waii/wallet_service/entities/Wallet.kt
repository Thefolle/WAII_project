package it.polito.waii.wallet_service.entities

import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.validation.constraints.Min

class Wallet(
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    var wid: Long?=0,
    @Min(value = 0)
    var balance: Float = 0.0F)
{
    fun addBalance(difference: Float): Float {
        if (balance + difference  < 0.0F) {
            balance = 0.0F
        } else {
            balance += difference
        }

        return balance
    }
}
