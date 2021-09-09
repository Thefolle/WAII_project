package it.polito.waii.orchestrator.kafka.producer

import it.polito.waii.orchestrator.dtos.TransactionDto
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
import org.springframework.retry.support.RetryTemplateBuilder
import org.springframework.util.backoff.BackOffExecution
import java.time.Duration

@Configuration
class CheckWallet {

    @Bean
    fun checkWalletProducerFactory(): ProducerFactory<String, TransactionDto> {
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
    fun checkWalletKafkaTemplate(producerFactory: ProducerFactory<String, TransactionDto>): KafkaTemplate<String, TransactionDto> {
        return KafkaTemplate(producerFactory)
    }

    @Bean
    fun checkWalletReplyingKafkaTemplate(producerFactory: ProducerFactory<String, TransactionDto>, @Qualifier("checkWalletConcurrentMessageListenerContainer") container: ConcurrentMessageListenerContainer<String, Long>): ReplyingKafkaTemplate<String, TransactionDto, Long> {
        val replyingKafkaTemplate = ReplyingKafkaTemplate(producerFactory, container)
        replyingKafkaTemplate.setSharedReplyTopic(true)
        // don't use the replyTimeout parameter of sendAndReceive: it is neglected, probably for a bug
        replyingKafkaTemplate.setDefaultReplyTimeout(Duration.ofSeconds(10))
        return replyingKafkaTemplate
    }

    @Bean
    fun checkWalletConsumerFactory(): ConsumerFactory<String, Long> {
        var config = mapOf(
            ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG to "localhost:9092",
            ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG to StringDeserializer::class.java,
            ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG to LongDeserializer::class.java,
            ConsumerConfig.GROUP_ID_CONFIG to "orchestrator_group_id_1",
            ConsumerConfig.AUTO_OFFSET_RESET_CONFIG to "latest"
        )

        return DefaultKafkaConsumerFactory(config)
    }

    @Bean
    fun checkWalletExceptionKafkaTemplate(@Qualifier("checkWalletExceptionProducerFactory") producerFactory: ProducerFactory<String, Any>): KafkaTemplate<String, Any> {
        return KafkaTemplate(producerFactory)
    }

    @Bean
    fun checkWalletConcurrentKafkaListenerContainerFactory(@Qualifier("checkWalletConsumerFactory") consumerFactory: ConsumerFactory<String, Long>, messageConverter: StringJsonMessageConverter, @Qualifier("checkWalletExceptionKafkaTemplate") exceptionReplyTemplate: KafkaTemplate<String, Any>): ConcurrentKafkaListenerContainerFactory<String, Long> {
        var containerFactory = ConcurrentKafkaListenerContainerFactory<String, Long>()
        containerFactory.containerProperties.setGroupId("orchestrator_group_id_1")
        containerFactory.consumerFactory = consumerFactory
        containerFactory.setMessageConverter(messageConverter)

        // The backoff controls how many times Kafka will attempt to send the same request on a controller;
        // in this case, no additional attempts are allowed
        containerFactory.setErrorHandler(SeekToCurrentErrorHandler(DeadLetterPublishingRecoverer(exceptionReplyTemplate) { _, _ ->
            TopicPartition(
                "exceptions",
                0
            )
        }) { BackOffExecution { BackOffExecution.STOP } })

        return containerFactory
    }

    @Bean
    fun checkWalletConcurrentMessageListenerContainer(@Qualifier("checkWalletConcurrentKafkaListenerContainerFactory") containerFactory: ConcurrentKafkaListenerContainerFactory<String, Long>): ConcurrentMessageListenerContainer<String, Long> {
        var container = containerFactory.createContainer("wallet_service_responses")
        container.containerProperties.setGroupId("orchestrator_group_id_1")

        val consumerAwareBatchErrorHandler = ConsumerAwareBatchErrorHandler { thrownException, data, consumer ->
            if (thrownException is SerializationException) {
                consumer.seek(TopicPartition("wallet_service_responses", 0), consumer.position(TopicPartition("wallet_service_responses", 0)) + 1)
            }
        }
        container.setBatchErrorHandler(consumerAwareBatchErrorHandler)

        return container
    }

}
