package it.polito.waii.order_service.services

import it.polito.waii.order_service.dtos.OrderDto
import reactor.core.publisher.Mono

interface OrderService {

    fun createOrder(orderDto: OrderDto): Mono<Long>

}