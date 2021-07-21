package it.polito.waii.warehouse_service.dtos

import it.polito.waii.warehouse_service.entities.Action

class UpdateQuantityDtoKafka(
    var warehouseId: Long,
    var productId: Long,
    var quantity: Long,
    var action: Action
)