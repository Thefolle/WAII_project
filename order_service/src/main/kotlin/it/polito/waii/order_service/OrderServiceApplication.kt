package it.polito.waii.order_service

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean

@SpringBootApplication
class OrderServiceApplication {

}

fun main(args: Array<String>) {
    runApplication<OrderServiceApplication>(*args)
}
