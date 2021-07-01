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

    /* Design

    There are right but conflicting design patterns:
    a) All listeners should subscribe to the same partition so as to preserve order of messages;
    b) There should be one listener per partition, because:
        1) If there are more listeners than partitions, (#listeners - #partitions) listeners are never triggered;
        2) If there are less listeners than partitions, the same handler should be in charge of serving multiple partitions.

    The design pattern 'a' fall backs to the point 'b1', so the final solution chosen is 'b'.
    The problem of this solution arises when three actors A, B and C are involved in a transaction:
        if A creates an order, B is this service and C reads the orders, then the response sent to C is not deterministic.
        if A = C, the problem vanishes since A can wait for the creation of the new order.
    */

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