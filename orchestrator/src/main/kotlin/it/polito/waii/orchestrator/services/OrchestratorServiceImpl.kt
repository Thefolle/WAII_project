package it.polito.waii.orchestrator.services

import it.polito.waii.orchestrator.dtos.TransactionDto
import it.polito.waii.orchestrator.dtos.UpdateQuantityDtoKafka
import org.springframework.core.ParameterizedTypeReference
import org.springframework.kafka.requestreply.ReplyingKafkaTemplate
import org.springframework.kafka.requestreply.RequestReplyMessageFuture
import org.springframework.kafka.requestreply.RequestReplyTypedMessageFuture
import org.springframework.kafka.support.KafkaHeaders
import org.springframework.messaging.support.MessageBuilder
import org.springframework.stereotype.Service
import java.nio.ByteBuffer
import java.time.Duration

@Service
class OrchestratorServiceImpl(
    val warehouseReplyingKafkaTemplate: ReplyingKafkaTemplate<String, Set<UpdateQuantityDtoKafka>, Float>,
    val walletReplyingKafkaTemplate: ReplyingKafkaTemplate<String, TransactionDto, Long>
    ) : OrchestratorService {

    override fun checkWarehouse(updateQuantitiesDto: Set<UpdateQuantityDtoKafka>): RequestReplyTypedMessageFuture<String, Set<UpdateQuantityDtoKafka>, Float> {
        val replyPartition = ByteBuffer.allocate(Int.SIZE_BYTES)
        replyPartition.putInt(0)
        val correlationId = ByteBuffer.allocate(Int.SIZE_BYTES)
        correlationId.putInt(2)

        return warehouseReplyingKafkaTemplate
            .sendAndReceive(
                MessageBuilder
                    .withPayload(
                        updateQuantitiesDto
                    )
                    .setHeader(KafkaHeaders.TOPIC, "warehouse_service_requests")
                    .setHeader(KafkaHeaders.PARTITION_ID, 0)
                    .setHeader(KafkaHeaders.CORRELATION_ID, correlationId.array())
                    .setHeader(KafkaHeaders.MESSAGE_KEY, "key1")
                    .setHeader(KafkaHeaders.REPLY_TOPIC, "warehouse_service_responses")
                    .setHeader(KafkaHeaders.REPLY_PARTITION, replyPartition.array())
                    .build(),
                Duration.ofSeconds(15),
                ParameterizedTypeReference.forType(Float::class.java)
            )
    }

    override fun checkWallet(transactionDto: TransactionDto, username: String, roles: String): RequestReplyMessageFuture<String, TransactionDto> {
        val replyPartition = ByteBuffer.allocate(Int.SIZE_BYTES)
        replyPartition.putInt(0)
        val correlationId = ByteBuffer.allocate(Int.SIZE_BYTES)
        correlationId.putInt(3)

        return walletReplyingKafkaTemplate
            .sendAndReceive(
                MessageBuilder
                    .withPayload(
                        transactionDto
                    )
                    .setHeader(KafkaHeaders.TOPIC, "wallet_service_requests")
                    .setHeader(KafkaHeaders.PARTITION_ID, 0)
                    .setHeader(KafkaHeaders.CORRELATION_ID, correlationId.array())
                    .setHeader(KafkaHeaders.MESSAGE_KEY, "key1")
                    .setHeader(KafkaHeaders.REPLY_TOPIC, "wallet_service_responses")
                    .setHeader(KafkaHeaders.REPLY_PARTITION, replyPartition.array())
                    .setHeader("username", username)
                    .setHeader("roles", roles)
                    .build(),
                Duration.ofSeconds(15)
            )
    }


}