package it.polito.waii.order_service.dtos

class OrderDto(
    val id: Long? = null,
    val buyerId: Long,
    val productIds: Set<Long>,
    val deliveries: Set<DeliveryDto>
)