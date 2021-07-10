package it.polito.waii.warehouse_service.entities

import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.Id
import javax.persistence.OneToMany

@Entity
class Product(
    @Id
    @GeneratedValue
    var id: Long?,

    @OneToMany(mappedBy = "warehouse")
    var warehouses: Set<Warehouse>
)
