package it.polito.waii.warehouse_service.dtos

data class PartialWarehouseDto(
    var name: String?,
    var city: String?,
    var region: String?,
    // expressed in m^3
    var capacity: Long?
)
