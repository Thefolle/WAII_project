package it.polito.waii.wallet_service.controllers

import it.polito.waii.wallet_service.dtos.TransactionDTO
import it.polito.waii.wallet_service.exceptions.UnsatisfiableRequestException
import it.polito.waii.wallet_service.services.WalletService
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.annotation.TopicPartition
import org.springframework.messaging.Message
import org.springframework.messaging.handler.annotation.Header
import org.springframework.messaging.handler.annotation.SendTo
import org.springframework.messaging.support.MessageBuilder
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.server.ResponseStatusException
import java.security.Principal

@Component
class WalletControllerKafka(private val walletService: WalletService) {

    @SendTo("wallet_service_responses")
    @KafkaListener(
        containerFactory = "performTransactionConcurrentKafkaListenerContainerFactory",
        topicPartitions = [TopicPartition(topic = "wallet_service_requests", partitions = ["0"])]
    )
    fun performTransaction(transactionDTO: TransactionDTO, @Header("username") username: String, @Header("roles") roles: String): Message<Long> {

        var payload : Long

        try {
            payload = if (transactionDTO.isRech)
                walletService.doRecharge(transactionDTO, username).tid!!
            else
                walletService.doCharge(transactionDTO, username).tid!!
        } catch (exception: ResponseStatusException) {
            return MessageBuilder
                .withPayload(0L)
                .setHeader("hasException", true)
                .setHeader("exceptionMessage", exception.reason)
                .setHeader("exceptionRawStatus", exception.status.value())
                .build()
        }

        return MessageBuilder
            .withPayload(payload)
            .setHeader("hasException", false)
            .build()

    }

}