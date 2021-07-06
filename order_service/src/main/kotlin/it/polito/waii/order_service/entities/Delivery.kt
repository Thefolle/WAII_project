package it.polito.waii.order_service.entities

import it.polito.waii.order_service.dtos.DeliveryDto
import org.springframework.data.neo4j.core.schema.GeneratedValue
import org.springframework.data.neo4j.core.schema.Id
import org.springframework.data.neo4j.core.schema.Node

@Node
data class Delivery(
    @Id
    @GeneratedValue
    val id: Long?,
    val shippingAddress: String,
    val warehouse: Warehouse
) {
    fun toDto() = DeliveryDto(
        shippingAddress, warehouse.id!!
    )
}
