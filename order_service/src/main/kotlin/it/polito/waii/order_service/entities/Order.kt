package it.polito.waii.order_service.entities

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
    val status: OrderStatus
)