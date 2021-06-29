package it.polito.waii.order_service.services

import it.polito.waii.order_service.dtos.OrderDto
import it.polito.waii.order_service.entities.Customer
import it.polito.waii.order_service.entities.Order
import it.polito.waii.order_service.entities.OrderStatus
import it.polito.waii.order_service.entities.Product
import it.polito.waii.order_service.repositories.CustomerRepository
import it.polito.waii.order_service.repositories.OrderRepository
import it.polito.waii.order_service.repositories.ProductRepository
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import reactor.core.publisher.Mono
import kotlinx.coroutines.launch
import kotlinx.coroutines.reactive.awaitLast
import kotlinx.coroutines.reactor.awaitSingle
import kotlin.coroutines.EmptyCoroutineContext

@Service
class OrderServiceImpl: OrderService {

    @Autowired
    lateinit var customerRepository: CustomerRepository

    @Autowired
    lateinit var productRepository: ProductRepository

    @Autowired
    lateinit var orderRepository: OrderRepository

    @Transactional
    override suspend fun createOrder(orderDto: OrderDto): Mono<Long> = coroutineScope {

        val exists = customerRepository.existsById(orderDto.buyerId).awaitSingle()
        val customer = if (!exists) customerRepository.save(Customer(orderDto.buyerId)).awaitSingle()
        else customerRepository.findById(orderDto.buyerId).awaitSingle()


//        var products = setOf<Product>()
//        productRepository
//            .saveAll(orderDto.productIds.map { Product(it) })
//            .doOnNext { products.plus(it) }
//            .awaitLast()
        val products = orderDto.productIds.map { Product(it) }.toSet()

        orderRepository.save(Order(null, customer, products, OrderStatus.ISSUED)).map { it.id }
    }

}