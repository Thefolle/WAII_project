package it.polito.waii.orchestrator.services

import it.polito.waii.orchestrator.dtos.TransactionDto
import it.polito.waii.orchestrator.dtos.UpdateQuantityDtoKafka
import org.springframework.kafka.requestreply.RequestReplyMessageFuture
import org.springframework.kafka.requestreply.RequestReplyTypedMessageFuture

interface OrchestratorService {

    fun checkWarehouse(updateQuantitiesDto: Set<UpdateQuantityDtoKafka>): RequestReplyTypedMessageFuture<String, Set<UpdateQuantityDtoKafka>, Float>
    fun checkWallet(transactionDto: TransactionDto, username: String, roles: String): RequestReplyMessageFuture<String, TransactionDto>

}