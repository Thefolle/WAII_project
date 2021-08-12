package it.polito.waii.order_service.entities

import it.polito.waii.order_service.dtos.DeliveryDto
import it.polito.waii.order_service.services.IdGenerator
import org.springframework.data.annotation.Version
import org.springframework.data.neo4j.core.schema.GeneratedValue
import org.springframework.data.neo4j.core.schema.Id
import org.springframework.data.neo4j.core.schema.Node

@Node
data class Delivery(
    @Id
    @GeneratedValue(IdGenerator::class)
    val id: Long?,
    val shippingAddress: String,
    val warehouse: Warehouse,
    val product: Product,
    val quantity: Long
) {

    fun toDto() = DeliveryDto(
        id,
        shippingAddress,
        warehouse.id!!
    )
}
