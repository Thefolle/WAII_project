package it.polito.waii.orchestrator.dtos

class UpdateQuantityDtoKafka(
    var warehouseId: Long,
    var productId: Long,
    var quantity: Long,
    var action: Action
)