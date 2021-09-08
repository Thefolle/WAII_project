package it.polito.waii.warehouse_service.kafka.producer

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.deser.std.NumberDeserializers
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.TopicPartition
import org.apache.kafka.common.errors.SerializationException
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.kafka.common.serialization.StringSerializer
import org.apache.kafka.common.serialization.LongSerializer
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
import org.springframework.kafka.support.TopicPartitionOffset
import org.springframework.kafka.support.serializer.JsonDeserializer
import java.time.Duration

@Configuration
class HasProductBeenBoughtByCustomer {

    @Bean
    fun hasProductBeenBoughtByCustomerProducerFactory(): ProducerFactory<String, Long> {
        var config = mapOf(
            ProducerConfig.BOOTSTRAP_SERVERS_CONFIG to "localhost:9092",
            ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG to StringSerializer::class.java,
            ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG to LongSerializer::class.java
        )

        return DefaultKafkaProducerFactory(config)
    }

    @Bean
    fun hasProductBeenBoughtByCustomerConcurrentMessageListenerContainer(@Qualifier("hasProductBeenBoughtByCustomerConcurrentKafkaListenerContainerFactory") containerFactory: ConcurrentKafkaListenerContainerFactory<String, Boolean>): ConcurrentMessageListenerContainer<String, Boolean> {
        var container = containerFactory.createContainer(TopicPartitionOffset("order_service_responses", 0))
        container.containerProperties.setGroupId("warehouse_service_group_id_3")

        val consumerAwareBatchErrorHandler = ConsumerAwareBatchErrorHandler { thrownException, data, consumer ->
            if (thrownException is SerializationException) {
                consumer.seek(TopicPartition("order_service_responses", 0), consumer.position(TopicPartition("order_service_responses", 0)) + 1)
            }
        }
        container.setBatchErrorHandler(consumerAwareBatchErrorHandler)

        return container
    }

    @Bean
    fun hasProductBeenBoughtByCustomerConcurrentKafkaListenerContainerFactory(@Qualifier("hasProductBeenBoughtByCustomerConsumerFactory") consumerFactory: ConsumerFactory<String, Boolean>): ConcurrentKafkaListenerContainerFactory<String, Boolean> {
        var container = ConcurrentKafkaListenerContainerFactory<String, Boolean>()
        container.consumerFactory = consumerFactory

        return container
    }

    @Bean
    fun hasProductBeenBoughtByCustomerConsumerFactory(): ConsumerFactory<String, Boolean> {
        var config = mapOf(
            ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG to "localhost:9092",
            ConsumerConfig.GROUP_ID_CONFIG to "warehouse_service_group_id_3",
            ConsumerConfig.AUTO_OFFSET_RESET_CONFIG to "latest",
            ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG to StringDeserializer::class.java,
            ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG to JsonDeserializer::class.java,
            JsonDeserializer.TRUSTED_PACKAGES to "*"
        )

        return DefaultKafkaConsumerFactory(config)
    }

    @Bean
    fun hasProductBeenBoughtByCustomerReplyingKafkaTemplate(@Qualifier("hasProductBeenBoughtByCustomerProducerFactory") producerFactory: ProducerFactory<String, Long>, @Qualifier("hasProductBeenBoughtByCustomerConcurrentMessageListenerContainer") container: ConcurrentMessageListenerContainer<String, Boolean>): ReplyingKafkaTemplate<String, Long, Boolean> {
        val replyingKafkaTemplate = ReplyingKafkaTemplate(producerFactory, container)
        replyingKafkaTemplate.setSharedReplyTopic(true)
        // don't use the replyTimeout parameter of sendAndReceive: it is neglected, probably for a bug
        replyingKafkaTemplate.setDefaultReplyTimeout(Duration.ofSeconds(15))
        return replyingKafkaTemplate
    }

}