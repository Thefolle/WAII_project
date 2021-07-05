package it.polito.waii.wallet_service.controllers

import it.polito.waii.wallet_service.dtos.TransactionDTO
import it.polito.waii.wallet_service.services.WalletServiceImpl
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.LocalDateTime
import java.time.ZoneOffset

@RestController
// this controller's URL are of type "/wallets/something"
// omit the "wallets" root of the URL, I will manage that inside the API gateway class
//Pierluigi
class WalletController(private val walletServiceImpl: WalletServiceImpl) {

    @PostMapping("/{walletId}/transactions")
    fun performTransaction(@PathVariable("walletId") walletId: Long,
                           @RequestBody transaction: TransactionDTO): ResponseEntity<TransactionDTO> {
        return ResponseEntity.status(HttpStatus.OK).body(walletServiceImpl.performTransaction(transaction))
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

}
