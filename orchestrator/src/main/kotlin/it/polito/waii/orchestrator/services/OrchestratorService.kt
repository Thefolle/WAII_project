package it.polito.waii.orchestrator.services

import it.polito.waii.orchestrator.dtos.TransactionDto
import it.polito.waii.orchestrator.dtos.UpdateQuantityDtoKafka

interface OrchestratorService {

    fun checkWarehouse(updateQuantityDtoKafka: UpdateQuantityDtoKafka)
    fun checkWallet(transactionDto: TransactionDto)

}