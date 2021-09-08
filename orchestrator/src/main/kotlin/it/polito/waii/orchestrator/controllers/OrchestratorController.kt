package it.polito.waii.orchestrator.controllers

import it.polito.waii.orchestrator.dtos.Action
import it.polito.waii.orchestrator.dtos.OrderDtoOrchestrator
import it.polito.waii.orchestrator.dtos.TransactionDto
import it.polito.waii.orchestrator.dtos.UpdateQuantityDtoKafka
import it.polito.waii.orchestrator.exceptions.UnsatisfiableRequestException
import it.polito.waii.orchestrator.services.OrchestratorService
import org.springframework.http.HttpStatus
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.annotation.TopicPartition
import org.springframework.kafka.support.KafkaNull
import org.springframework.messaging.Message
import org.springframework.messaging.handler.annotation.Header
import org.springframework.messaging.handler.annotation.SendTo
import org.springframework.messaging.support.MessageBuilder
import org.springframework.stereotype.Component
import org.springframework.web.server.ResponseStatusException

@Component
class OrchestratorController(val orchestratorService: OrchestratorService) {

    @SendTo("orchestrator_responses")
    @KafkaListener(
        containerFactory = "createOrderConcurrentKafkaListenerContainerFactory",
        topicPartitions = [TopicPartition(topic = "orchestrator_requests", partitions = ["0"])]
    )
    fun createOrder(orderDtoOrchestrator: OrderDtoOrchestrator, @Header("username") username: String, @Header("roles") roles: String): Message<Float> {

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
        var totalPrice: Float
        var futureResult: Message<*>
        try {
            futureResult =
                orchestratorService
                    .checkWarehouse(
                        updateQuantities
                    )
                    .get()
            totalPrice = futureResult.payload
        } catch (exception: Exception) {
            return MessageBuilder
                .withPayload(0f)
                .setHeader("hasException", true)
                .setHeader("exceptionMessage", "The request cannot be processed due to some" +
                        "malfunction. Please, try later.")
                .setHeader("exceptionStatus", HttpStatus.REQUEST_TIMEOUT.value())
                .build()
        }

        if ((futureResult.headers["hasException"] as Boolean)) {
            return MessageBuilder
                .withPayload(0f)
                .setHeader("hasException", true)
                .setHeader("exceptionMessage", futureResult.headers["exceptionMessage"])
                .setHeader("exceptionRawStatus", futureResult.headers["exceptionRawStatus"])
                .build()
        }


        // prepare data
        val transactionDto =
            TransactionDto(
                null,
                orderDtoOrchestrator.walletId,
                totalPrice,
                null,
                !orderDtoOrchestrator.isIssuingOrCancelling,
                orderDtoOrchestrator.orderId,
                null
            )

        // withdraw from the wallet
        try {
            futureResult =
                orchestratorService
                    .checkWallet(
                        transactionDto,
                        username,
                        roles
                    )
                    .get()
        } catch (exception: Exception) {
            return MessageBuilder
                .withPayload(0f)
                .setHeader("hasException", true)
                .setHeader("exceptionMessage", "The request cannot be processed due to some" +
                        "malfunction. Please, try later.")
                .setHeader("exceptionStatus", HttpStatus.REQUEST_TIMEOUT.value())
                .build()
        }

        if ((futureResult.headers["hasException"] as Boolean)) {
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

            return MessageBuilder
                .withPayload(0f)
                .setHeader("hasException", true)
                .setHeader("exceptionMessage", futureResult.headers["exceptionMessage"])
                .setHeader("exceptionRawStatus", futureResult.headers["exceptionRawStatus"])
                .build()
        }

        return MessageBuilder
            .withPayload(totalPrice)
            .setHeader("hasException", false)
            .build()
    }

}