package it.polito.waii.warehouse_service.entities

import it.polito.waii.warehouse_service.dtos.ProductDTO
import it.polito.waii.warehouse_service.dtos.ProductWarehouseDTO
import javax.persistence.*

@Entity
class ProductWarehouse (
    @EmbeddedId
    var compositeKey: CompositeKey,

    @ManyToOne
    @MapsId("productId")
    @JoinColumn
    var product: Product,

    @ManyToOne
    @MapsId("warehouseId")
    @JoinColumn
    var warehouse: Warehouse,

    var quantity: Long,
    // alarm level is a threshold
    var alarmLevel: Long
) {
    fun toDTO(): ProductWarehouseDTO = ProductWarehouseDTO(
        productId = compositeKey.productId,
        warehouseId = compositeKey.warehouseId,
        quantity = quantity,
        alarmLevel = alarmLevel
    )
}
