package it.polito.waii.order_service.repositories

import it.polito.waii.order_service.entities.Order
import org.springframework.data.neo4j.repository.ReactiveNeo4jRepository
import org.springframework.data.neo4j.repository.query.Query
import org.springframework.data.repository.query.Param
import reactor.core.publisher.Mono

interface OrderRepository: ReactiveNeo4jRepository<Order, Long> {

    // Cannot infer a general query named deleteIfIsolated, regardless of the type of the node,
    // because id is a local property of the node, unlike the internal auto-generated id
    @Query(
        "match (w:Warehouse {id: \$id})\n" +
                "where not (w)-[]-()\n" +
                "delete w"
    )
    fun deleteWarehouseIfIsolated(@Param("id") id: Long): Mono<Void>

    @Query(
        "match (d {id: \$deliveryId})-[r]-(w:Warehouse {id: \$warehouseId})\n" +
                "delete r"
    )
    fun detachWarehouse(@Param("deliveryId") deliveryId: Long, @Param("warehouseId") warehouseId: Long): Mono<Void>

    @Query(
        "match (p:Product {id: \$id})\n" +
                "where not (p)-[]-()\n" +
                "delete p"
    )
    fun deleteProductIfIsolated(@Param("id") id: Long): Mono<Void>

    @Query(
        "match (d {id: \$deliveryId})-[r]-(p:Product {id: \$productId})\n" +
                "delete r"
    )
    fun detachProduct(@Param("deliveryId") deliveryId: Long, @Param("productId") productId: Long): Mono<Void>

    @Query(
        "match (c:Customer {id: \$id})\n" +
                "where not (p)-[]-()\n" +
                "delete c"
    )
    fun deleteCustomerIfIsolated(@Param("id") id: Long): Mono<Void>

    @Query(
        "match (d {id: \$deliveryId})-[r]-(c:Customer {id: \$customerId})\n" +
                "delete r"
    )
    fun detachCustomer(@Param("deliveryId") deliveryId: Long, @Param("customerId") customerId: Long): Mono<Void>

    @Query(
        "match (w:Wallet {id: \$id})\n" +
                "where not (p)-[]-()\n" +
                "delete w"
    )
    fun deleteWalletIfIsolated(@Param("id") id: Long): Mono<Void>

    @Query(
        "match (d {id: \$deliveryId})-[r]-(w:Wallet {id: \$walletId})\n" +
                "delete r"
    )
    fun detachWallet(@Param("deliveryId") deliveryId: Long, @Param("walletId") walletId: Long): Mono<Void>


    @Query(
        "match (o:Order {id: \$id})\n" +
                "match (o)-[:BUYER]->(customer:Customer)\n" +
                "match (o)-[:DELIVERIES]->(delivery:Delivery)-[:WAREHOUSE]->(warehouse:Warehouse)\n" +
                "match (delivery)-[:PRODUCT]->(product:Product)\n" +
                "detach delete o, delivery\n" +
                "\n" +
                "with product, customer, warehouse\n" +
                "\n" +
                "// delete product if they have just become isolated nodes\n" +
                "match (product)\n" +
                "where not (product)-[]-()\n" +
                "delete product\n" +
                "\n" +
                "with customer, warehouse\n" +
                "\n" +
                "match (customer)\n" +
                "where not (customer)-[]-()\n" +
                "delete customer\n" +
                "\n" +
                "with warehouse\n" +
                "\n" +
                "match (warehouse)\n" +
                "where not (warehouse)-[]-()\n" +
                "delete warehouse"
    )
    override fun deleteById(@Param("id") id: Long): Mono<Void>

}