package it.polito.waii.order_service.dtos

import it.polito.waii.order_service.entities.OrderStatus

/**
 * The product ids correspond to the key list of the deliveries field and the quantities field
  */


class OrderDto(
    val id: Long? = null,
    val buyerId: Long,
    val walletId: Long,
    val deliveries: Map<Long, DeliveryDto>,
    val quantities: Map<Long, Long>,
    val total: Float,
    val status: OrderStatus? = null
) {
    // no-arg constructor for Jackson
    constructor() : this(null, 0, 0, mapOf(), mapOf(), 0f, null)

    fun toOrderDtoOrchestrator(isIssuingOrCancelling: Boolean) = OrderDtoOrchestrator(
        id,
        buyerId,
        walletId,
        deliveries,
        quantities,
        total,
        isIssuingOrCancelling,
        status
    )
}