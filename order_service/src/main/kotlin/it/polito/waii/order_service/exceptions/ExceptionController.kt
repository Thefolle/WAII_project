package it.polito.waii.order_service.exceptions

import org.apache.kafka.clients.consumer.ConsumerRecord
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.annotation.TopicPartition
import org.springframework.kafka.support.KafkaHeaders
import org.springframework.stereotype.Component
import java.nio.ByteBuffer

@Component
class ExceptionController {

    @KafkaListener(
        containerFactory = "exceptionsConcurrentKafkaListenerContainerFactory",
        topicPartitions = [TopicPartition(topic = "exceptions", partitions = ["0"])]
    )
    fun handleException(message: ConsumerRecord<Any, Any>) {
        println("Exception printer")
        val correlationId = ByteBuffer.allocate(2 * Int.SIZE_BYTES)
        correlationId.put(message.headers().find { it.key().contentEquals(KafkaHeaders.CORRELATION_ID) }?.value(), 0, 4)
        println("\n${correlationId.int}\n")

    }

}