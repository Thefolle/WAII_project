package it.polito.waii.order_service.controllers

import it.polito.waii.order_service.dtos.OrderDto
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class TestController {

    @Autowired
    lateinit var template: KafkaTemplate<String, OrderDto>

    @GetMapping
    fun test() {
        template.send("topic1", OrderDto(2, setOf(3, 5)))
        println("Sending message")
    }
}