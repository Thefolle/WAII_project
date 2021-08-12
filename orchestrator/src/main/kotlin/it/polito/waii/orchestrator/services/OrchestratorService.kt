package it.polito.waii.orchestrator.services

import it.polito.waii.orchestrator.dtos.TransactionDto
import it.polito.waii.orchestrator.dtos.UpdateQuantityDtoKafka
import org.springframework.kafka.requestreply.RequestReplyMessageFuture

interface OrchestratorService {

    fun checkWarehouse(updateQuantitiesDto: Set<UpdateQuantityDtoKafka>): RequestReplyMessageFuture<String, Set<UpdateQuantityDtoKafka>>
    fun checkWallet(transactionDto: TransactionDto): RequestReplyMessageFuture<String, TransactionDto>

}