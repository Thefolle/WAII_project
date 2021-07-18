package it.polito.waii.order_service.services

import it.polito.waii.order_service.dtos.OrderDto
import it.polito.waii.order_service.dtos.PatchOrderDto
import it.polito.waii.order_service.entities.*
import it.polito.waii.order_service.repositories.OrderRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import reactor.core.publisher.Mono
import reactor.core.publisher.Flux

@Service
class OrderServiceImpl: OrderService {

    @Autowired
    lateinit var orderRepository: OrderRepository

    var i: Long = 0


    @Transactional
    override fun createOrder(orderDto: OrderDto): Mono<Long> {

        val customer = Customer(orderDto.buyerId)
        val products = orderDto.deliveries.keys.map { Product(it) }.toSet()
        val deliveries = orderDto.deliveries.map {
            Delivery(
                i++,
                it.value.shippingAddress,
                Warehouse(it.value.warehouseId),
                products.first { innerIt -> innerIt.id == it.key },
                orderDto.quantities[it.key]!!
            )
        }
            .toSet()

        return orderRepository
            .save(Order(i++, customer, deliveries, orderDto.total, OrderStatus.ISSUED))
            .map { it.id }
    }

    @Transactional
    override fun getOrders(): Flux<OrderDto> {
        return orderRepository
            .findAll()
            .map {
                it.toDto()
            }
    }

    @Transactional
    override fun getOrderById(id: Long): Mono<OrderDto> {
        return orderRepository
            .findById(id)
            .map { it.toDto() }
    }

    @Transactional
    override suspend fun updateOrder(orderDto: PatchOrderDto): Order = coroutineScope {

        val oldOrder =
            orderRepository
                .findById(orderDto.id!!)
                .awaitSingle()

        val deliveries = orderDto.deliveries?.map {
            val isNew = it.value.id == null
            if (isNew) {
                Delivery(
                    null,
                    it.value.shippingAddress!!,
                    Warehouse(it.value.warehouseId!!),
                    Product(it.key),
                    orderDto.quantities!![it.key]!!
                )
            } else {
                val oldDelivery = oldOrder.deliveries.find { innerIt -> innerIt.id == it.value.id }!!

                val isWarehouseChanged = it.value.warehouseId != null
                val oldWarehouseId = oldDelivery.warehouse.id
                val newWarehouseId = it.value.warehouseId
                if (isWarehouseChanged) {
                    orderRepository
                        .detachWarehouse(it.value.id!!, oldWarehouseId!!)
                        .awaitSingleOrNull()
                    orderRepository
                        .deleteWarehouseIfIsolated(oldWarehouseId)
                        .awaitSingleOrNull()
                }

                val isProductChanged = it.key != oldDelivery.product.id
                val oldProductId = oldDelivery.product.id
                val newProductId = it.key
                if (isProductChanged) {
                    orderRepository
                        .detachProduct(it.value.id!!, oldProductId!!)
                        .awaitSingleOrNull()
                    orderRepository
                        .deleteProductIfIsolated(oldProductId)
                        .awaitSingleOrNull()
                }

                val delivery = Delivery(
                    it.value.id,
                    it.value.shippingAddress ?: oldDelivery.shippingAddress,
                    Warehouse(if (isWarehouseChanged) newWarehouseId else oldWarehouseId),
                    Product(if (isProductChanged) newProductId else oldProductId),
                    orderDto.quantities?.get(it.key) ?: oldDelivery.quantity
                )

                delivery
            }
        }
            ?.toSet()

        orderRepository
            .save(
                Order(
                    orderDto.id,
                    Customer(orderDto.buyerId ?: oldOrder.buyer.id),
                    deliveries ?: oldOrder.deliveries,
                    orderDto.total ?: oldOrder.total,
                    orderDto.status ?: oldOrder.status
                )
            )
            .awaitSingle()
    }

    @Transactional
    override fun deleteOrderById(id: Long): Mono<Void> {
        return orderRepository
            .deleteById(id)
    }

}