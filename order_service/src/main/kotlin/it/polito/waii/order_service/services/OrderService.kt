package it.polito.waii.order_service.services

import it.polito.waii.order_service.dtos.OrderDto
import it.polito.waii.order_service.dtos.PatchOrderDto
import it.polito.waii.order_service.entities.Order
import kotlinx.coroutines.CoroutineScope
import org.springframework.messaging.handler.annotation.Header
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

interface OrderService {

    suspend fun createOrder(orderDto: OrderDto, username: String, roles: String): Long
    fun getOrders(): Flux<OrderDto>
    fun getOrderById(id: Long): Mono<OrderDto>
    suspend fun updateOrder(orderDto: PatchOrderDto, username: String, roles: String): Order
    suspend fun deleteOrderById(id: Long, username: String, roles: String)


}