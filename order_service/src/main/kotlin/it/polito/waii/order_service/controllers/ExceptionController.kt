package it.polito.waii.order_service.controllers

import org.apache.kafka.clients.consumer.ConsumerRecord
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.annotation.TopicPartition
import org.springframework.kafka.support.KafkaHeaders
import org.springframework.stereotype.Component
import java.nio.ByteBuffer
import java.nio.charset.Charset

@Component
class ExceptionController {

    @KafkaListener(
        containerFactory = "exceptionsConcurrentKafkaListenerContainerFactory",
        topicPartitions = [TopicPartition(topic = "exceptions", partitions = ["0"])]
    )
    fun handleException(message: ConsumerRecord<Any, Any>) {
        println("Probed exception in the dead-letter topic:")

        message.headers().asIterable()
            .forEach {
                if (it.key().contentEquals("kafka_replyTopic") ||
                    it.key().contentEquals("kafka_dlt-original-topic") ||
                    it.key().contentEquals("kafka_dlt-exception-message")) {
                    println("- ${it.key()}: ${it.value().toString(Charset.defaultCharset())}")
                }

            }

    }

}