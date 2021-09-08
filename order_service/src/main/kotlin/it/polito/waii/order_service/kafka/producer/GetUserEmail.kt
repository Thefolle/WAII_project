package it.polito.waii.order_service.kafka.producer

import com.fasterxml.jackson.databind.ObjectMapper
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.TopicPartition
import org.apache.kafka.common.errors.SerializationException
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
import org.springframework.kafka.support.serializer.JsonDeserializer
import java.time.Duration

@Configuration
class GetUserEmail {

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
    fun getUserEmailConcurrentMessageListenerContainer(@Qualifier("getUserEmailConcurrentKafkaListenerContainerFactory") containerFactory: ConcurrentKafkaListenerContainerFactory<String, String>): ConcurrentMessageListenerContainer<String, String> {
        var container = containerFactory.createContainer("catalogue_service_responses")
        container.containerProperties.setGroupId("order_service_group_id_30")

        val consumerAwareBatchErrorHandler = ConsumerAwareBatchErrorHandler { thrownException, data, consumer ->
            if (thrownException is SerializationException) {
                consumer.seek(TopicPartition("catalogue_service_responses", 1), consumer.position(TopicPartition("catalogue_service_responses", 1)) + 1)
            }
        }
        container.setBatchErrorHandler(consumerAwareBatchErrorHandler)

        return container
    }

    @Bean
    fun getUserEmailConcurrentKafkaListenerContainerFactory(@Qualifier("getUserEmailConsumerFactory") consumerFactory: ConsumerFactory<String, String>): ConcurrentKafkaListenerContainerFactory<String, String> {
        var container = ConcurrentKafkaListenerContainerFactory<String, String>()
        container.consumerFactory = consumerFactory

        return container
    }

    @Bean
    fun getUserEmailConsumerFactory(): ConsumerFactory<String, String> {
        var config = mapOf(
            ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG to "localhost:9092",
            ConsumerConfig.GROUP_ID_CONFIG to "order_service_group_id_30",
            ConsumerConfig.AUTO_OFFSET_RESET_CONFIG to "latest",
            ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG to StringDeserializer::class.java,
            ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG to StringDeserializer::class.java
        )

        return DefaultKafkaConsumerFactory(config)
    }

    @Bean
    fun getUserEmailReplyingKafkaTemplate(@Qualifier("getUserEmailProducerFactory") producerFactory: ProducerFactory<String, String>, @Qualifier("getUserEmailConcurrentMessageListenerContainer") container: ConcurrentMessageListenerContainer<String, String>): ReplyingKafkaTemplate<String, String, String> {
        val replyingKafkaTemplate = ReplyingKafkaTemplate(producerFactory, container)
        replyingKafkaTemplate.setSharedReplyTopic(true)
        // don't use the replyTimeout parameter of sendAndReceive: it is neglected, probably for a bug
        replyingKafkaTemplate.setDefaultReplyTimeout(Duration.ofSeconds(15))
        return replyingKafkaTemplate
    }

}