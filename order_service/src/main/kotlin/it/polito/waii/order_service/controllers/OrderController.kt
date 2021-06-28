package it.polito.waii.order_service.controllers

import it.polito.waii.order_service.dtos.OrderDto
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component

@Component
class OrderController {

    @KafkaListener(topics = ["topic1"], containerFactory = "concurrentKafkaListenerContainerFactory")
    fun listen(orderDto: OrderDto) {
        println(orderDto.productIds)
    }

}