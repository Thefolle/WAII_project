package it.polito.waii.wallet_service.repositories


import it.polito.waii.wallet_service.entities.Transaction
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
interface TransactionRepository: CrudRepository<Transaction, Long> {
    fun findByTimestampBetween(startDate: LocalDateTime, endDate: LocalDateTime): MutableList<Transaction>
}
