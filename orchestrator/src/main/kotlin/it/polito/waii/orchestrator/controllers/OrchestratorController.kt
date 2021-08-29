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
    fun createOrder(orderDtoOrchestrator: OrderDtoOrchestrator, @Header("username") username: String, @Header("roles") roles: String): Float {

        // prepare data
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

        // check warehouse availability and get the total price of the products
        var totalPrice = 0f
        try {
            totalPrice =
                orchestratorService
                    .checkWarehouse(
                        updateQuantities
                    )
                    .get()
                    .payload
        } catch (exception: Exception) {
            throw UnsatisfiableRequestException("Some failure of warehouse_service has occurred")
        }


        // prepare data
        val transactionDto =
            TransactionDto(
                null,
                orderDtoOrchestrator.walletId,
                totalPrice,
                null,
                !orderDtoOrchestrator.isIssuingOrCancelling,
                // for now this is null: modify order_service to first save the order, as Saga requires
                orderDtoOrchestrator.id,
                null
            )

        try {
            orchestratorService
                .checkWallet(
                    transactionDto,
                    username,
                    roles
                )
                .get()
        } catch (exception: Exception) {
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

            throw UnsatisfiableRequestException("Some failure of wallet_service occurred")
        }


        return totalPrice
    }

}