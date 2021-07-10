package it.polito.waii.wallet_service.repositories

import it.polito.waii.wallet_service.entities.Recharge
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface RechargeRepository: CrudRepository<Recharge, Long> {
}
