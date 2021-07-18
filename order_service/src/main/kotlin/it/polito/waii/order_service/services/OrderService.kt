package it.polito.waii.order_service.services

import it.polito.waii.order_service.dtos.OrderDto
import it.polito.waii.order_service.dtos.PatchOrderDto
import it.polito.waii.order_service.entities.Order
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

interface OrderService {

    fun createOrder(orderDto: OrderDto): Mono<Long>
    fun getOrders(): Flux<OrderDto>
    fun getOrderById(id: Long): Mono<OrderDto>
    suspend fun updateOrder(orderDto: PatchOrderDto): Order
    fun deleteOrderById(id: Long): Mono<Void>

}