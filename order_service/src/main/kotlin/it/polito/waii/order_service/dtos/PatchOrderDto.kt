package it.polito.waii.order_service.dtos

import it.polito.waii.order_service.entities.OrderStatus

class PatchOrderDto(
    val id: Long? = null,
    val buyerId: Long?,
    val walletId: Long?,
    val deliveries: Map<Long, PatchDeliveryDto>?,
    val quantities: Map<Long, Long>?,
    val status: OrderStatus? = null
) {
    // no-arg constructor for Jackson
    constructor() : this(null, 0, 0, mapOf(), mapOf(), null)
}