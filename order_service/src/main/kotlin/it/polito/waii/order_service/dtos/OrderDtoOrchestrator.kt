package it.polito.waii.order_service.dtos

import it.polito.waii.order_service.entities.OrderStatus

class OrderDtoOrchestrator (
    val id: Long? = null,
    val buyerId: Long,
    val walletId: Long,
    val deliveries: Map<Long, DeliveryDto>,
    val quantities: Map<Long, Long>,
    val total: Float,
    var isIssuingOrCancelling: Boolean?,
    val status: OrderStatus? = null
    ) {
        // no-arg constructor for Jackson
        constructor() : this(null, 0, 0, mapOf(), mapOf(), 0f, false, null)
    }