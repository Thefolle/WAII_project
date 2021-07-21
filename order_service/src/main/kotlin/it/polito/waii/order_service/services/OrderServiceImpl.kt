package it.polito.waii.order_service.services

import it.polito.waii.order_service.dtos.DeliveryDto
import it.polito.waii.order_service.dtos.OrderDto
import it.polito.waii.order_service.dtos.PatchOrderDto
import it.polito.waii.order_service.entities.*
import it.polito.waii.order_service.exceptions.UnsatisfiableRequestException
import it.polito.waii.order_service.repositories.OrderRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.apache.kafka.common.errors.TimeoutException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.core.ParameterizedTypeReference
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.requestreply.KafkaReplyTimeoutException
import org.springframework.kafka.requestreply.ReplyingKafkaTemplate
import org.springframework.kafka.support.KafkaHeaders
import org.springframework.messaging.support.MessageBuilder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import reactor.core.publisher.Mono
import reactor.core.publisher.Flux
import java.nio.ByteBuffer
import java.nio.charset.Charset

@Service
class OrderServiceImpl : OrderService {

    @Autowired
    lateinit var orderRepository: OrderRepository

    @Autowired
    @Qualifier("orderDtoLong2ReplyingKafkaTemplate")
    lateinit var orderDtoLongReplyingKafkaTemplate: ReplyingKafkaTemplate<String, OrderDto, Long>

    var i: Long = 0

    @Transactional
    override fun createOrder(orderDto: OrderDto): Mono<Long> {

        val replyPartition = ByteBuffer.allocate(Int.SIZE_BYTES)
        replyPartition.putInt(0)
        val correlationId = ByteBuffer.allocate(Int.SIZE_BYTES)
        correlationId.putInt(0)

        val future = orderDtoLongReplyingKafkaTemplate
            .sendAndReceive(
                MessageBuilder
                    .withPayload(
                        orderDto
                    )
                    .setHeader(KafkaHeaders.TOPIC, "orchestrator_requests")
                    .setHeader(KafkaHeaders.PARTITION_ID, 0)
                    .setHeader(KafkaHeaders.MESSAGE_KEY, "key1")
                    .setHeader(
                        KafkaHeaders.REPLY_TOPIC,
                        "orchestrator_responses".toByteArray(Charset.defaultCharset())
                    )

                    .setHeader(KafkaHeaders.REPLY_PARTITION, replyPartition.array())
                    .setHeader(KafkaHeaders.CORRELATION_ID, correlationId.array())
                    .build(),
                ParameterizedTypeReference.forType<Long>(Long::class.java)
            )
        val futureResult = future.get().payload



        val customer = Customer(orderDto.buyerId)
        val wallet = Wallet(orderDto.walletId)
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

//        if (futureResult == 1L) throw UnsatisfiableRequestException("The warehouse is full")

        return orderRepository
            .save(Order(i++, customer, wallet, deliveries, orderDto.total, OrderStatus.ISSUED))
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

        val isCustomerChanged = orderDto.buyerId != oldOrder.buyer.id
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

        val isWalletChanged = orderDto.walletId != oldOrder.wallet.id
        val oldWalletId = oldOrder.buyer.id
        val newWalletId = orderDto.buyerId
        if (isWalletChanged) {
            orderRepository
                .detachWallet(orderDto.id, oldWalletId!!)
                .awaitSingleOrNull()
            orderRepository
                .deleteWalletIfIsolated(oldWalletId)
                .awaitSingleOrNull()
        }

        orderRepository
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
    }

    @Transactional
    override fun deleteOrderById(id: Long): Mono<Void> {
        return orderRepository
            .deleteById(id)
    }

}