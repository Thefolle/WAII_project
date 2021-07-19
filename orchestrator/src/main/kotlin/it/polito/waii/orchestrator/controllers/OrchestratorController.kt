package it.polito.waii.orchestrator.controllers

import it.polito.waii.orchestrator.dtos.OrderDtoOrchestrator
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.annotation.TopicPartition
import org.springframework.messaging.handler.annotation.SendTo
import org.springframework.stereotype.Component

@Component
class OrchestratorController {

    @SendTo("orchestrator_responses")
    @KafkaListener(
        containerFactory = "createOrderOrchestratorConcurrentKafkaListenerContainerFactory",
        topicPartitions = [TopicPartition(topic = "orchestrator_requests", partitions = ["0"])]
    )
    fun createOrder(orderDtoOrchestrator: OrderDtoOrchestrator): Long {
        println("\nReceived\n")
        // check warehouse availability as warehouse.capacity - sum(product.quantity) >= 0
        // check wallet balance against the order's total price
        // perform transactions
        return 1
    }

}