package it.polito.waii.order_service.kafka.producer

import it.polito.waii.order_service.dtos.OrderDto
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
import org.springframework.kafka.core.ConsumerFactory
import org.springframework.kafka.core.DefaultKafkaConsumerFactory
import org.springframework.kafka.core.DefaultKafkaProducerFactory
import org.springframework.kafka.core.ProducerFactory
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer
import org.springframework.kafka.listener.ConsumerAwareBatchErrorHandler
import org.springframework.kafka.requestreply.ReplyingKafkaTemplate
import org.springframework.kafka.support.serializer.JsonSerializer
import org.springframework.retry.RetryPolicy
import org.springframework.retry.support.RetryTemplateBuilder

@Configuration
class CreateOrderToOrchestrator {

    @Bean
    fun createOrderToOrchestratorProducerFactory(): ProducerFactory<String, OrderDto> {
        var config = mapOf(
            ProducerConfig.BOOTSTRAP_SERVERS_CONFIG to "localhost:9092",
            ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG to StringSerializer::class.java,
            ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG to JsonSerializer::class.java
        )

        return DefaultKafkaProducerFactory(config)
    }

    @Bean
    fun createOrderToOrchestratorReplyingKafkaTemplate(@Qualifier("createOrderToOrchestratorProducerFactory") producerFactory: ProducerFactory<String, OrderDto>, @Qualifier("createOrderToOrchestratorConcurrentMessageListenerContainer") container: ConcurrentMessageListenerContainer<String, Long>): ReplyingKafkaTemplate<String, OrderDto, Long> {
        val replyingKafkaTemplate = ReplyingKafkaTemplate(producerFactory, container)
        replyingKafkaTemplate.setSharedReplyTopic(true)
        return replyingKafkaTemplate
    }

    @Bean
    fun createOrderToOrchestratorConcurrentMessageListenerContainer(@Qualifier("createOrderToOrchestratorConcurrentKafkaListenerContainerFactory") containerFactory: ConcurrentKafkaListenerContainerFactory<String, Long>): ConcurrentMessageListenerContainer<String, Long> {
        var container = containerFactory.createContainer("orchestrator_responses")
        container.containerProperties.setGroupId("outer_service_group_id")

        // this error handler is called when a listener receives a message from the shared reply topic that
        // is directed to another handler; the topic is just discarded by passing to the next message
        val consumerAwareBatchErrorHandler = ConsumerAwareBatchErrorHandler { thrownException, data, consumer ->
            if (thrownException is SerializationException) {
                consumer.seek(TopicPartition("orchestrator_responses", 0), consumer.position(TopicPartition("orchestrator_responses", 0)) + 1)
            }
        }
        container.setBatchErrorHandler(consumerAwareBatchErrorHandler)

        return container
    }

    @Bean
    fun createOrderToOrchestratorConcurrentKafkaListenerContainerFactory(@Qualifier("createOrderToOrchestratorConsumerFactory") consumerFactory: ConsumerFactory<String, Long>): ConcurrentKafkaListenerContainerFactory<String, Long> {
        var containerFactory = ConcurrentKafkaListenerContainerFactory<String, Long>()
        containerFactory.consumerFactory = consumerFactory
        containerFactory.setRetryTemplate(
            RetryTemplateBuilder()
                .maxAttempts(1)
                .build()
        )

        return containerFactory
    }

    @Bean
    fun createOrderToOrchestratorConsumerFactory(): ConsumerFactory<String, Long> {
        var config = mapOf(
            ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG to "localhost:9092",
            ConsumerConfig.GROUP_ID_CONFIG to "order_service_group_id",
            ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG to StringDeserializer::class.java,
            ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG to LongDeserializer::class.java,
            ConsumerConfig.AUTO_OFFSET_RESET_CONFIG to "latest"
        )

        return DefaultKafkaConsumerFactory(config)
    }

}