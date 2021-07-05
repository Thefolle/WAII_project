package it.polito.waii.wallet_service.services

import it.polito.waii.wallet_service.dtos.TransactionDTO
import java.time.LocalDateTime

interface WalletService {

    fun performTransaction(transaction: TransactionDTO): TransactionDTO

    fun getTransaction(walletId: Long, transactionId: Long): TransactionDTO

    fun getTransactions(walletId: Long, startDate: LocalDateTime, endDate: LocalDateTime): List<TransactionDTO>

}
