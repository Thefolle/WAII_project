package it.polito.waii.orchestrator.dtos

/**
 * The product ids correspond to the key list of the deliveries field and the quantities field
 */


class OrderDtoOrchestrator(
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
}