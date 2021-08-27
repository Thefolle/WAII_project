package it.polito.waii.order_service.services

import it.polito.waii.order_service.dtos.OrderDto
import it.polito.waii.order_service.dtos.PatchOrderDto
import it.polito.waii.order_service.entities.*
import it.polito.waii.order_service.exceptions.UnsatisfiableRequestException
import it.polito.waii.order_service.repositories.OrderRepository
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.core.ParameterizedTypeReference
import org.springframework.kafka.requestreply.ReplyingKafkaTemplate
import org.springframework.kafka.support.KafkaHeaders
import org.springframework.messaging.handler.annotation.Header
import org.springframework.messaging.support.MessageBuilder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import reactor.core.publisher.Mono
import reactor.core.publisher.Flux
import java.nio.ByteBuffer
import java.time.Duration

@Service
class OrderServiceImpl : OrderService {

    @Autowired
    lateinit var orderRepository: OrderRepository

    @Autowired
    @Qualifier("createOrderToOrchestratorReplyingKafkaTemplate")
    lateinit var orderDtoLongReplyingKafkaTemplate: ReplyingKafkaTemplate<String, OrderDto, Long>


    @Transactional
    override suspend fun createOrder(orderDto: OrderDto, username: String, roles: String): Long = coroutineScope {

        // process the request
        val customer = Customer(orderDto.buyerId)
        val wallet = Wallet(orderDto.walletId)
        val products = orderDto.deliveries.keys.map { Product(it) }.toSet()
        val deliveries = orderDto.deliveries.map {
            Delivery(
                null,
                it.value.shippingAddress,
                Warehouse(it.value.warehouseId),
                products.first { innerIt -> innerIt.id == it.key },
                orderDto.quantities[it.key]!!
            )
        }
            .toSet()

        var orderId =
            orderRepository
                .save(Order(null, customer, wallet, deliveries, orderDto.total, OrderStatus.ISSUED))
                .map { it.id }
                .awaitSingle()!!

        // request orchestration
        val replyPartition = ByteBuffer.allocate(Int.SIZE_BYTES)
        replyPartition.putInt(0)
        val correlationId = ByteBuffer.allocate(Int.SIZE_BYTES)
        correlationId.putInt(0)

        orderDto.isIssuingOrCancelling = true

        val future =
            orderDtoLongReplyingKafkaTemplate
                .sendAndReceive(
                    MessageBuilder
                        .withPayload(
                            orderDto
                        )
                        .setHeader(KafkaHeaders.TOPIC, "orchestrator_requests")
                        .setHeader(KafkaHeaders.PARTITION_ID, 0)
                        .setHeader(KafkaHeaders.MESSAGE_KEY, "key1")
                        .setHeader(KafkaHeaders.REPLY_TOPIC, "orchestrator_responses")
                        .setHeader(KafkaHeaders.REPLY_PARTITION, replyPartition.array())
                        .setHeader(KafkaHeaders.CORRELATION_ID, correlationId.array())
                        .setHeader("username", username)
                        .setHeader("roles", roles)
                        .build(),
                    Duration.ofSeconds(15),
                    ParameterizedTypeReference.forType<Long>(Long::class.java)
                )

        try {
            future
                .get()
        } catch (exception: Exception) {
            orderRepository
                .deleteById(orderId)
                .awaitSingleOrNull()
            orderId = -1L
            throw UnsatisfiableRequestException("The order couldn't be created because" +
                    " the following service didn't reply: orchestrator_service")
        }

        orderId
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
    override suspend fun updateOrder(orderDto: PatchOrderDto, username: String, roles: String): Order = coroutineScope {

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

        val isCustomerChanged = if (orderDto.buyerId != null) orderDto.buyerId != oldOrder.buyer.id else false
        val oldCustomerId = oldOrder.buyer.id
        val newCustomerId = orderDto.buyerId
        if (isCustomerChanged) {
            orderRepository
                .detachCustomer(orderDto.id, oldCustomerId!!)
                .awaitSingleOrNull()
            orderRepository
                .deleteCustomerIfIsolated(oldCustomerId)
                .awaitSingleOrNull()
        }

        val isWalletChanged = if (orderDto.walletId != null) orderDto.walletId != oldOrder.wallet.id else false
        val oldWalletId = oldOrder.wallet.id
        val newWalletId = orderDto.walletId
        if (isWalletChanged) {
            orderRepository
                .detachWallet(orderDto.id, oldWalletId!!)
                .awaitSingleOrNull()
            orderRepository
                .deleteWalletIfIsolated(oldWalletId)
                .awaitSingleOrNull()
        }

        if (orderDto.status == OrderStatus.CANCELED && oldOrder.status == OrderStatus.ISSUED) throw UnsatisfiableRequestException("The order cannot be deleted anymore.")

        val order = orderRepository
            .save(
                Order(
                    orderDto.id,
                    Customer(if (isCustomerChanged) newCustomerId else oldCustomerId),
                    Wallet(if (isWalletChanged) newWalletId else oldWalletId),
                    deliveries ?: oldOrder.deliveries,
                    orderDto.total ?: oldOrder.total,
                    orderDto.status ?: oldOrder.status
                )
            )
            .awaitSingle()



        if (orderDto.status == OrderStatus.CANCELED || orderDto.status == OrderStatus.FAILED || isWalletChanged || isCustomerChanged || deliveries != null || orderDto.total != null) {
            // restore wallet and warehouse as before issuing
            val replyPartition = ByteBuffer.allocate(Int.SIZE_BYTES)
            replyPartition.putInt(0)
            val correlationId = ByteBuffer.allocate(Int.SIZE_BYTES)
            correlationId.putInt(1)

            val orderToCancel = oldOrder.toDto()
            orderToCancel.isIssuingOrCancelling = false

            // deposit into the old wallet and bring back products into the old warehouse
            var future =
                orderDtoLongReplyingKafkaTemplate
                    .sendAndReceive(
                        MessageBuilder
                            .withPayload(
                                orderToCancel
                            )
                            .setHeader(KafkaHeaders.TOPIC, "orchestrator_requests")
                            .setHeader(KafkaHeaders.PARTITION_ID, 0)
                            .setHeader(KafkaHeaders.MESSAGE_KEY, "key1")
                            .setHeader(KafkaHeaders.REPLY_TOPIC, "orchestrator_responses")
                            .setHeader(KafkaHeaders.REPLY_PARTITION, replyPartition.array())
                            .setHeader(KafkaHeaders.CORRELATION_ID, correlationId.array())
                            .setHeader("username", username)
                            .setHeader("roles", roles)
                            .build(),
                        Duration.ofSeconds(15),
                        ParameterizedTypeReference.forType<Long>(Long::class.java)
                    )

            try {
                future
                    .get()
            } catch (exception: Exception) {
                throw UnsatisfiableRequestException("The order couldn't be updated because" +
                        " the following service didn't reply: orchestrator_service")
            }

            val orderToIssue = order.toDto()
            orderToIssue.isIssuingOrCancelling = true
            // withdraw from the new wallet and take products from the new warehouse
            future =
                orderDtoLongReplyingKafkaTemplate
                    .sendAndReceive(
                        MessageBuilder
                            .withPayload(
                                orderToIssue
                            )
                            .setHeader(KafkaHeaders.TOPIC, "orchestrator_requests")
                            .setHeader(KafkaHeaders.PARTITION_ID, 0)
                            .setHeader(KafkaHeaders.MESSAGE_KEY, "key1")
                            .setHeader(KafkaHeaders.REPLY_TOPIC, "orchestrator_responses")
                            .setHeader(KafkaHeaders.REPLY_PARTITION, replyPartition.array())
                            .setHeader(KafkaHeaders.CORRELATION_ID, correlationId.array())
                            .setHeader("username", username)
                            .setHeader("roles", roles)
                            .build(),
                        Duration.ofSeconds(15),
                        ParameterizedTypeReference.forType<Long>(Long::class.java)
                    )

            try {
                future
                    .get()
            } catch (exception: Exception) {
                throw UnsatisfiableRequestException("The order couldn't be updated because" +
                        " the following service didn't reply: orchestrator_service")
            }


        }

        order
    }

    @Transactional
    override suspend fun deleteOrderById(id: Long, username: String, roles: String): Unit = coroutineScope {

        updateOrder(
            PatchOrderDto(
                id,
                null,
                null,
                null,
                null,
                null,
                OrderStatus.CANCELED
            ),
            username,
            roles
        )

//        Alternatively, delete the order from the db
//        orderRepository
//            .deleteById(id)
//            .awaitSingle()
    }

}