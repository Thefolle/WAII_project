package it.polito.waii.order_service.controllers

import it.polito.waii.order_service.dtos.DeliveryDto
import it.polito.waii.order_service.dtos.OrderDto
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.support.KafkaNull
import org.springframework.messaging.MessageHeaders
import org.springframework.messaging.support.MessageBuilder
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class TestController {

    @Autowired
    lateinit var voidKafkaTemplate: KafkaTemplate<String, Void>

    @Autowired
    lateinit var orderDtoKafkaTemplate: KafkaTemplate<String, OrderDto>

    @PostMapping
    fun test() {
        orderDtoKafkaTemplate.send(
            "topic1",
            0,
            "key1",
            OrderDto(null,
                2,
                setOf(3, 5),
                setOf(DeliveryDto("Paseo de Gracia, 56", 0))
            )
        )

        println("Sending message")
    }

    @GetMapping
    fun getOrders() {
        voidKafkaTemplate.send("topic1", 1, "key1", null)
    }

}