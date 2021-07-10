package it.polito.waii.warehouse_service.dtos

import it.polito.waii.warehouse_service.entities.Product

data class WarehouseDto(
    var id: Long?,
    var name: String,
    var city: String,
    var region: String,
    // expressed in m^3
    var capacity: Long,
    var availability: Long?
)
