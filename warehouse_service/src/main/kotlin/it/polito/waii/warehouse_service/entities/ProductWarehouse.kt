package it.polito.waii.warehouse_service.entities

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
    var warehouse: Warehouse
)