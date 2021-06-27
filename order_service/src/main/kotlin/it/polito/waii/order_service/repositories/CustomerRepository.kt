package it.polito.waii.order_service.repositories

import it.polito.waii.order_service.entities.Customer
import org.springframework.data.neo4j.repository.ReactiveNeo4jRepository
import org.springframework.stereotype.Repository

@Repository
interface CustomerRepository: ReactiveNeo4jRepository<Customer, Long> {
}