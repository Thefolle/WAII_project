package it.polito.waii.warehouse_service.controllers

import it.polito.waii.warehouse_service.dtos.UpdateQuantityDtoKafka
import it.polito.waii.warehouse_service.exceptions.UnsatisfiableRequestException
import it.polito.waii.warehouse_service.services.WarehouseService
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.annotation.TopicPartition
import org.springframework.messaging.handler.annotation.SendTo
import org.springframework.stereotype.Component

@Component
class WarehouseControllerKafka(val warehouseService: WarehouseService) {

    @SendTo("warehouse_service_responses")
    @KafkaListener(
        containerFactory = "updateQuantityConcurrentKafkaListenerContainerFactory",
        topicPartitions = [TopicPartition(topic = "warehouse_service_requests", partitions = ["0"])]
    )
    fun updateProductQuantity(updateQuantityDtoKafka: UpdateQuantityDtoKafka): Long {
        println("Warehouse service received")

        try {
            warehouseService
                .updateProductQuantity(
                    updateQuantityDtoKafka.warehouseId,
                    updateQuantityDtoKafka.toUpdateQuantityDto()
                )
        } catch (exception: Exception) {
            throw UnsatisfiableRequestException(exception.message)
        }

        return 10
    }

}