package it.polito.waii.warehouse_service.controllers

import com.sun.mail.iap.Response
import it.polito.waii.warehouse_service.dtos.UpdateQuantityDtoKafka
import it.polito.waii.warehouse_service.exceptions.UnsatisfiableRequestException
import it.polito.waii.warehouse_service.services.WarehouseService
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.annotation.TopicPartition
import org.springframework.messaging.Message
import org.springframework.messaging.handler.annotation.SendTo
import org.springframework.messaging.support.MessageBuilder
import org.springframework.stereotype.Component
import org.springframework.web.server.ResponseStatusException

@Component
class WarehouseControllerKafka(val warehouseService: WarehouseService) {

    @SendTo("warehouse_service_responses")
    @KafkaListener(
        containerFactory = "updateQuantitiesConcurrentKafkaListenerContainerFactory",
        topicPartitions = [TopicPartition(topic = "warehouse_service_requests", partitions = ["0"])]
    )
    fun updateProductQuantities(updateQuantitiesDto: Set<UpdateQuantityDtoKafka>): Message<Float> {

        val totalPrice: Float
        try {
            totalPrice = warehouseService
                .updateProductQuantities(
                    updateQuantitiesDto
                )
        } catch (exception: ResponseStatusException) {
            return MessageBuilder
                .withPayload(0f)
                .setHeader("hasException", true)
                .setHeader("exceptionMessage", exception.reason)
                .setHeader("exceptionRawStatus", exception.rawStatusCode)
                .build()
        }

        return MessageBuilder
            .withPayload(totalPrice)
            .setHeader("hasException", false)
            .build()
    }

}