package it.polito.waii.order_service.controllers

import it.polito.waii.order_service.dtos.OrderDto
import it.polito.waii.order_service.services.OrderService
import kotlinx.coroutines.reactive.awaitLast
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.runBlocking
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.annotation.TopicPartition
import org.springframework.kafka.support.KafkaNull
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.messaging.handler.annotation.SendTo
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

@Component
class OrderController {

    @Autowired
    lateinit var orderService: OrderService

    // Methods are incomplete for two reasons:
    // - no communication with the other services
    // - the return value must be provided by sending another message to the caller

//    @SendTo("topic1")
    @KafkaListener(topics = ["topic1"], containerFactory = "orderDtoConcurrentKafkaListenerContainerFactory", topicPartitions = [TopicPartition(topic = "topic1", partitions =  ["0"])])
    fun createOrder(orderDto: OrderDto): Long {
        return orderService
                .createOrder(orderDto)
                .block()!!
    }

//    @SendTo("topic1")
    @KafkaListener(topics = ["topic1"], containerFactory = "voidConcurrentKafkaListenerContainerFactory", topicPartitions = [TopicPartition(topic = "topic1", partitions =  ["1"])])
    fun getOrders(@Payload(required = false) empty: Void?): Set<OrderDto> {
        return orderService
            .getOrders()
            .toIterable()
            .toSet()
            .apply {
                println(this)
            }
    }

}