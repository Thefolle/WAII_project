package it.polito.waii.order_service.controllers

import com.fasterxml.jackson.databind.ObjectMapper
import it.polito.waii.order_service.dtos.DeliveryDto
import it.polito.waii.order_service.dtos.OrderDto
import it.polito.waii.order_service.dtos.PatchOrderDto
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.kafka.requestreply.ReplyingKafkaTemplate
import org.springframework.kafka.support.KafkaHeaders
import org.springframework.kafka.support.KafkaNull
import org.springframework.messaging.support.MessageBuilder
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException
import java.nio.ByteBuffer
import java.nio.charset.Charset
import java.time.Duration

@RestController
class TestController {

    @Autowired
    lateinit var stringVoidSetOrderDtoReplyingKafkaTemplate: ReplyingKafkaTemplate<String, Void, Set<OrderDto>>

    @Autowired
    @Qualifier("createOrderLoopbackReplyingKafkaTemplate")
    lateinit var stringOrderDtoLongReplyingKafkaTemplate: ReplyingKafkaTemplate<String, OrderDto, Long>

    @Autowired
    lateinit var stringLongOrderDtoReplyingKafkaTemplate: ReplyingKafkaTemplate<String, Long, OrderDto>

    @Autowired
    lateinit var stringPatchOrderDtoVoidReplyingKafkaTemplate: ReplyingKafkaTemplate<String, PatchOrderDto, Void>

    @Autowired
    lateinit var stringLongVoidReplyingKafkaTemplate: ReplyingKafkaTemplate<String, Long, Void>

    @PostMapping
    fun createOrder(): Long {
        val replyPartition = ByteBuffer.allocate(Int.SIZE_BYTES)
        replyPartition.putInt(0)
        val correlationId = ByteBuffer.allocate(Int.SIZE_BYTES)
        correlationId.putInt(0)

        val future =
        stringOrderDtoLongReplyingKafkaTemplate
            .sendAndReceive<Long?>(
                MessageBuilder
                    .withPayload(
                        OrderDto(
                            null,
                            2,
                            1,
                            mapOf(
                                1L to DeliveryDto(null, "Paseo de Gracia, 56", 2),
                                7L to DeliveryDto(null, "Calle Bertrellans, 15 Madrid", 3)
                            ),
                            mapOf(
                                1L to 1,
                                7L to 2
                            ),
                            3.5f,
                            null
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
                Duration.ofSeconds(15),
                ParameterizedTypeReference.forType(Long::class.java)
            )

        var result: Long
        try {
            result = future.get().payload
        } catch (exception: Exception) {
            throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, exception.message)
        }

        return result
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
    fun updateOrder(@PathVariable("id") id: Long, @RequestBody orderDto: PatchOrderDto) {

        stringPatchOrderDtoVoidReplyingKafkaTemplate
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

    @DeleteMapping("/{id}")
    fun deleteOrderById(@PathVariable("id") id: Long) {

        stringLongVoidReplyingKafkaTemplate
            .send(MessageBuilder
                .withPayload(
                    id
                )
                .setHeader(KafkaHeaders.TOPIC, "order_service_requests")
                .setHeader(KafkaHeaders.PARTITION_ID, 4)
                .setHeader(KafkaHeaders.MESSAGE_KEY, "key1")
                .build()
            )
            .get()
    }

}