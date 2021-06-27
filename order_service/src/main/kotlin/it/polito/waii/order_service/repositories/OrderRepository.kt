package it.polito.waii.order_service.repositories

import it.polito.waii.order_service.entities.Order
import org.springframework.data.neo4j.repository.ReactiveNeo4jRepository

interface OrderRepository: ReactiveNeo4jRepository<Order, Long> {
}