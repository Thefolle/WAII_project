package it.polito.waii.order_service.entities

import org.springframework.data.neo4j.core.schema.Id
import org.springframework.data.neo4j.core.schema.Node

@Node
class Wallet(
    @Id
    val id: Long?
)