package it.polito.waii.wallet_service.controllers

import it.polito.waii.wallet_service.dtos.TransactionDTO
import it.polito.waii.wallet_service.exceptions.UnsatisfiableRequestException
import it.polito.waii.wallet_service.services.WalletService
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.annotation.TopicPartition
import org.springframework.messaging.handler.annotation.Header
import org.springframework.messaging.handler.annotation.SendTo
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import java.security.Principal

@Component
class WalletControllerKafka(private val walletService: WalletService) {

    @SendTo("wallet_service_responses")
    @KafkaListener(
        containerFactory = "performTransactionConcurrentKafkaListenerContainerFactory",
        topicPartitions = [TopicPartition(topic = "wallet_service_requests", partitions = ["0"])]
    )
    fun performTransaction(transactionDTO: TransactionDTO, @Header("username") username: String, @Header("roles") roles: String): Long {

        try {
            return if (transactionDTO.isRech)
                walletService.doRecharge(transactionDTO, roles).tid!!
            else
                walletService.doCharge(transactionDTO, username).tid!!
        } catch (exception: Exception) {
            throw UnsatisfiableRequestException(exception.message)
        }

    }

}