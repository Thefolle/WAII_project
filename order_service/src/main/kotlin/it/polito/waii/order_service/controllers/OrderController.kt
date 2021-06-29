package it.polito.waii.order_service.controllers

import it.polito.waii.order_service.dtos.OrderDto
import it.polito.waii.order_service.services.OrderService
import kotlinx.coroutines.reactor.awaitSingle
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

@Component
class OrderController {

    @Autowired
    lateinit var orderService: OrderService

    @KafkaListener(topics = ["topic1"], containerFactory = "concurrentKafkaListenerContainerFactory")
    suspend fun createOrder(orderDto: OrderDto): Long {
        return orderService.createOrder(orderDto).awaitSingle()
    }

}