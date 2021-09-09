package it.polito.waii.wallet_service.controllers

import it.polito.waii.wallet_service.dtos.TransactionDTO
import it.polito.waii.wallet_service.dtos.WalletDTO
import it.polito.waii.wallet_service.services.WalletServiceImpl
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException
import java.time.LocalDateTime
import java.time.ZoneOffset

@RestController
class WalletController(private val walletServiceImpl: WalletServiceImpl) {

    @PostMapping("/{walletId}/transactions")
    fun performTransaction(@PathVariable("walletId") walletId: Long,
                           @RequestBody transaction: TransactionDTO): ResponseEntity<TransactionDTO> {
        if (transaction.wid != walletId) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "The wallet id in the request body must match" +
                    " the id in the URI.")
        }

        return if (transaction.isRech)
            ResponseEntity.status(HttpStatus.OK).body(walletServiceImpl.doRecharge(transaction))
        else
            ResponseEntity.status(HttpStatus.OK).body(walletServiceImpl.doCharge(transaction))
    }

    @GetMapping("/{walletId}/transactions/{transactionId}")
    fun getTransaction(@PathVariable("walletId") walletId: Long,
                       @PathVariable("transactionId") transactionId: Long): ResponseEntity<TransactionDTO> {
        return ResponseEntity.status(HttpStatus.OK).body(walletServiceImpl.getTransaction(walletId, transactionId))
    }

    @GetMapping("/{walletId}/transactions")
    fun getTransactions(@PathVariable("walletId") walletId: Long,
                        @RequestParam("from") from: Long,
                        @RequestParam("to") to: Long): ResponseEntity<List<TransactionDTO>> {
        val result = walletServiceImpl.getTransactions(walletId, LocalDateTime.ofEpochSecond(from / 1000, 0, ZoneOffset.UTC), LocalDateTime.ofEpochSecond(to / 1000, 0, ZoneOffset.UTC))

        return if (result.isEmpty())
            ResponseEntity.status(HttpStatus.NO_CONTENT).build()
        else ResponseEntity.status(HttpStatus.OK).body(result)
    }

    @GetMapping("/{walletId}/allTransactions")
    fun getTransactions(@PathVariable("walletId") walletId: Long): ResponseEntity<List<TransactionDTO>> {
        val result = walletServiceImpl.getAllTransactions(walletId)

        return if (result.isEmpty())
            ResponseEntity.status(HttpStatus.NO_CONTENT).build()
        else ResponseEntity.status(HttpStatus.OK).body(result)
    }

    @PostMapping("/")
    fun createWallet(): ResponseEntity<WalletDTO>{
        return ResponseEntity.status(HttpStatus.CREATED).body(walletServiceImpl.createWallet())
    }

    @GetMapping("/{walletId}")
    fun getWallet(@PathVariable("walletId") walletId: Long): ResponseEntity<WalletDTO>{
        return ResponseEntity.status(HttpStatus.OK).body(walletServiceImpl.getWallet(walletId))
    }

}
