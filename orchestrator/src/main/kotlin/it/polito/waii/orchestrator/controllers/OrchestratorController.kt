package it.polito.waii.orchestrator.controllers

import it.polito.waii.orchestrator.dtos.Action
import it.polito.waii.orchestrator.dtos.OrderDtoOrchestrator
import it.polito.waii.orchestrator.dtos.TransactionDto
import it.polito.waii.orchestrator.dtos.UpdateQuantityDtoKafka
import it.polito.waii.orchestrator.exceptions.UnsatisfiableRequestException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.annotation.TopicPartition
import org.springframework.kafka.requestreply.ReplyingKafkaTemplate
import org.springframework.kafka.support.KafkaHeaders
import org.springframework.messaging.handler.annotation.SendTo
import org.springframework.messaging.support.MessageBuilder
import org.springframework.stereotype.Component
import java.nio.ByteBuffer

@Component
class OrchestratorController {

    @Autowired
    lateinit var warehouseReplyingKafkaTemplate: ReplyingKafkaTemplate<String, UpdateQuantityDtoKafka, Long>

    @Autowired
    lateinit var walletReplyingKafkaTemplate: ReplyingKafkaTemplate<String, TransactionDto, Long>

    @SendTo("orchestrator_responses")
    @KafkaListener(
        containerFactory = "createOrderConcurrentKafkaListenerContainerFactory",
        topicPartitions = [TopicPartition(topic = "orchestrator_requests", partitions = ["0"])]
    )
    fun createOrder(orderDtoOrchestrator: OrderDtoOrchestrator): Long {

        // warehouse check
        val replyPartition = ByteBuffer.allocate(Int.SIZE_BYTES)
        replyPartition.putInt(0)
        val correlationId = ByteBuffer.allocate(Int.SIZE_BYTES)
        correlationId.putInt(0)

        println("orchestrator received")
        warehouseReplyingKafkaTemplate
            .sendAndReceive(
                MessageBuilder
                    .withPayload(
                        UpdateQuantityDtoKafka(
                            orderDtoOrchestrator.deliveries.values.elementAt(0).warehouseId,
                            orderDtoOrchestrator.deliveries.keys.elementAt(0),
                            orderDtoOrchestrator.quantities[orderDtoOrchestrator.deliveries.keys.elementAt(0)]!!,
                            Action.REMOVE
                        )
                    )
                    .setHeader(KafkaHeaders.TOPIC, "warehouse_service_requests")
                    .setHeader(KafkaHeaders.PARTITION_ID, 0)
                    .setHeader(KafkaHeaders.CORRELATION_ID, correlationId.array())
                    .setHeader(KafkaHeaders.MESSAGE_KEY, "key1")
                    .setHeader(KafkaHeaders.REPLY_TOPIC, "warehouse_service_responses")
                    .setHeader(KafkaHeaders.REPLY_PARTITION, replyPartition.array())
                    .build()
            )
            .get()
            .payload


        // wallet check
        val replyPartitionWarehouse = ByteBuffer.allocate(Int.SIZE_BYTES)
        replyPartition.putInt(0)
        val correlationIdWarehouse = ByteBuffer.allocate(Int.SIZE_BYTES)
        correlationId.putInt(1)

        println("orchestrator received")
        walletReplyingKafkaTemplate
            .sendAndReceive(
                MessageBuilder
                    .withPayload(
                        TransactionDto(
                            null,
                            orderDtoOrchestrator.walletId,
                            orderDtoOrchestrator.total,
                            null,
                            false,
                            // for now this is null: modify order_service to first save the order, as Saga requires
                            orderDtoOrchestrator.id,
                            null
                        )
                    )
                    .setHeader(KafkaHeaders.TOPIC, "wallet_service_requests")
                    .setHeader(KafkaHeaders.PARTITION_ID, 0)
                    .setHeader(KafkaHeaders.CORRELATION_ID, correlationIdWarehouse.array())
                    .setHeader(KafkaHeaders.MESSAGE_KEY, "key1")
                    .setHeader(KafkaHeaders.REPLY_TOPIC, "wallet_service_responses")
                    .setHeader(KafkaHeaders.REPLY_PARTITION, replyPartitionWarehouse.array())
                    .build()
            )
            .get()
            .payload

        return 1
        // check warehouse availability as warehouse.capacity - sum(product.quantity) >= 0
        // check wallet balance against the order's total price
        // perform transactions
//        throw UnsatisfiableRequestException("The warehouse is full")
    }

}