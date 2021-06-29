package it.polito.waii.order_service.controllers

import it.polito.waii.order_service.dtos.OrderDto
import it.polito.waii.order_service.services.OrderService
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.runBlocking
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

@Component
class OrderController {

    @Autowired
    lateinit var orderService: OrderService

    // Methods are incomplete for two reasons:
    // - no communication with the other services
    // - the return value must be provided by sending another message to the caller

    @KafkaListener(topics = ["topic1"], containerFactory = "concurrentKafkaListenerContainerFactory")
    fun createOrder(orderDto: OrderDto): Long {
        return runBlocking {
            orderService
                .createOrder(orderDto)
                .awaitSingle()
        }
    }

}