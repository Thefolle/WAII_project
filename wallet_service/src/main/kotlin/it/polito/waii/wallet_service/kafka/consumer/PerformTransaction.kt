package it.polito.waii.wallet_service.kafka.consumer

import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.TopicPartition
import org.apache.kafka.common.serialization.LongSerializer
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.kafka.common.serialization.StringSerializer
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory
import org.springframework.kafka.core.*
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer
import org.springframework.kafka.listener.SeekToCurrentErrorHandler
import org.springframework.kafka.support.converter.MessageConverter
import org.springframework.kafka.support.converter.StringJsonMessageConverter
import org.springframework.kafka.support.serializer.JsonSerializer
import org.springframework.util.backoff.BackOffExecution
import java.time.Instant

@Configuration
class PerformTransaction {

    val REQUEST_TIMEOUT_MS_CONFIG = 15000

    @Bean
    fun performTransactionConsumerFactory(): ConsumerFactory<String, String> {
        var config = mapOf(
            ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG to "localhost:9092",
            ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG to StringDeserializer::class.java,
            ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG to StringDeserializer::class.java,
            ConsumerConfig.GROUP_ID_CONFIG to "wallet_service_group_id_0",
            ConsumerConfig.AUTO_OFFSET_RESET_CONFIG to "latest",
            ConsumerConfig.REQUEST_TIMEOUT_MS_CONFIG to REQUEST_TIMEOUT_MS_CONFIG
        )

        return DefaultKafkaConsumerFactory(config)
    }

    @Bean
    fun performTransactionExceptionProducerFactory(): ProducerFactory<String, Any> {
        var config = mapOf(
            ProducerConfig.BOOTSTRAP_SERVERS_CONFIG to "localhost:9092",
            ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG to StringSerializer::class.java,
            ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG to JsonSerializer::class.java
        )

        return DefaultKafkaProducerFactory(config)
    }

    @Bean
    fun performTransactionExceptionKafkaTemplate(producerFactory: ProducerFactory<String, Any>): KafkaTemplate<String, Any> {
        return KafkaTemplate(producerFactory)
    }

    @Bean
    fun performTransactionConcurrentKafkaListenerContainerFactory(@Qualifier("performTransactionConsumerFactory") consumerFactory: ConsumerFactory<String, String>, messageConverter: MessageConverter, replyTemplate: KafkaTemplate<String, Long>, @Qualifier("performTransactionExceptionKafkaTemplate") exceptionReplyTemplate: KafkaTemplate<String, Any>): ConcurrentKafkaListenerContainerFactory<String, String> {
        var containerFactory = ConcurrentKafkaListenerContainerFactory<String, String>()
        containerFactory.containerProperties.setGroupId("wallet_service_group_id_0")
        containerFactory.consumerFactory = consumerFactory
        containerFactory.setMessageConverter(messageConverter)
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
            it.timestamp() < containerFactoryInitializationTimestamp
        }
        containerFactory.setAckDiscarded(true)

        return containerFactory
    }

    @Bean
    fun performTransactionProducerFactory(): ProducerFactory<String, Long> {
        var config = mapOf(
            ProducerConfig.BOOTSTRAP_SERVERS_CONFIG to "localhost:9092",
            ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG to StringSerializer::class.java,
            ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG to LongSerializer::class.java
        )

        return DefaultKafkaProducerFactory(config)
    }

    @Bean
    fun performTransactionKafkaTemplate(producerFactory: ProducerFactory<String, Long>): KafkaTemplate<String, Long> {
        return KafkaTemplate(producerFactory)
    }

}
