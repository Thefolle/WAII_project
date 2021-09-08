package it.polito.waii.catalogue_service.kafka.consumer

import com.fasterxml.jackson.databind.ObjectMapper
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.TopicPartition
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.kafka.common.serialization.StringSerializer
import org.apache.kafka.common.serialization.VoidDeserializer
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory
import org.springframework.kafka.core.*
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer
import org.springframework.kafka.listener.SeekToCurrentErrorHandler
import org.springframework.kafka.support.serializer.JsonSerializer
import org.springframework.util.backoff.BackOffExecution

@Configuration
class GetUserEmail {

    @Bean
    fun getUserEmailConsumerFactory(): ConsumerFactory<String, String> {
        var config = mapOf(
            ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG to "localhost:9092",
            ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG to StringDeserializer::class.java,
            ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG to StringDeserializer::class.java,
            ConsumerConfig.GROUP_ID_CONFIG to "catalogue_service_group_id_3",
            ConsumerConfig.AUTO_OFFSET_RESET_CONFIG to "latest",
        )

        return DefaultKafkaConsumerFactory(config)
    }

    @Bean
    fun getUserEmailConcurrentKafkaListenerContainerFactory(@Qualifier("getUserEmailConsumerFactory") consumerFactory: ConsumerFactory<String, String>, @Qualifier("getUserEmailKafkaTemplate") kafkaTemplate: KafkaTemplate<String, String>, @Qualifier("getUserEmailExceptionKafkaTemplate") exceptionReplyTemplate: KafkaTemplate<String, Any>): ConcurrentKafkaListenerContainerFactory<String, String> {
        var concurrentKafkaListenerContainerFactory = ConcurrentKafkaListenerContainerFactory<String, String>()
        concurrentKafkaListenerContainerFactory.consumerFactory = consumerFactory
        concurrentKafkaListenerContainerFactory.setReplyTemplate(kafkaTemplate)

        // The backoff controls how many times Kafka will attempt to send the same request on a controller;
        // in this case, no additional attempts are allowed
        concurrentKafkaListenerContainerFactory.setErrorHandler(SeekToCurrentErrorHandler(DeadLetterPublishingRecoverer(exceptionReplyTemplate) { _, _ ->
            TopicPartition(
                "exceptions",
                0
            )
        }) { BackOffExecution { BackOffExecution.STOP } })

        return concurrentKafkaListenerContainerFactory
    }

    @Bean
    fun getUserEmailProducerFactory(): ProducerFactory<String, String> {
        var config = mapOf(
            ProducerConfig.BOOTSTRAP_SERVERS_CONFIG to "localhost:9092",
            ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG to StringSerializer::class.java,
            ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG to StringSerializer::class.java
        )

        return DefaultKafkaProducerFactory(config)
    }

    @Bean
    fun getUserEmailKafkaTemplate(@Qualifier("getUserEmailProducerFactory") producerFactory: ProducerFactory<String, String>): KafkaTemplate<String, String> {
        return KafkaTemplate(producerFactory)
    }

    @Bean
    fun getUserEmailExceptionProducerFactory(): ProducerFactory<String, Any> {
        var config = mapOf(
            ProducerConfig.BOOTSTRAP_SERVERS_CONFIG to "localhost:9092",
            ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG to StringSerializer::class.java,
            ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG to JsonSerializer::class.java
        )

        return DefaultKafkaProducerFactory(config)
    }

    @Bean
    fun getUserEmailExceptionKafkaTemplate(@Qualifier("getUserEmailExceptionProducerFactory") producerFactory: ProducerFactory<String, Any>): KafkaTemplate<String, Any> {
        return KafkaTemplate(producerFactory)
    }

}