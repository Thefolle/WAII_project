package it.polito.waii.order_service.entities

import it.polito.waii.order_service.dtos.OrderDto
import org.springframework.data.neo4j.core.schema.GeneratedValue
import org.springframework.data.neo4j.core.schema.Id
import org.springframework.data.neo4j.core.schema.Node
import org.springframework.data.neo4j.core.schema.Relationship

@Node
data class Order(
    @Id
    @GeneratedValue
    val id: Long?,
    @Relationship(type = "PLACED_BY", direction = Relationship.Direction.OUTGOING)
    val buyer: Customer,
    @Relationship(value = "CONTAINS_PRODUCT", direction = Relationship.Direction.OUTGOING)
    val products: Set<Product>,
    val status: OrderStatus,
    val deliveries: Set<Delivery>
) {
    fun toDto(): OrderDto = OrderDto(
        id,
        buyer.id!!,
        products.map { it.id!! }.toSet(),
        deliveries.map { it.toDto() }.toSet()
    )
}