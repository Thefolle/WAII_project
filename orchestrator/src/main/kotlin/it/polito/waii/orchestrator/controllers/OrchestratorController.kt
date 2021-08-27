package it.polito.waii.orchestrator.controllers

import it.polito.waii.orchestrator.dtos.Action
import it.polito.waii.orchestrator.dtos.OrderDtoOrchestrator
import it.polito.waii.orchestrator.dtos.TransactionDto
import it.polito.waii.orchestrator.dtos.UpdateQuantityDtoKafka
import it.polito.waii.orchestrator.exceptions.UnsatisfiableRequestException
import it.polito.waii.orchestrator.services.OrchestratorService
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.annotation.TopicPartition
import org.springframework.messaging.handler.annotation.Header
import org.springframework.messaging.handler.annotation.SendTo
import org.springframework.stereotype.Component

@Component
class OrchestratorController(val orchestratorService: OrchestratorService) {

    @SendTo("orchestrator_responses")
    @KafkaListener(
        containerFactory = "createOrderConcurrentKafkaListenerContainerFactory",
        topicPartitions = [TopicPartition(topic = "orchestrator_requests", partitions = ["0"])]
    )
    fun createOrder(orderDtoOrchestrator: OrderDtoOrchestrator, @Header("username") username: String, @Header("roles") roles: String): Long {

        // warehouse check
        val updateQuantities =
            orderDtoOrchestrator.deliveries.keys
                .map {
                    UpdateQuantityDtoKafka(
                        orderDtoOrchestrator.deliveries[it]!!.warehouseId,
                        it,
                        orderDtoOrchestrator.quantities[it]!!,
                        // quantities are increased or decreased depending on whether a customer
                        // is issuing an order or he is cancelling it
                        if (orderDtoOrchestrator.isIssuingOrCancelling) Action.REMOVE else Action.ADD
                    )
                }
                .toSet()

        val warehouseFuture =
            orchestratorService
                .checkWarehouse(
                    updateQuantities
                )

        // wallet check
        val transactionDto =
            TransactionDto(
                null,
                orderDtoOrchestrator.walletId,
                orderDtoOrchestrator.total,
                null,
                !orderDtoOrchestrator.isIssuingOrCancelling,
                // for now this is null: modify order_service to first save the order, as Saga requires
                orderDtoOrchestrator.id,
                null
            )

        val walletFuture =
            orchestratorService
                .checkWallet(
                    transactionDto,
                    username,
                    roles
                )

        // check eventual failures
        var hasFailedWallet = false
        try {
            walletFuture.get()
        } catch (exception: Exception) {
            hasFailedWallet = true
        }

        var hasFailedWarehouse = false
        try {
            warehouseFuture.get()
        } catch (exception: Exception) {
            hasFailedWarehouse = true
        }

        if (hasFailedWallet && !hasFailedWarehouse) {
            orchestratorService
                .checkWarehouse(
                    updateQuantities
                        .map {
                            it.action = if (orderDtoOrchestrator.isIssuingOrCancelling) Action.ADD else Action.REMOVE
                            it
                        }
                        .toSet()
                )
                .get()
        } else if (!hasFailedWallet && hasFailedWarehouse) {
            orchestratorService
                .checkWallet(
                    transactionDto
                        .also {
                            it.isRech = orderDtoOrchestrator.isIssuingOrCancelling
                        },
                    username,
                    roles
                )
        }

        if (hasFailedWallet || hasFailedWarehouse) {
            var message = "The order couldn't be created due to some" +
                    " failure of the following services: "
            message += if (hasFailedWallet) "wallet_service " else ""
            message += if (hasFailedWarehouse) "warehouse_service " else ""
            throw UnsatisfiableRequestException(message)
        }


        return 0
    }

}