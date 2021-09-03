package it.polito.waii.order_service.kafka.producer

import it.polito.waii.order_service.dtos.OrderDto
import it.polito.waii.order_service.dtos.PatchOrderDto
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.TopicPartition
import org.apache.kafka.common.errors.SerializationException
import org.apache.kafka.common.serialization.*
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
import java.time.Duration

@Configuration
class UpdateOrderLoopback {

    @Bean
    fun updateOrderLoopbackProducerFactory(): ProducerFactory<String, PatchOrderDto> {
        var config = mapOf(
            ProducerConfig.BOOTSTRAP_SERVERS_CONFIG to "localhost:9092",
            ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG to StringSerializer::class.java,
            ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG to JsonSerializer::class.java
        )

        return DefaultKafkaProducerFactory(config)
    }

    @Bean
    fun updateOrderLoopbackOrderDtoReplyingKafkaTemplate(@Qualifier("updateOrderLoopbackProducerFactory") producerFactory: ProducerFactory<String, PatchOrderDto>, @Qualifier("updateOrderLoopbackConcurrentMessageListenerContainer") container: ConcurrentMessageListenerContainer<String, Void>): ReplyingKafkaTemplate<String, PatchOrderDto, Void> {
        val replyingKafkaTemplate = ReplyingKafkaTemplate(producerFactory, container)
        replyingKafkaTemplate.setSharedReplyTopic(true)
        // don't use the replyTimeout parameter of sendAndReceive: it is neglected, probably for a bug
        replyingKafkaTemplate.setDefaultReplyTimeout(Duration.ofSeconds(15))
        return replyingKafkaTemplate
    }

    @Bean
    fun updateOrderLoopbackConcurrentMessageListenerContainer(@Qualifier("updateOrderLoopbackConcurrentKafkaListenerContainerFactory") containerFactory: ConcurrentKafkaListenerContainerFactory<String, Void>): ConcurrentMessageListenerContainer<String, Void> {
        var container = containerFactory.createContainer("order_service_responses")
        container.containerProperties.setGroupId("order_service_group_id_20")

        val consumerAwareBatchErrorHandler = ConsumerAwareBatchErrorHandler { thrownException, data, consumer ->
            if (thrownException is SerializationException) {
                consumer.seek(TopicPartition("order_service_responses", 0), consumer.position(TopicPartition("order_service_responses", 0)) + 1)
            }
        }
        container.setBatchErrorHandler(consumerAwareBatchErrorHandler)

        return container
    }

    @Bean
    fun updateOrderLoopbackConcurrentKafkaListenerContainerFactory(@Qualifier("updateOrderLoopbackConsumerFactory") consumerFactory: ConsumerFactory<String, Void>): ConcurrentKafkaListenerContainerFactory<String, Void> {
        var container = ConcurrentKafkaListenerContainerFactory<String, Void>()
        container.consumerFactory = consumerFactory

        return container
    }

    @Bean
    fun updateOrderLoopbackConsumerFactory(): ConsumerFactory<String, Void> {
        var config = mapOf(
            ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG to "localhost:9092",
            ConsumerConfig.GROUP_ID_CONFIG to "order_service_group_id",
            ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG to StringDeserializer::class.java,
            ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG to VoidDeserializer::class.java,
            ConsumerConfig.AUTO_OFFSET_RESET_CONFIG to "latest"
        )

        return DefaultKafkaConsumerFactory(config)
    }

}
