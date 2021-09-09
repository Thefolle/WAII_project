package it.polito.waii.warehouse_service.entities

import it.polito.waii.warehouse_service.dtos.WarehouseDto
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.Id
import javax.persistence.OneToMany
import javax.validation.constraints.Min

@Entity
class Warehouse(
    @Id
    @GeneratedValue
    var id: Long?,
    var name: String,
    var city: String,
    var region: String,
    var capacity: Long,

    @OneToMany(mappedBy = "product")
    var products: MutableSet<ProductWarehouse>?
) {
    fun toDto(): WarehouseDto = WarehouseDto(
        id,
        name,
        city,
        region,
        capacity
    )
}
