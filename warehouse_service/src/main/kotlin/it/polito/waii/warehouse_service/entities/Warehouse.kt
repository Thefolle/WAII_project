package it.polito.waii.warehouse_service.entities

import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.Id
import javax.persistence.OneToMany

@Entity
class Warehouse(
    @Id
    @GeneratedValue
    var id: Long?,
    var name: String,
    var city: String,
    var region: String,
    // expressed in m^3
    var capacity: Long,
    // current availability is computed on the fly

    @OneToMany(mappedBy = "product")
    var products: MutableSet<ProductWarehouse>
)
