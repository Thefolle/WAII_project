package it.polito.waii.order_service.services

import it.polito.waii.order_service.dtos.OrderDto
import it.polito.waii.order_service.entities.Customer
import it.polito.waii.order_service.repositories.CustomerRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import reactor.core.publisher.Mono

@Service
class OrderServiceImpl: OrderService {

    @Autowired
    lateinit var customerRepository: CustomerRepository

    @Transactional
    override fun createOrder(orderDto: OrderDto): Mono<Long> {
        var customer = Customer()
        return customerRepository.save(customer).map { it.id }
    }


}