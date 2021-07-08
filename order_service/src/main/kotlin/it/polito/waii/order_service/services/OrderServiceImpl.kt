package it.polito.waii.order_service.services

import it.polito.waii.order_service.dtos.OrderDto
import it.polito.waii.order_service.entities.*
import it.polito.waii.order_service.repositories.OrderRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import reactor.core.publisher.Mono
import reactor.core.publisher.Flux

@Service
class OrderServiceImpl: OrderService {

    @Autowired
    lateinit var orderRepository: OrderRepository


    @Transactional
    override fun createOrder(orderDto: OrderDto): Mono<Long> {

        val customer = Customer(orderDto.buyerId)
        val products = orderDto.productIds.map { Product(it) }.toSet()

        return orderRepository
            .save(Order(null, customer, products, OrderStatus.ISSUED, orderDto.deliveries.map { Delivery(null, it.shippingAddress, Warehouse(it.warehouseId)) }.toSet()))
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
    override fun updateOrder(orderDto: OrderDto): Mono<Void> {
        return orderRepository
            .save(
                Order(
                    null,
                    Customer(orderDto.buyerId),
                    orderDto.productIds.map { Product(it) }.toSet(),
                    orderDto.status!!,
                    orderDto.deliveries.map { Delivery(null, it.shippingAddress, Warehouse(it.warehouseId)) }.toSet()
                )
            )
            .then()
    }

    @Transactional
    override fun deleteOrderById(id: Long): Mono<Void> {
        return orderRepository
            .deleteById(id)
    }

}