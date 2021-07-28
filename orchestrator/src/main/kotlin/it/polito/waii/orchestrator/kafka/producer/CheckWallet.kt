package it.polito.waii.orchestrator.kafka.producer

import it.polito.waii.orchestrator.dtos.UpdateQuantityDtoKafka
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.TopicPartition
import org.apache.kafka.common.errors.SerializationException
import org.apache.kafka.common.serialization.LongDeserializer
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.kafka.common.serialization.StringSerializer
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory
import org.springframework.kafka.core.*
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer
import org.springframework.kafka.listener.ConsumerAwareBatchErrorHandler
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer
import org.springframework.kafka.listener.SeekToCurrentErrorHandler
import org.springframework.kafka.requestreply.ReplyingKafkaTemplate
import org.springframework.kafka.support.converter.StringJsonMessageConverter
import org.springframework.kafka.support.serializer.JsonSerializer
import org.springframework.util.backoff.BackOffExecution

@Configuration
class CheckWallet {

    val REQUEST_TIMEOUT_MS_CONFIG = 15000

    @Bean
    fun checkWalletProducerFactory(): ProducerFactory<String, UpdateQuantityDtoKafka> {
        var config = mapOf(
            ProducerConfig.BOOTSTRAP_SERVERS_CONFIG to "localhost:9092",
            ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG to StringSerializer::class.java,
            ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG to JsonSerializer::class.java
        )

        return DefaultKafkaProducerFactory(config)
    }

    @Bean
    fun checkWalletExceptionProducerFactory(): ProducerFactory<String, Any> {
        var config = mapOf(
            ProducerConfig.BOOTSTRAP_SERVERS_CONFIG to "localhost:9092",
            ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG to StringSerializer::class.java,
            ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG to JsonSerializer::class.java
        )

        return DefaultKafkaProducerFactory(config)
    }

    @Bean
    fun checkWalletKafkaTemplate(producerFactory: ProducerFactory<String, UpdateQuantityDtoKafka>): KafkaTemplate<String, UpdateQuantityDtoKafka> {
        return KafkaTemplate(producerFactory)
    }

    @Bean
    fun checkWalletReplyingKafkaTemplate(producerFactory: ProducerFactory<String, UpdateQuantityDtoKafka>, @Qualifier("checkWalletConcurrentMessageListenerContainer") container: ConcurrentMessageListenerContainer<String, Long>): ReplyingKafkaTemplate<String, UpdateQuantityDtoKafka, Long> {
        val replyingKafkaTemplate = ReplyingKafkaTemplate(producerFactory, container)
        replyingKafkaTemplate.setSharedReplyTopic(true)
        return replyingKafkaTemplate
    }

    @Bean
    fun checkWalletConsumerFactory(): ConsumerFactory<String, Long> {
        var config = mapOf(
            ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG to "localhost:9092",
            ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG to StringDeserializer::class.java,
            ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG to LongDeserializer::class.java,
            ConsumerConfig.GROUP_ID_CONFIG to "orchestrator_group_id",
            ConsumerConfig.AUTO_OFFSET_RESET_CONFIG to "latest",
            ConsumerConfig.REQUEST_TIMEOUT_MS_CONFIG to REQUEST_TIMEOUT_MS_CONFIG
        )

        return DefaultKafkaConsumerFactory(config)
    }

    @Bean
    fun checkWalletExceptionKafkaTemplate(@Qualifier("checkWalletExceptionProducerFactory") producerFactory: ProducerFactory<String, Any>): KafkaTemplate<String, Any> {
        return KafkaTemplate(producerFactory)
    }

    @Bean
    fun checkWalletConcurrentKafkaListenerContainerFactory(@Qualifier("checkWalletConsumerFactory") consumerFactory: ConsumerFactory<String, Long>, messageConverter: StringJsonMessageConverter, replyTemplate: KafkaTemplate<String, Long>, @Qualifier("checkWalletExceptionKafkaTemplate") exceptionReplyTemplate: KafkaTemplate<String, Any>): ConcurrentKafkaListenerContainerFactory<String, Long> {
        var container = ConcurrentKafkaListenerContainerFactory<String, Long>()
        container.consumerFactory = consumerFactory
        container.setMessageConverter(messageConverter)
        container.setReplyTemplate(replyTemplate)

        // The backoff controls how many times Kafka will attempt to send the same request on a controller;
        // in this case, no additional attempts are allowed
        container.setErrorHandler(SeekToCurrentErrorHandler(DeadLetterPublishingRecoverer(exceptionReplyTemplate) { _, _ ->
            TopicPartition(
                "exceptions",
                0
            )
        }) { BackOffExecution { BackOffExecution.STOP } })

        return container
    }

    @Bean
    fun checkWalletConcurrentMessageListenerContainer(@Qualifier("checkWalletConcurrentKafkaListenerContainerFactory") containerFactory: ConcurrentKafkaListenerContainerFactory<String, Long>): ConcurrentMessageListenerContainer<String, Long> {
        var container = containerFactory.createContainer("warehouse_service_responses")
        container.containerProperties.setGroupId("warehouse_service_group_id_1")

        val consumerAwareBatchErrorHandler = ConsumerAwareBatchErrorHandler { thrownException, data, consumer ->
            if (thrownException is SerializationException) {
                consumer.seek(TopicPartition("warehouse_service_responses", 0), consumer.position(TopicPartition("warehouse_service_responses", 0)) + 1)
            }
        }
        container.setBatchErrorHandler(consumerAwareBatchErrorHandler)

        return container
    }

}