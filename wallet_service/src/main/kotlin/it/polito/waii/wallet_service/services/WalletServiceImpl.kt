package it.polito.waii.wallet_service.services

import it.polito.waii.wallet_service.dtos.TransactionDTO
import it.polito.waii.wallet_service.dtos.UserDTO
import it.polito.waii.wallet_service.dtos.WalletDTO
import it.polito.waii.wallet_service.entities.Recharge
import it.polito.waii.wallet_service.entities.Rolename
import it.polito.waii.wallet_service.entities.Transaction
import it.polito.waii.wallet_service.entities.Wallet
import it.polito.waii.wallet_service.exceptions.UnsatisfiableRequestException
import it.polito.waii.wallet_service.repositories.RechargeRepository
import it.polito.waii.wallet_service.repositories.TransactionRepository
import it.polito.waii.wallet_service.repositories.WalletRepository
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import java.time.LocalDateTime

@Service
@Transactional
class WalletServiceImpl(val walletRepository: WalletRepository, val transactionRepository: TransactionRepository, val rechargeRepository: RechargeRepository): WalletService {

    private fun getUsername(): String {
        val principal = SecurityContextHolder.getContext().authentication.principal
        return if (principal is UserDTO) {
            principal.username
        } else {
            principal.toString()
        }
    }

    private fun getUserRole(): Boolean {
        val principal = SecurityContextHolder.getContext().authentication.principal
        return if (principal is UserDTO) {
            principal.isAdmin
        } else {
            false
        }
    }

    private fun getWalletById(walletId: Long): Wallet {
        val walletOptional = walletRepository.findById(walletId)
        if (walletOptional.isEmpty) throw ResponseStatusException(HttpStatus.NOT_FOUND, "No wallet with id $walletId exists.")
        return walletOptional.get()
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    override fun doRecharge(transaction: TransactionDTO) = doRechargeInner(transaction)

    override fun doRecharge(transaction: TransactionDTO, roles: String): TransactionDTO {
        if (roles.contains("ROLE_ADMIN")) return doRechargeInner(transaction)
        else throw UnsatisfiableRequestException("The user cannot perform the recharge because his privileges are lower than admin.")
    }

    private fun doRechargeInner(transaction: TransactionDTO): TransactionDTO {
        val wallet = getWalletById(transaction.wid)
        wallet.addBalance(transaction.transactedMoneyAmount)
        val recharge = Recharge(rid = null,
            rechargedMoneyAmount = transaction.transactedMoneyAmount,
            timestamp = LocalDateTime.now())
        val saved = rechargeRepository.save(recharge)
        val res = Transaction(tid = null,
            wallet = wallet,
            transactedMoneyAmount = transaction.transactedMoneyAmount,
            timestamp = LocalDateTime.now(),
            isRech = true,
            orderId = null,
            recharge = saved)
        transactionRepository.save(res)
        return res.toDto()
    }

    private fun doChargeInner(transaction: TransactionDTO, username: String): TransactionDTO {
        val wallet = getWalletById(transaction.wid)
        //check if I am the owner of the wallet
        if (username != wallet.ownerUsername) throw ResponseStatusException(HttpStatus.FORBIDDEN, "You do not own this wallet!")
        //check if I have enough money
        if (wallet.balance < transaction.transactedMoneyAmount) throw ResponseStatusException(HttpStatus.FORBIDDEN, "Not enough money!")
        wallet.addBalance(-transaction.transactedMoneyAmount)
        val res = Transaction(tid = null,
            wallet = wallet,
            transactedMoneyAmount = transaction.transactedMoneyAmount,
            timestamp = LocalDateTime.now(),
            isRech = false,
            orderId = transaction.orderId,
            recharge = null)

        return transactionRepository
            .save(res)
            .toDto()
    }

    override fun doCharge(transaction: TransactionDTO) = doChargeInner(transaction, getUsername())
    override fun doCharge(transaction: TransactionDTO, username: String) = doChargeInner(transaction, username)

    override fun getTransaction(walletId: Long, transactionId: Long): TransactionDTO {
        val transactionOptional = transactionRepository.findById(transactionId)
        if (transactionOptional.isEmpty) throw ResponseStatusException(HttpStatus.NOT_FOUND, "No transaction with id $transactionId exists.")
        val isAdmin = getUserRole()
        val walletOptional = walletRepository.findById(walletId)
        val username = getUsername()
        if (!isAdmin && walletOptional.get().ownerUsername != username) throw ResponseStatusException(HttpStatus.FORBIDDEN, "You do not own this wallet!")
        if (transactionOptional.get().wallet.wid != walletId) throw ResponseStatusException(HttpStatus.NOT_FOUND, "No transaction with id $transactionId in wallet $walletId exists.")
        return transactionOptional.get().toDto()
    }

    override fun getTransactions(walletId: Long, startDate: LocalDateTime, endDate: LocalDateTime): List<TransactionDTO> {
        if (!walletRepository.existsById(walletId)) throw ResponseStatusException(HttpStatus.NOT_FOUND, "No wallet with id $walletId exists.")

        return transactionRepository
            .findByTimestampBetween(startDate, endDate).map { it.toDto() }
            .filter { it.wid == walletId }
    }

    override fun createWallet(): WalletDTO {
        val username = getUsername()
        val wallet  = Wallet(null, username, 0.0F)
        walletRepository.save(wallet)
        return wallet.toDTO()
    }

    override fun getWallet(walletId: Long): WalletDTO {
        val username = getUsername()
        val walletOptional = walletRepository.findById(walletId)
        if(walletOptional.isEmpty) throw  ResponseStatusException(HttpStatus.NOT_FOUND, "Wallet doesn't exists with id $walletId.")
        val isAdmin = getUserRole()
        if (!isAdmin && walletOptional.get().ownerUsername != username) throw ResponseStatusException(HttpStatus.FORBIDDEN, "You do not own this wallet!")
        return walletOptional.get().toDTO()
    }

}
