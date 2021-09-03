package it.polito.waii.order_service.controllers

import com.fasterxml.jackson.databind.ObjectMapper
import it.polito.waii.order_service.dtos.InputOrderDto
import it.polito.waii.order_service.dtos.OrderDto
import it.polito.waii.order_service.dtos.PatchOrderDto
import it.polito.waii.order_service.dtos.UserDTO
import kotlinx.coroutines.reactor.awaitSingle
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.kafka.requestreply.ReplyingKafkaTemplate
import org.springframework.kafka.support.KafkaHeaders
import org.springframework.kafka.support.KafkaNull
import org.springframework.kafka.support.KafkaUtils
import org.springframework.messaging.Message
import org.springframework.messaging.MessageHeaders
import org.springframework.messaging.support.MessageBuilder
import org.springframework.security.core.context.ReactiveSecurityContextHolder
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException
import java.nio.ByteBuffer
import java.nio.charset.Charset
import java.time.Duration
import java.util.concurrent.TimeUnit
import javax.validation.Valid

@RestController
class TestController {

    @Autowired
    lateinit var stringVoidSetOrderDtoReplyingKafkaTemplate: ReplyingKafkaTemplate<String, Void, Set<OrderDto>>

    @Autowired
    @Qualifier("createOrderLoopbackReplyingKafkaTemplate")
    lateinit var stringOrderDtoLongReplyingKafkaTemplate: ReplyingKafkaTemplate<String, InputOrderDto, Long>

    @Autowired
    lateinit var stringLongOrderDtoReplyingKafkaTemplate: ReplyingKafkaTemplate<String, Long, OrderDto>

    @Autowired
    lateinit var stringPatchOrderDtoVoidReplyingKafkaTemplate: ReplyingKafkaTemplate<String, PatchOrderDto, Void>

    @Autowired
    lateinit var stringLongVoidReplyingKafkaTemplate: ReplyingKafkaTemplate<String, Long, Void>

    @PostMapping
    suspend fun createOrder(@Valid @RequestBody orderDto: InputOrderDto): String {
        var userDetails = extractPrincipalFromSecurityContext()
        val username = userDetails.username
        val roles = userDetails.authorities.joinToString(",") { it.authority }

        val replyPartition = ByteBuffer.allocate(Int.SIZE_BYTES)
        replyPartition.putInt(0)
        val correlationId = ByteBuffer.allocate(Int.SIZE_BYTES)
        correlationId.putInt(0)

        val future =
        stringOrderDtoLongReplyingKafkaTemplate
            .sendAndReceive<Long?>(
                MessageBuilder
                    .withPayload(
                        orderDto
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
                    .setHeader("username", username)
                    .setHeader("roles", roles)
                    .build(),
                ParameterizedTypeReference.forType(Long::class.java)
            )

        var response: Message<Long>
        try {
            response = future.get()
        } catch (exception: Exception) {
            throw ResponseStatusException(HttpStatus.REQUEST_TIMEOUT, "The request cannot be processed due to some " +
                    "malfunction. Please, try later.")
        }

        propagateExceptionIfPresent(response.headers)

        return "The order has been correctly created. The id of the order is ${response.payload}."

    }

    @GetMapping
    fun getOrders(): Set<OrderDto> {
        val replyPartition = ByteBuffer.allocate(Int.SIZE_BYTES)
        replyPartition.putInt(0)
        val correlationId = ByteBuffer.allocate(Int.SIZE_BYTES)
        correlationId.putInt(1)
        val objectMapper = ObjectMapper()
        val type = objectMapper.typeFactory.constructParametricType(Set::class.java, OrderDto::class.java)

        val future = stringVoidSetOrderDtoReplyingKafkaTemplate
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
                ParameterizedTypeReference.forType<Set<OrderDto>>(type)
            )

        var response: Message<Set<OrderDto>>
        try {
            response = future.get()
        } catch (exception: Exception) {
            throw ResponseStatusException(HttpStatus.REQUEST_TIMEOUT, "The request cannot be processed due to some " +
                    "malfunction. Please, try later.")
        }

        return response.payload
    }

    @GetMapping("/{id}")
    fun getOrderById(@PathVariable("id") id: Long) : OrderDto {
        val replyPartition = ByteBuffer.allocate(Int.SIZE_BYTES)
        replyPartition.putInt(0)
        val correlationId = ByteBuffer.allocate(Int.SIZE_BYTES)
        correlationId.putInt(2)

        val response = stringLongOrderDtoReplyingKafkaTemplate
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

        propagateExceptionIfPresent(response.headers)

        return response.payload
    }

    @PatchMapping("/{id}")
    suspend fun updateOrder(@PathVariable("id") id: Long, @RequestBody orderDto: PatchOrderDto): String {
        var userDetails = extractPrincipalFromSecurityContext()
        val username = userDetails.username
        val roles = userDetails.authorities.joinToString(",") { it.authority }

        if (orderDto.id == null) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "The order id was not specified." +
                    " If you want to create an order, use the pertinent endpoint instead.")
        } else if (id != orderDto.id) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "The order id in the URI and in the request body" +
                    " must match.")
        }

        val replyPartition = ByteBuffer.allocate(Int.SIZE_BYTES)
        replyPartition.putInt(0)
        val correlationId = ByteBuffer.allocate(Int.SIZE_BYTES)
        correlationId.putInt(3)

        var response: Message<Void>
        try {
            response = stringPatchOrderDtoVoidReplyingKafkaTemplate
                .sendAndReceive(
                    MessageBuilder
                        .withPayload(
                            orderDto
                        )
                        .setHeader(KafkaHeaders.TOPIC, "order_service_requests")
                        .setHeader(KafkaHeaders.PARTITION_ID, 3)
                        .setHeader(KafkaHeaders.MESSAGE_KEY, "key1")
                        .setHeader(
                            KafkaHeaders.REPLY_TOPIC,
                            "order_service_responses".toByteArray(Charset.defaultCharset())
                        )
                        .setHeader(KafkaHeaders.REPLY_PARTITION, replyPartition.array())
                        .setHeader(KafkaHeaders.CORRELATION_ID, correlationId.array())
                        .setHeader("username", username)
                        .setHeader("roles", roles)
                        .build(),
                    ParameterizedTypeReference.forType<Void>(Void::class.java)
                )
                .get()
        } catch (exception: Exception) {
            throw ResponseStatusException(HttpStatus.REQUEST_TIMEOUT, "The request cannot be processed due to some " +
                    "malfunction. Please, try later.")
        }

        propagateExceptionIfPresent(response.headers)

        return "Order updated successfully!"
    }

    @DeleteMapping("/{id}")
    suspend fun deleteOrderById(@PathVariable("id") id: Long): String {
        var userDetails = extractPrincipalFromSecurityContext()
        val username = userDetails.username
        val roles = userDetails.authorities.joinToString(",") { it.authority }

        val replyPartition = ByteBuffer.allocate(Int.SIZE_BYTES)
        replyPartition.putInt(0)
        val correlationId = ByteBuffer.allocate(Int.SIZE_BYTES)
        correlationId.putInt(4)

        var response: Message<*>
        try {
            response = stringLongVoidReplyingKafkaTemplate
                .sendAndReceive(
                    MessageBuilder
                        .withPayload(
                            id
                        )
                        .setHeader(KafkaHeaders.TOPIC, "order_service_requests")
                        .setHeader(KafkaHeaders.PARTITION_ID, 4)
                        .setHeader(KafkaHeaders.MESSAGE_KEY, "key1")
                        .setHeader("username", username)
                        .setHeader("roles", roles)
                        .setHeader(
                            KafkaHeaders.REPLY_TOPIC,
                            "order_service_responses".toByteArray(Charset.defaultCharset())
                        )
                        .setHeader(KafkaHeaders.REPLY_PARTITION, replyPartition.array())
                        .setHeader(KafkaHeaders.CORRELATION_ID, correlationId.array())
                        .build()
                )
                .get()
        } catch (exception: Exception) {
            throw ResponseStatusException(HttpStatus.REQUEST_TIMEOUT, "The request cannot be processed due to some " +
                    "malfunction. Please, try later.")
        }

        propagateExceptionIfPresent(response.headers)

        return "The order has been correctly canceled."
    }

    private suspend fun extractPrincipalFromSecurityContext(): UserDTO {
        var principal: Any
        try {
            principal = ReactiveSecurityContextHolder.getContext().awaitSingle().authentication.principal
        } catch (exception: NoSuchElementException) {
            throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "You need to login first.")
        }

        return if (principal is UserDTO) principal else throw ResponseStatusException(
            HttpStatus.BAD_REQUEST,
            "The jwt doesn't hold correct user data."
        )
    }

    private fun propagateExceptionIfPresent(headers: MessageHeaders) {
        if ((headers["hasException"] as Boolean)) {
            throw ResponseStatusException(
                HttpStatus.valueOf(headers["exceptionRawStatus"] as Int),
                headers["exceptionMessage"] as String
            )
        }
    }

}