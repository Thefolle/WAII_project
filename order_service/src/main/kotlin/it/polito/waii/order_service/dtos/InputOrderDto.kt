package it.polito.waii.order_service.dtos

import it.polito.waii.order_service.entities.OrderStatus

/**
 * The product ids correspond to the key list of the deliveries field and the quantities field
  */


class InputOrderDto(
    val walletId: Long,
    val deliveries: Map<Long, DeliveryDto>,
    val quantities: Map<Long, Long>,
) {
    // no-arg constructor for Jackson
    constructor() : this(0, mapOf(), mapOf())
}