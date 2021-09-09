package it.polito.waii.warehouse_service.dtos

data class WarehouseDto(
    var id: Long?,
    var name: String,
    var city: String,
    var region: String,
    var capacity: Long
)
