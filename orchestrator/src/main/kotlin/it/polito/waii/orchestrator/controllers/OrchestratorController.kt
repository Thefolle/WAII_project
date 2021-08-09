package it.polito.waii.orchestrator.controllers

import it.polito.waii.orchestrator.dtos.Action
import it.polito.waii.orchestrator.dtos.OrderDtoOrchestrator
import it.polito.waii.orchestrator.dtos.TransactionDto
import it.polito.waii.orchestrator.dtos.UpdateQuantityDtoKafka
import it.polito.waii.orchestrator.exceptions.UnsatisfiableRequestException
import it.polito.waii.orchestrator.services.OrchestratorService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.annotation.TopicPartition
import org.springframework.kafka.requestreply.ReplyingKafkaTemplate
import org.springframework.kafka.support.KafkaHeaders
import org.springframework.messaging.handler.annotation.SendTo
import org.springframework.messaging.support.MessageBuilder
import org.springframework.stereotype.Component
import java.nio.ByteBuffer

@Component
class OrchestratorController(val orchestratorService: OrchestratorService) {

    @SendTo("orchestrator_responses")
    @KafkaListener(
        containerFactory = "createOrderConcurrentKafkaListenerContainerFactory",
        topicPartitions = [TopicPartition(topic = "orchestrator_requests", partitions = ["0"])]
    )
    fun createOrder(orderDtoOrchestrator: OrderDtoOrchestrator): Long {

        println("orchestrator received")

        // warehouse check
        orchestratorService
            .checkWarehouse(
                orderDtoOrchestrator.deliveries.keys
                    .map {
                        UpdateQuantityDtoKafka(
                            orderDtoOrchestrator.deliveries[it]!!.warehouseId,
                            it,
                            orderDtoOrchestrator.quantities[it]!!,
                            Action.REMOVE
                        )
                    }
                    .toSet()
            )

        println("Warehouse checked successfully")

        // wallet check
        orchestratorService
            .checkWallet(
                TransactionDto(
                    null,
                    orderDtoOrchestrator.walletId,
                    orderDtoOrchestrator.total,
                    null,
                    false,
                    // for now this is null: modify order_service to first save the order, as Saga requires
                    orderDtoOrchestrator.id,
                    null
                )
            )

        println("Wallet checked successfully")


        return 1
        // check warehouse availability as warehouse.capacity - sum(product.quantity) >= 0
        // check wallet balance against the order's total price
        // perform transactions
//        throw UnsatisfiableRequestException("The warehouse is full")
    }

}