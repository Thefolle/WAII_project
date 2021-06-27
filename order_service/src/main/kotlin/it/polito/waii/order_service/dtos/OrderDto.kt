package it.polito.waii.order_service.dtos

class OrderDto(
    val buyerId: Long,
    val productIds: Set<Long>
)