package it.polito.waii.order_service.repositories

import it.polito.waii.order_service.entities.Product
import org.springframework.data.neo4j.repository.ReactiveNeo4jRepository

interface ProductRepository: ReactiveNeo4jRepository<Product, Long> {
}