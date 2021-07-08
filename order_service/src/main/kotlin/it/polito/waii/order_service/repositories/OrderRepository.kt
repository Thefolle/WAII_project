package it.polito.waii.order_service.repositories

import it.polito.waii.order_service.entities.Order
import org.springframework.data.neo4j.repository.ReactiveNeo4jRepository
import org.springframework.data.neo4j.repository.query.Query
import org.springframework.data.repository.query.Param
import reactor.core.publisher.Mono

interface OrderRepository: ReactiveNeo4jRepository<Order, Long> {

    @Query(
        "match (o:Order) where id(o)=\$id\n" +
                "match (o)-[:PRODUCTS]->(products:Product)\n" +
                "match (o)-[:BUYER]->(customer:Customer)\n" +
                "match (o)-[:DELIVERIES]->(deliveries:Delivery)-[:WAREHOUSE]->(warehouses:Warehouse)\n" +
                "detach delete o, deliveries\n" +
                "\n" +
                "with products, customer, warehouses\n" +
                "\n" +
                "// delete products if they have just become isolated nodes\n" +
                "match (products)\n" +
                "where not (products)<-[:PRODUCTS]-()\n" +
                "delete products\n" +
                "\n" +
                "with customer, warehouses\n" +
                "\n" +
                "match (customer)\n" +
                "where not (customer)<-[:BUYER]-()\n" +
                "delete customer\n" +
                "\n" +
                "with warehouses\n" +
                "\n" +
                "match (warehouses)\n" +
                "where not (warehouses)<-[:WAREHOUSE]-()\n" +
                "delete warehouses"
    )
    override fun deleteById(@Param("id") id: Long): Mono<Void>

}