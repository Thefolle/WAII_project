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
class GetAllAdminEmails {

    @Bean
    fun getAllAdminEmailsConsumerFactory(): ConsumerFactory<String, Void> {
        var config = mapOf(
            ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG to "localhost:9092",
            ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG to StringDeserializer::class.java,
            ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG to VoidDeserializer::class.java,
            ConsumerConfig.GROUP_ID_CONFIG to "order_service_group_id",
            ConsumerConfig.AUTO_OFFSET_RESET_CONFIG to "latest",
        )

        return DefaultKafkaConsumerFactory(config)
    }

    @Bean
    fun getAllAdminEmailsConcurrentKafkaListenerContainerFactory(@Qualifier("getAllAdminEmailsConsumerFactory") consumerFactory: ConsumerFactory<String, Void>, @Qualifier("getAllAdminEmailsKafkaTemplate") kafkaTemplate: KafkaTemplate<String, Set<String>>, exceptionReplyTemplate: KafkaTemplate<String, Any>): ConcurrentKafkaListenerContainerFactory<String, Void> {
        var concurrentKafkaListenerContainerFactory = ConcurrentKafkaListenerContainerFactory<String, Void>()
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
    fun getAllAdminEmailsProducerFactory(): ProducerFactory<String, Set<String>> {
        var config = mapOf(
            ProducerConfig.BOOTSTRAP_SERVERS_CONFIG to "localhost:9092"
        )
        val objectMapper = ObjectMapper()
        val type = objectMapper.typeFactory.constructParametricType(Set::class.java, String::class.java)

        return DefaultKafkaProducerFactory(config, StringSerializer(), JsonSerializer(type, objectMapper))
    }

    @Bean
    fun getAllAdminEmailsKafkaTemplate(@Qualifier("getAllAdminEmailsProducerFactory") producerFactory: ProducerFactory<String, Set<String>>): KafkaTemplate<String, Set<String>> {
        return KafkaTemplate(producerFactory)
    }

    @Bean
    fun getAllAdminEmailsExceptionProducerFactory(): ProducerFactory<String, Any> {
        var config = mapOf(
            ProducerConfig.BOOTSTRAP_SERVERS_CONFIG to "localhost:9092",
            ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG to StringSerializer::class.java,
            ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG to JsonSerializer::class.java
        )

        return DefaultKafkaProducerFactory(config)
    }

    @Bean
    fun getAllAdminEmailsExceptionKafkaTemplate(producerFactory: ProducerFactory<String, Any>): KafkaTemplate<String, Any> {
        return KafkaTemplate(producerFactory)
    }

}