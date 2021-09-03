package it.polito.waii.order_service.kafka.consumer

import it.polito.waii.order_service.dtos.PatchOrderDto
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.TopicPartition
import org.apache.kafka.common.serialization.*
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory
import org.springframework.kafka.core.*
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer
import org.springframework.kafka.listener.SeekToCurrentErrorHandler
import org.springframework.kafka.support.serializer.JsonDeserializer
import org.springframework.kafka.support.serializer.JsonSerializer
import org.springframework.util.backoff.BackOffExecution
import java.time.Instant

@Configuration
class UpdateOrder {

    @Bean
    fun updateOrderConsumerFactory(): ConsumerFactory<String, PatchOrderDto> {
        var config = mapOf(
            ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG to "localhost:9092",
            ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG to StringDeserializer::class.java,
            ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG to JsonDeserializer::class.java,
            ConsumerConfig.GROUP_ID_CONFIG to "order_service_group_id",
            ConsumerConfig.AUTO_OFFSET_RESET_CONFIG to "latest",
            JsonDeserializer.TRUSTED_PACKAGES to "*"
        )

        return DefaultKafkaConsumerFactory(config)
    }

    @Bean
    fun updateOrderExceptionKafkaTemplate(@Qualifier("updateOrderExceptionProducerFactory") producerFactory: ProducerFactory<String, Any>): KafkaTemplate<String, Any> {
        return KafkaTemplate(producerFactory)
    }

    @Bean
    fun updateOrderExceptionProducerFactory(): ProducerFactory<String, Any> {
        var config = mapOf(
            ProducerConfig.BOOTSTRAP_SERVERS_CONFIG to "localhost:9092",
            ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG to StringSerializer::class.java,
            ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG to JsonSerializer::class.java
        )

        return DefaultKafkaProducerFactory(config)
    }

    @Bean
    fun updateOrderConcurrentKafkaListenerContainerFactory(consumerFactory: ConsumerFactory<String, PatchOrderDto>, @Qualifier("updateOrderKafkaTemplate") replyTemplate: KafkaTemplate<String, Void>, @Qualifier("updateOrderExceptionKafkaTemplate") exceptionReplyTemplate: KafkaTemplate<String, Any>): ConcurrentKafkaListenerContainerFactory<String, PatchOrderDto> {
        var containerFactory = ConcurrentKafkaListenerContainerFactory<String, PatchOrderDto>()
        containerFactory.consumerFactory = consumerFactory
        containerFactory.setReplyTemplate(replyTemplate)

        // The backoff controls how many times Kafka will attempt to send the same request on a controller;
        // in this case, no additional attempts are allowed
        containerFactory.setErrorHandler(SeekToCurrentErrorHandler(DeadLetterPublishingRecoverer(exceptionReplyTemplate) { _, _ ->
            TopicPartition(
                "exceptions",
                0
            )
        }) { BackOffExecution { BackOffExecution.STOP } })

        val containerFactoryInitializationTimestamp = Instant.now().toEpochMilli()
        containerFactory.setRecordFilterStrategy {
            // discard messages earlier than the bootstrap of the microservice
            it.timestamp() < containerFactoryInitializationTimestamp
        }
        containerFactory.setAckDiscarded(true)

        return containerFactory
    }

    @Bean
    fun updateOrderKafkaTemplate(@Qualifier("updateOrderProducerFactory") producerFactory: ProducerFactory<String, Void>): KafkaTemplate<String, Void> {
        return KafkaTemplate(producerFactory)
    }

    @Bean
    fun updateOrderProducerFactory(): ProducerFactory<String, Void> {
        var config = mapOf(
            ProducerConfig.BOOTSTRAP_SERVERS_CONFIG to "localhost:9092",
            ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG to StringSerializer::class.java,
            ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG to VoidSerializer::class.java
        )

        return DefaultKafkaProducerFactory(config)
    }

}
