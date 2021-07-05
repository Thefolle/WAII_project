package it.polito.waii.wallet_service.services

import it.polito.waii.wallet_service.dtos.TransactionDTO
import it.polito.waii.wallet_service.entities.Transaction
import it.polito.waii.wallet_service.entities.Wallet
import it.polito.waii.wallet_service.repositories.TransactionRepository
import it.polito.waii.wallet_service.repositories.WalletRepository
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import java.time.LocalDateTime

@Service
@Transactional
class WalletServiceImpl(val walletRepository: WalletRepository, val transactionRepository: TransactionRepository): WalletService {

    private fun getWalletbyId(walletId: Long): Wallet {
        val walletOptional = walletRepository.findById(walletId)
        if (walletOptional.isEmpty) throw ResponseStatusException(HttpStatus.NOT_FOUND, "No wallet with id $walletId exists.")
        return walletOptional.get()
    }

    //ToDo: add security constraint (Only ADMINS can perform this)
    fun doRecharge(transaction: TransactionDTO): TransactionDTO {
        val wallet = getWalletbyId(transaction.wid)
        wallet.addBalance(transaction.transactedMoneyAmount)
        val res = Transaction(tid = null,
                              wid = transaction.wid,
                              transactedMoneyAmount = transaction.transactedMoneyAmount,
                              timestamp = LocalDateTime.now(),
                              isRech = true,
                              orderId = null,
                              rechargeId = transaction.rechargeId)
        transactionRepository.save(res)
        return res.toDto()
    }

    fun doCharge(transaction: TransactionDTO): TransactionDTO {
        val wallet = getWalletbyId(transaction.wid)
        //ToDo : check if I am the owner of the wallet
        if (wallet.balance < transaction.transactedMoneyAmount) throw ResponseStatusException(HttpStatus.FORBIDDEN, "Not enough money!")
        wallet.addBalance(-transaction.transactedMoneyAmount)
        val res = Transaction(tid = null,
            wid = transaction.wid,
            transactedMoneyAmount = transaction.transactedMoneyAmount,
            timestamp = LocalDateTime.now(),
            isRech = false,
            orderId = transaction.orderId,
            rechargeId = null)
        transactionRepository.save(res)
        return res.toDto()
    }

    override fun performTransaction(transaction: TransactionDTO): TransactionDTO {

        return if (transaction.isRech)
            doRecharge(transaction)
        else
            doCharge(transaction)
    }

    override fun getTransaction(walletId: Long, transactionId: Long): TransactionDTO {
        val transactionOptional = transactionRepository.findById(transactionId)
        if (transactionOptional.isEmpty) throw ResponseStatusException(HttpStatus.NOT_FOUND, "No transaction with id $transactionId exists.")
        return transactionOptional.get().toDto()
    }

    override fun getTransactions(walletId: Long, startDate: LocalDateTime, endDate: LocalDateTime): List<TransactionDTO> {
        if (!walletRepository.existsById(walletId)) throw ResponseStatusException(HttpStatus.NOT_FOUND, "No wallet with id $walletId exists.")

        return transactionRepository
            .findByTimestampBetween(startDate, endDate).map { it.toDto() }
            .filter { it.wid == walletId }
    }

}
