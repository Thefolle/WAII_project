package it.polito.waii.order_service.dtos

class DeliveryDto(
    val shippingAddress: String,
    val warehouseId: Long
) {
    // no-arg constructor used by Jackson mapper
    constructor() : this("", 0)
}