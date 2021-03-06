package it.polito.waii.order_service.entities

import org.springframework.data.annotation.Version
import org.springframework.data.neo4j.core.schema.GeneratedValue
import org.springframework.data.neo4j.core.schema.Id
import org.springframework.data.neo4j.core.schema.Node

@Node
data class Product(
    @Id
    val id: Long?
)
