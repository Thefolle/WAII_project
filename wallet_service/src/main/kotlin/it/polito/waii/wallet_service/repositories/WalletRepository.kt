package it.polito.waii.wallet_service.repositories

import it.polito.waii.wallet_service.entities.Wallet
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface WalletRepository: CrudRepository<Wallet, Long> {

    fun findByOwnerUsername(username: String): Optional<Wallet>

}
