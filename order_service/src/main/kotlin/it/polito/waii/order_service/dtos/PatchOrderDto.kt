package it.polito.waii.order_service.dtos

import it.polito.waii.order_service.entities.OrderStatus

class PatchOrderDto(
    val id: Long? = null,
    val buyerId: Long?,
    val deliveries: Map<Long, PatchDeliveryDto>?,
    val quantities: Map<Long, Long>?,
    val total: Float?,
    val status: OrderStatus? = null
) {
    // no-arg constructor for Jackson
    constructor() : this(null, 0, mapOf(), mapOf(), 0f, null)
}