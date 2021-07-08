package it.polito.waii.order_service.services

import it.polito.waii.order_service.dtos.OrderDto
import kotlinx.coroutines.CoroutineScope
import org.neo4j.cypherdsl.core.Order
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

interface OrderService {

    fun createOrder(orderDto: OrderDto): Mono<Long>
    fun getOrders(): Flux<OrderDto>
    fun getOrderById(id: Long): Mono<OrderDto>
    fun updateOrder(orderDto: OrderDto): Mono<Void>
    fun deleteOrderById(id: Long): Mono<Void>

}