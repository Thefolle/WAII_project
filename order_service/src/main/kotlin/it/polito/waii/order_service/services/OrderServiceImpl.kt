package it.polito.waii.order_service.services

import it.polito.waii.order_service.dtos.InputOrderDto
import it.polito.waii.order_service.dtos.OrderDto
import it.polito.waii.order_service.dtos.OrderDtoOrchestrator
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
import org.springframework.http.HttpStatus
import org.springframework.kafka.requestreply.ReplyingKafkaTemplate
import org.springframework.kafka.support.KafkaHeaders
import org.springframework.messaging.Message
import org.springframework.messaging.handler.annotation.Header
import org.springframework.messaging.support.MessageBuilder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import reactor.core.publisher.Mono
import reactor.core.publisher.Flux
import java.lang.NullPointerException
import java.nio.ByteBuffer
import java.time.Duration
import java.util.concurrent.TimeUnit

@Service
class OrderServiceImpl : OrderService {

    @Autowired
    lateinit var orderRepository: OrderRepository

    @Autowired
    @Qualifier("createOrderToOrchestratorReplyingKafkaTemplate")
    lateinit var orderDtoLongReplyingKafkaTemplate: ReplyingKafkaTemplate<String, OrderDtoOrchestrator, Float>

    @Transactional
    override suspend fun createOrder(orderDto: InputOrderDto, username: String, roles: String): Long {

        if (orderDto.deliveries.keys != orderDto.quantities.keys) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "The set of products in deliveries must equal the set " +
                    "of products in quantities.")
        }

        // process the request
        val customer = Customer(username)
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

        // request orchestration
        val replyPartition = ByteBuffer.allocate(Int.SIZE_BYTES)
        replyPartition.putInt(0)
        val correlationId = ByteBuffer.allocate(Int.SIZE_BYTES)
        correlationId.putInt(0)

        val newOrder = Order(null, customer, wallet, deliveries, 0f, OrderStatus.ISSUED)
        val orderDtoOrchestrator = newOrder.toDto().toOrderDtoOrchestrator(true)

        val future =
            orderDtoLongReplyingKafkaTemplate
                .sendAndReceive(
                    MessageBuilder
                        .withPayload(
                            orderDtoOrchestrator
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
                    ParameterizedTypeReference.forType<Float>(Float::class.java)
                )

        var totalPrice: Float
        var response : Message<*>
        try {
            response = future
                .get()
            totalPrice = response.payload
        } catch (exception: Exception) {
            throw ResponseStatusException(HttpStatus.REQUEST_TIMEOUT, "The request cannot be processed due to some " +
                    "malfunction. Please, try later.")
        }

        if ((response.headers["hasException"] as Boolean)) {
            throw ResponseStatusException(
                HttpStatus.valueOf(response.headers["exceptionRawStatus"] as Int),
                response.headers["exceptionMessage"] as String
            )
        }

        newOrder.total = totalPrice

        return orderRepository
            .save(newOrder)
            .map { it.id }
            .awaitSingle()!!

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
    override suspend fun getOrderById(id: Long): OrderDto {
        if (!orderRepository.existsById(id).awaitSingle()) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "No order with id $id exists.")
        }

        return orderRepository
            .findById(id)
            .awaitSingle()
            .toDto()
    }

    private fun updateOrderStatus(orderDto: PatchOrderDto, oldOrder: Order) {
        if ((orderDto.status == OrderStatus.CANCELED || orderDto.status == OrderStatus.FAILED ) && oldOrder.status != OrderStatus.ISSUED) {
            throw ResponseStatusException(
                HttpStatus.FORBIDDEN,
                "The order cannot be updated to ${orderDto.status.toString().lowercase()} anymore, " +
                        "since it is ${oldOrder.status.toString().lowercase()}."
            )
        } else if (oldOrder.status == OrderStatus.DELIVERING && orderDto.status != OrderStatus.DELIVERED) {
            throw ResponseStatusException(
                HttpStatus.FORBIDDEN,
                "The order cannot be updated to ${orderDto.status.toString().lowercase()}, since shipping has already started."
            )
        } else if (oldOrder.status == OrderStatus.DELIVERED ||
            oldOrder.status == OrderStatus.CANCELED ||
            oldOrder.status == OrderStatus.FAILED) {
            throw ResponseStatusException(
                HttpStatus.FORBIDDEN,
                "The order cannot be updated to ${orderDto.status.toString().lowercase()}, since it is already ${oldOrder.status.toString().lowercase()}."
            )
        } else if (oldOrder.status == OrderStatus.ISSUED && orderDto.status == OrderStatus.DELIVERED) {
            throw ResponseStatusException(
                HttpStatus.FORBIDDEN,
                "The order cannot be updated to delivered, since shipping has not started yet."
            )
        }
        oldOrder.status = orderDto.status!!
    }

    @Transactional
    override suspend fun updateOrder(orderDto: PatchOrderDto, username: String, roles: String) {

        var oldOrder: Order
        try {
            oldOrder =
                orderRepository
                    .findById(orderDto.id!!)
                    .awaitSingle()
        } catch (exception: NoSuchElementException) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "No order with id ${orderDto.id} exists.")
        }

        // check status
        if (orderDto.status != null && orderDto.status != oldOrder.status) {
            if (orderDto.walletId != null || orderDto.deliveries != null || orderDto.quantities != null) {
                throw ResponseStatusException(HttpStatus.BAD_REQUEST, "You cannot modify the order's status and its data" +
                        " at the same time!")
            }
            updateOrderStatus(orderDto, oldOrder)
            // if the status has been correctly modified, data cannot be changed
            if (oldOrder.status != OrderStatus.ISSUED) {
                orderRepository
                    .save(
                        oldOrder
                    )
                    .awaitSingle()

                if (oldOrder.status == OrderStatus.FAILED || oldOrder.status == OrderStatus.CANCELED) {
                    propagateUpdateMoneyAndProducts(oldOrder, username, roles, false)
                }

                return
            }
        }
        // cannot modify data if status is not issued
        if (oldOrder.status != OrderStatus.ISSUED) {
            throw ResponseStatusException(HttpStatus.FORBIDDEN,
                "The order data cannot be updated anymore, since it is ${oldOrder.status.toString().lowercase()}")
        }

        var deliveries = orderDto.deliveries?.map {
            val isNew = oldOrder.deliveries.none { delivery -> delivery.product.id == it.key }
            if (isNew) {
                try {
                    Delivery(
                        null,
                        it.value.shippingAddress!!,
                        Warehouse(it.value.warehouseId!!),
                        Product(it.key),
                        orderDto.quantities!![it.key]!!
                    )
                } catch (exception: Exception) {
                    throw ResponseStatusException(HttpStatus.BAD_REQUEST, "New deliveries must contain all fields: shippingAddress, warehouseId and quantity of the specified product.")
                }

            } else {
                var oldDelivery: Delivery
                try {
                    oldDelivery = oldOrder.deliveries.find { oldDelivery -> oldDelivery.product.id == it.key }!!
                } catch (exception: Exception) {
                    // never thrown
                    throw ResponseStatusException(HttpStatus.NOT_FOUND, "No delivery for the product with " +
                            "id ${it.key} exists.")
                }

                val isWarehouseChanged = it.value.warehouseId != null
                val oldWarehouseId = oldDelivery.warehouse.id
                val newWarehouseId = it.value.warehouseId
                if (isWarehouseChanged) {
                    orderRepository
                        .detachWarehouse(oldDelivery.id!!, oldWarehouseId!!)
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
                        .detachProduct(oldDelivery.id!!, oldProductId!!)
                        .awaitSingleOrNull()
                    orderRepository
                        .deleteProductIfIsolated(oldProductId)
                        .awaitSingleOrNull()
                }

                val delivery = Delivery(
                    oldDelivery.id!!,
                    it.value.shippingAddress ?: oldDelivery.shippingAddress,
                    Warehouse(if (isWarehouseChanged) newWarehouseId else oldWarehouseId),
                    Product(if (isProductChanged) newProductId else oldProductId),
                    orderDto.quantities?.get(it.key) ?: oldDelivery.quantity
                )

                delivery
            }
        }
            ?.toSet()

        var otherDeliveries = orderDto.quantities?.map {
            val wasNew = oldOrder.deliveries.none { delivery -> delivery.product.id == it.key }
            if (wasNew) {
                null
            } else {
                val oldDelivery: Delivery?
                try {
                    oldDelivery = oldOrder.deliveries.find { oldDelivery -> oldDelivery.product.id == it.key }
                } catch (exception: Exception) {
                    // never thrown
                    throw ResponseStatusException(HttpStatus.NOT_FOUND, "No delivery for the product with " +
                            "id ${it.key} exists.")
                }

                val delivery = Delivery(
                    oldDelivery!!.id!!,
                    oldDelivery.shippingAddress,
                    oldDelivery.warehouse,
                    oldDelivery.product,
                    it.value
                )

                delivery
            }

        }
            ?.filterNotNull()?.toSet()

        deliveries = otherDeliveries?.plus(deliveries ?: setOf()) ?: deliveries

        val untouchedDeliveries = oldOrder.deliveries.filter { oldDelivery -> deliveries?.none { oldDelivery.id == it.id } ?: false }
        deliveries = deliveries?.plus(untouchedDeliveries)


        val isWalletChanged = orderDto.walletId != null
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

        var newOrder =
            Order(
                orderDto.id,
                oldOrder.buyer,
                Wallet(if (isWalletChanged) newWalletId else oldWalletId),
                deliveries ?: oldOrder.deliveries,
                oldOrder.total, // this value will remain the same if only the order status changed
                orderDto.status ?: oldOrder.status
            )

        val orphanQuantities = orderDto.quantities?.keys?.filter { productId -> newOrder.deliveries.none { delivery -> delivery.product.id == productId } }
        if (orphanQuantities?.isNotEmpty() == true) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "You have specified quantities for products that " +
                    "you haven't bought. The associated product ids are: ${orphanQuantities.joinToString()}.")
        }


        if (isWalletChanged || deliveries != null) {
            // deposit into the old wallet and bring back products into the old warehouse
            propagateUpdateMoneyAndProducts(oldOrder, username, roles, false)

            // withdraw from the new wallet and take products from the new warehouse
            val newPrice = propagateUpdateMoneyAndProducts(newOrder, username, roles, true)

            newOrder.total = newPrice

        }

        orderRepository
            .save(
                newOrder
            )
            .awaitSingle()
    }

    private fun propagateUpdateMoneyAndProducts(oldOrder: Order, username: String, roles: String, takeOrPut: Boolean): Float {
        val replyPartition = ByteBuffer.allocate(Int.SIZE_BYTES)
        replyPartition.putInt(0)
        val correlationId = ByteBuffer.allocate(Int.SIZE_BYTES)
        correlationId.putInt(1)

        val orderToCancel = oldOrder.toDto().toOrderDtoOrchestrator(takeOrPut)

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
                    ParameterizedTypeReference.forType<Float>(Float::class.java)
                )

        var response: Message<Float>
        try {
            response = future
                .get()
        } catch (exception: Exception) {
            throw ResponseStatusException(
                HttpStatus.REQUEST_TIMEOUT,
                "The order couldn't be updated due to some malfunction. Please, try later."
            )
        }

        if ((response.headers["hasException"] as Boolean)) {
            throw ResponseStatusException(
                HttpStatus.valueOf(response.headers["exceptionRawStatus"] as Int),
                response.headers["exceptionMessage"] as String
            )
        }

        return response.payload
    }

    @Transactional
    override suspend fun deleteOrderById(id: Long, username: String, roles: String) {

        updateOrder(
            PatchOrderDto(
                id,
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