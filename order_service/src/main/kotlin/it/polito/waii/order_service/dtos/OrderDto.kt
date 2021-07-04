package it.polito.waii.order_service.dtos

import it.polito.waii.order_service.entities.OrderStatus

class OrderDto(
    val id: Long? = null,
    val buyerId: Long,
    val productIds: Set<Long>,
    val deliveries: Set<DeliveryDto>,
    val status: OrderStatus? = null
)