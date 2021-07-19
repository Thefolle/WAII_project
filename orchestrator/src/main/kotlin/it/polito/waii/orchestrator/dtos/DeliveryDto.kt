package it.polito.waii.orchestrator.dtos

class DeliveryDto(
    val id: Long?,
    val shippingAddress: String,
    val warehouseId: Long
) {
    // no-arg constructor used by Jackson mapper
    constructor() : this(null, "", 0)
}