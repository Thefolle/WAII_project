package it.polito.waii.order_service.controllers

import it.polito.waii.order_service.dtos.DeliveryDto
import it.polito.waii.order_service.dtos.OrderDto
import it.polito.waii.order_service.entities.OrderStatus
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.core.ParameterizedTypeReference
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.requestreply.ReplyingKafkaTemplate
import org.springframework.kafka.support.KafkaHeaders
import org.springframework.kafka.support.KafkaNull
import org.springframework.messaging.MessageHeaders
import org.springframework.messaging.support.MessageBuilder
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RestController
import java.nio.ByteBuffer
import java.nio.charset.Charset

@RestController
class TestController {

//    @Autowired
//    lateinit var voidKafkaTemplate: KafkaTemplate<String, Void>
//
//    @Autowired
//    lateinit var orderDtoKafkaTemplate: KafkaTemplate<String, OrderDto>

    @Autowired
    lateinit var replyingKafkaTemplate: ReplyingKafkaTemplate<String, OrderDto, Long>

//    @PostMapping
//    fun test() {
//        orderDtoKafkaTemplate.send(
//            "topic1",
//            0,
//            "key1",
//            OrderDto(null,
//                2,
//                setOf(3, 5),
//                setOf(DeliveryDto("Paseo de Gracia, 56", 0))
//            )
//        )
//
//        println("Sending message")
//    }
//
//    @GetMapping
//    fun getOrders() {
//        voidKafkaTemplate.send("topic1", 1, "key1", null)
//    }

    @PutMapping
    fun createOrder(): Long {
        val replyPartition = ByteBuffer.allocate(Int.SIZE_BYTES)
        replyPartition.putInt(0)
        val correlationId = ByteBuffer.allocate(Int.SIZE_BYTES)
        correlationId.putInt(0)

        val returnId =
        replyingKafkaTemplate
            .sendAndReceive(
                MessageBuilder
                    .withPayload(
                        OrderDto(null,
                            2,
                            setOf(3, 5),
                            setOf(DeliveryDto("Paseo de Gracia, 56", 0)),
                            OrderStatus.CANCELED
                        )
                    )
                    .setHeader(KafkaHeaders.TOPIC, "order_service_requests")
                    .setHeader(KafkaHeaders.PARTITION_ID, 0)
                    .setHeader(KafkaHeaders.MESSAGE_KEY, "key1")
                    .setHeader(KafkaHeaders.REPLY_TOPIC, "order_service_responses".toByteArray(Charset.defaultCharset()))
                    .setHeader(KafkaHeaders.REPLY_PARTITION, replyPartition.array())
                    .setHeader(KafkaHeaders.CORRELATION_ID, correlationId.array())
                    .build(),
                ParameterizedTypeReference.forType<Long>(Long::class.java)
            )
            .get()

        println(returnId)
        return 0
    }

}