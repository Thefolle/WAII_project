package it.polito.waii.order_service.kafka.producer

import com.fasterxml.jackson.databind.ObjectMapper
import it.polito.waii.order_service.dtos.OrderDto
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.TopicPartition
import org.apache.kafka.common.errors.SerializationException
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.kafka.common.serialization.StringSerializer
import org.apache.kafka.common.serialization.VoidSerializer
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
import org.springframework.kafka.support.serializer.JsonDeserializer

@Configuration
class GetOrdersLoopback {

    @Bean
    fun getOrdersLoopbackProducerFactory(): ProducerFactory<String, Void> {
        var config = mapOf(
            ProducerConfig.BOOTSTRAP_SERVERS_CONFIG to "kafka:9092",
            ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG to StringSerializer::class.java,
            ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG to VoidSerializer::class.java
        )

        return DefaultKafkaProducerFactory(config)
    }

    @Bean
    fun getOrdersLoopbackConcurrentMessageListenerContainer(@Qualifier("getOrdersLoopbackConcurrentKafkaListenerContainerFactory") containerFactory: ConcurrentKafkaListenerContainerFactory<String, Set<OrderDto>>): ConcurrentMessageListenerContainer<String, Set<OrderDto>> {
        var container = containerFactory.createContainer("order_service_responses")
        container.containerProperties.setGroupId("order_service_group_id_4")

        val consumerAwareBatchErrorHandler = ConsumerAwareBatchErrorHandler { thrownException, data, consumer ->
            if (thrownException is SerializationException) {
                consumer.seek(TopicPartition("order_service_responses", 0), consumer.position(TopicPartition("order_service_responses", 0)) + 1)
            }
        }
        container.setBatchErrorHandler(consumerAwareBatchErrorHandler)

        return container
    }

    @Bean
    fun getOrdersLoopbackConcurrentKafkaListenerContainerFactory(@Qualifier("getOrdersLoopbackConsumerFactory") consumerFactory: ConsumerFactory<String, Set<OrderDto>>): ConcurrentKafkaListenerContainerFactory<String, Set<OrderDto>> {
        var container = ConcurrentKafkaListenerContainerFactory<String, Set<OrderDto>>()
        container.consumerFactory = consumerFactory

        return container
    }

    @Bean
    fun getOrdersLoopbackConsumerFactory(): ConsumerFactory<String, Set<OrderDto>> {
        var config = mapOf(
            ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG to "kafka:9092",
            ConsumerConfig.GROUP_ID_CONFIG to "order_service_group_id",
            ConsumerConfig.AUTO_OFFSET_RESET_CONFIG to "latest",
            JsonDeserializer.TRUSTED_PACKAGES to "*"
        )
        val objectMapper = ObjectMapper()
        val type = objectMapper.typeFactory.constructParametricType(Set::class.java, OrderDto::class.java)

        return DefaultKafkaConsumerFactory(config, StringDeserializer(), JsonDeserializer(type, objectMapper, false))
    }

    @Bean
    fun getOrdersLoopbackReplyingKafkaTemplate(@Qualifier("getOrdersLoopbackProducerFactory") producerFactory: ProducerFactory<String, Void>, @Qualifier("getOrdersLoopbackConcurrentMessageListenerContainer") container: ConcurrentMessageListenerContainer<String, Set<OrderDto>>): ReplyingKafkaTemplate<String, Void, Set<OrderDto>> {
        val replyingKafkaTemplate = ReplyingKafkaTemplate(producerFactory, container)
        replyingKafkaTemplate.setSharedReplyTopic(true)
        return replyingKafkaTemplate
    }

}
