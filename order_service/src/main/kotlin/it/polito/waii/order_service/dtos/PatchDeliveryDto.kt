package it.polito.waii.order_service.dtos

class PatchDeliveryDto(
    val shippingAddress: String?,
    val warehouseId: Long?
    ) {
        // no-arg constructor used by Jackson mapper
        constructor() : this("", 0)
    }