package it.polito.waii.order_service.entities

import it.polito.waii.order_service.dtos.OrderDto
import it.polito.waii.order_service.services.IdGenerator
import org.springframework.data.annotation.Version
import org.springframework.data.neo4j.core.schema.GeneratedValue
import org.springframework.data.neo4j.core.schema.Id
import org.springframework.data.neo4j.core.schema.Node
import org.springframework.data.neo4j.core.schema.Relationship

@Node
data class Order(
    @Id
    @GeneratedValue(IdGenerator::class)
    val id: Long?,
    var buyer: Customer,
    val wallet: Wallet,
    val deliveries: Set<Delivery>,
    var total: Float,
    var status: OrderStatus,
) {


    fun toDto(): OrderDto = OrderDto(
        id,
        buyer.id!!,
        wallet.id!!,
        deliveries.associate { it.product.id!! to it.toDto() },
        deliveries.associate { it.product.id!! to it.quantity },
        total,
        false,
        status
    )

    fun withBuyer(buyer: Customer) : Order = Order(
        this.id,
        buyer,
        this.wallet,
        this.deliveries,
        this.total,
        this.status
    )

    fun withDeliveries(deliveries: Set<Delivery>) : Order = Order(
        this.id,
        this.buyer,
        this.wallet,
        deliveries,
        this.total,
        this.status
    )

    fun withTotal(total: Float) : Order = Order(
        this.id,
        this.buyer,
        this.wallet,
        this.deliveries,
        total,
        this.status
    )
}