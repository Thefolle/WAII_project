package it.polito.waii.wallet_service.controllers

import it.polito.waii.wallet_service.dtos.TransactionDTO
import it.polito.waii.wallet_service.services.WalletService
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.annotation.TopicPartition
import org.springframework.messaging.handler.annotation.SendTo
import org.springframework.stereotype.Component

@Component
class WalletControllerKafka(private val walletService: WalletService) {

    @SendTo("wallet_service_responses")
    @KafkaListener(
        containerFactory = "performTransactionConcurrentKafkaListenerContainerFactory",
        topicPartitions = [TopicPartition(topic = "wallet_service_requests", partitions = ["0"])]
    )
    fun performTransaction(transactionDTO: TransactionDTO): Long {
        println("Wallet received")

        return if (transactionDTO.isRech)
            walletService.doRecharge(transactionDTO).tid!!
        else
            walletService.doCharge(transactionDTO).tid!!
    }

}