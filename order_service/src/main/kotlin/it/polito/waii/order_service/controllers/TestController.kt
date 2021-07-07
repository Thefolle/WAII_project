package it.polito.waii.order_service.controllers

import com.fasterxml.jackson.databind.ObjectMapper
import it.polito.waii.order_service.dtos.DeliveryDto
import it.polito.waii.order_service.dtos.OrderDto
import it.polito.waii.order_service.entities.OrderStatus
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.MediaType
import org.springframework.kafka.requestreply.ReplyingKafkaTemplate
import org.springframework.kafka.support.KafkaHeaders
import org.springframework.kafka.support.KafkaNull
import org.springframework.messaging.support.MessageBuilder
import org.springframework.web.bind.annotation.*
import java.nio.ByteBuffer
import java.nio.charset.Charset
import java.time.Duration
import java.util.concurrent.TimeUnit

@RestController
class TestController {

    @Autowired
    lateinit var stringVoidSetOrderDtoReplyingKafkaTemplate: ReplyingKafkaTemplate<String, Void, Set<OrderDto>>

    @Autowired
    lateinit var stringOrderDtoLongReplyingKafkaTemplate: ReplyingKafkaTemplate<String, OrderDto, Long>

    @Autowired
    lateinit var stringLongOrderDtoReplyingKafkaTemplate: ReplyingKafkaTemplate<String, Long, OrderDto>

    @Autowired
    lateinit var stringOrderDtoVoidReplyingKafkaTemplate: ReplyingKafkaTemplate<String, OrderDto, Void>

    @PostMapping
    fun createOrder(): Long {
        val replyPartition = ByteBuffer.allocate(Int.SIZE_BYTES)
        replyPartition.putInt(0)
        val correlationId = ByteBuffer.allocate(Int.SIZE_BYTES)
        correlationId.putInt(0)

        return stringOrderDtoLongReplyingKafkaTemplate
            .sendAndReceive<Long?>(
                MessageBuilder
                    .withPayload(
                        OrderDto(
                            null,
                            2,
                            setOf(3, 5),
                            setOf(DeliveryDto("Paseo de Gracia, 56", 0)),
                            OrderStatus.CANCELED
                        )
                    )
                    .setHeader(KafkaHeaders.TOPIC, "order_service_requests")
                    .setHeader(KafkaHeaders.PARTITION_ID, 0)
                    .setHeader(KafkaHeaders.MESSAGE_KEY, "key1")
                    .setHeader(
                        KafkaHeaders.REPLY_TOPIC,
                        "order_service_responses".toByteArray(Charset.defaultCharset())
                    )
                    .setHeader(KafkaHeaders.REPLY_PARTITION, replyPartition.array())
                    .setHeader(KafkaHeaders.CORRELATION_ID, correlationId.array())
                    .build(),
                ParameterizedTypeReference.forType(Long::class.java)
            )
            .get()
            .payload
    }

    @GetMapping
    fun getOrders(): Set<OrderDto> {
        val replyPartition = ByteBuffer.allocate(Int.SIZE_BYTES)
        replyPartition.putInt(0)
        val correlationId = ByteBuffer.allocate(Int.SIZE_BYTES)
        correlationId.putInt(1)
        val objectMapper = ObjectMapper()
        val type = objectMapper.typeFactory.constructParametricType(Set::class.java, OrderDto::class.java)

        return stringVoidSetOrderDtoReplyingKafkaTemplate
            .sendAndReceive(
                MessageBuilder
                    .withPayload(KafkaNull.INSTANCE)
                    .setHeader(KafkaHeaders.TOPIC, "order_service_requests")
                    .setHeader(KafkaHeaders.PARTITION_ID, 1)
                    .setHeader(KafkaHeaders.MESSAGE_KEY, "key1")
                    .setHeader(
                        KafkaHeaders.REPLY_TOPIC,
                        "order_service_responses".toByteArray(Charset.defaultCharset())
                    )
                    .setHeader(KafkaHeaders.REPLY_PARTITION, replyPartition.array())
                    .setHeader(KafkaHeaders.CORRELATION_ID, correlationId.array())
                    .build(),
                Duration.ofSeconds(15),
                ParameterizedTypeReference.forType<Set<OrderDto>>(type)
            )
            .get()
            .payload
    }

    @GetMapping("/{id}")
    fun getOrderById(@PathVariable("id") id: Long) : OrderDto {
        val replyPartition = ByteBuffer.allocate(Int.SIZE_BYTES)
        replyPartition.putInt(0)
        val correlationId = ByteBuffer.allocate(Int.SIZE_BYTES)
        correlationId.putInt(2)

        return stringLongOrderDtoReplyingKafkaTemplate
            .sendAndReceive(
                MessageBuilder
                    .withPayload(
                        id
                    )
                    .setHeader(KafkaHeaders.TOPIC, "order_service_requests")
                    .setHeader(KafkaHeaders.PARTITION_ID, 2)
                    .setHeader(KafkaHeaders.MESSAGE_KEY, "key1")
                    .setHeader(
                        KafkaHeaders.REPLY_TOPIC,
                        "order_service_responses".toByteArray(Charset.defaultCharset())
                    )
                    .setHeader(KafkaHeaders.REPLY_PARTITION, replyPartition.array())
                    .setHeader(KafkaHeaders.CORRELATION_ID, correlationId.array())
                    .build(),
                ParameterizedTypeReference.forType<OrderDto>(OrderDto::class.java)
            )
            .get()
            .payload
    }

    @PatchMapping("/{id}", consumes = [MediaType.APPLICATION_JSON_VALUE])
    fun updateOrder(@PathVariable("id") id: Long, @RequestBody orderDto: OrderDto) {
        val replyPartition = ByteBuffer.allocate(Int.SIZE_BYTES)
        replyPartition.putInt(0)
        val correlationId = ByteBuffer.allocate(Int.SIZE_BYTES)
        correlationId.putInt(3)

        stringOrderDtoVoidReplyingKafkaTemplate
            .send(MessageBuilder
                .withPayload(
                    orderDto
                )
                .setHeader(KafkaHeaders.TOPIC, "order_service_requests")
                .setHeader(KafkaHeaders.PARTITION_ID, 3)
                .setHeader(KafkaHeaders.MESSAGE_KEY, "key1")
                .build()
            )
            .get()
    }

}