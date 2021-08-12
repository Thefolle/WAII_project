package it.polito.waii.order_service.services

import org.springframework.data.neo4j.core.schema.IdGenerator

class IdGenerator: IdGenerator<Long> {

    // time is naturally rising in a monotone way; thus, the initialization avoids conflicts with ids generated
    // in previous sessions of the module
    private var timestamp: Long = System.currentTimeMillis()

    // assume this function is automatically synchronized by Neo4j among calling threads
    override fun generateId(primaryLabel: String, entity: Any): Long {
        return timestamp++
    }

}