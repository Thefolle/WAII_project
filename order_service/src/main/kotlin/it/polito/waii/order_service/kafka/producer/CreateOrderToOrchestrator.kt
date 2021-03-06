package it.polito.waii.order_service.kafka.producer

import it.polito.waii.order_service.dtos.OrderDtoOrchestrator
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.TopicPartition
import org.apache.kafka.common.errors.SerializationException
import org.apache.kafka.common.serialization.FloatDeserializer
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
import java.time.Duration

@Configuration
class CreateOrderToOrchestrator {

    @Bean
    fun createOrderToOrchestratorProducerFactory(): ProducerFactory<String, OrderDtoOrchestrator> {
        var config = mapOf(
            ProducerConfig.BOOTSTRAP_SERVERS_CONFIG to "localhost:9092",
            ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG to StringSerializer::class.java,
            ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG to JsonSerializer::class.java
        )

        return DefaultKafkaProducerFactory(config)
    }

    @Bean
    fun createOrderToOrchestratorReplyingKafkaTemplate(@Qualifier("createOrderToOrchestratorProducerFactory") producerFactory: ProducerFactory<String, OrderDtoOrchestrator>, @Qualifier("createOrderToOrchestratorConcurrentMessageListenerContainer") container: ConcurrentMessageListenerContainer<String, Float>): ReplyingKafkaTemplate<String, OrderDtoOrchestrator, Float> {
        val replyingKafkaTemplate = ReplyingKafkaTemplate(producerFactory, container)
        replyingKafkaTemplate.setSharedReplyTopic(true)
        // don't use the replyTimeout parameter of sendAndReceive: it is neglected, probably for a bug
        replyingKafkaTemplate.setDefaultReplyTimeout(Duration.ofSeconds(17))
        return replyingKafkaTemplate
    }

    @Bean
    fun createOrderToOrchestratorConcurrentMessageListenerContainer(@Qualifier("createOrderToOrchestratorConcurrentKafkaListenerContainerFactory") containerFactory: ConcurrentKafkaListenerContainerFactory<String, Float>): ConcurrentMessageListenerContainer<String, Float> {
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
    fun createOrderToOrchestratorConcurrentKafkaListenerContainerFactory(@Qualifier("createOrderToOrchestratorConsumerFactory") consumerFactory: ConsumerFactory<String, Float>): ConcurrentKafkaListenerContainerFactory<String, Float> {
        var containerFactory = ConcurrentKafkaListenerContainerFactory<String, Float>()
        containerFactory.consumerFactory = consumerFactory

        return containerFactory
    }

    @Bean
    fun createOrderToOrchestratorConsumerFactory(): ConsumerFactory<String, Float> {
        var config = mapOf(
            ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG to "localhost:9092",
            ConsumerConfig.GROUP_ID_CONFIG to "order_service_group_id",
            ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG to StringDeserializer::class.java,
            ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG to FloatDeserializer::class.java,
            ConsumerConfig.AUTO_OFFSET_RESET_CONFIG to "latest"
        )

        return DefaultKafkaConsumerFactory(config)
    }

}
