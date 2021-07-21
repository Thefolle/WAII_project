package it.polito.waii.warehouse_service.controllers

import it.polito.waii.warehouse_service.dtos.UpdateQuantityDtoKafka
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.annotation.TopicPartition
import org.springframework.messaging.handler.annotation.SendTo
import org.springframework.stereotype.Component

@Component
class WarehouseControllerKafka {

    @SendTo("warehouse_service_responses")
    @KafkaListener(
        containerFactory = "updateQuantityConcurrentKafkaListenerContainerFactory",
        topicPartitions = [TopicPartition(topic = "warehouse_service_requests", partitions = ["0"])]
    )
    fun updateProductQuantity(updateQuantityDtoKafka: UpdateQuantityDtoKafka): Long {
        println("\n\nWarehouse received\n\n")

        return 10
    }

}