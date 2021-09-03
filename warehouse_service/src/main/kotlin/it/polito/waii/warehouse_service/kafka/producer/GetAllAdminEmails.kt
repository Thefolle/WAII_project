package it.polito.waii.warehouse_service.kafka.producer

import com.fasterxml.jackson.databind.ObjectMapper
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
import java.time.Duration

@Configuration
class GetAllAdminEmails {

    @Bean
    fun getAllAdminEmailsProducerFactory(): ProducerFactory<String, Void> {
        var config = mapOf(
            ProducerConfig.BOOTSTRAP_SERVERS_CONFIG to "localhost:9092",
            ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG to StringSerializer::class.java,
            ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG to VoidSerializer::class.java
        )

        return DefaultKafkaProducerFactory(config)
    }

    @Bean
    fun getAllAdminEmailsConcurrentMessageListenerContainer(@Qualifier("getAllAdminEmailsConcurrentKafkaListenerContainerFactory") containerFactory: ConcurrentKafkaListenerContainerFactory<String, Set<String>>): ConcurrentMessageListenerContainer<String, Set<String>> {
        var container = containerFactory.createContainer("catalogue_service_responses")
        container.containerProperties.setGroupId("catalogue_service_group_id")

        val consumerAwareBatchErrorHandler = ConsumerAwareBatchErrorHandler { thrownException, data, consumer ->
            if (thrownException is SerializationException) {
                consumer.seek(TopicPartition("order_service_responses", 0), consumer.position(TopicPartition("order_service_responses", 0)) + 1)
            }
        }
        container.setBatchErrorHandler(consumerAwareBatchErrorHandler)

        return container
    }

    @Bean
    fun getAllAdminEmailsConcurrentKafkaListenerContainerFactory(@Qualifier("getAllAdminEmailsConsumerFactory") consumerFactory: ConsumerFactory<String, Set<String>>): ConcurrentKafkaListenerContainerFactory<String, Set<String>> {
        var container = ConcurrentKafkaListenerContainerFactory<String, Set<String>>()
        container.consumerFactory = consumerFactory

        return container
    }

    @Bean
    fun getAllAdminEmailsConsumerFactory(): ConsumerFactory<String, Set<String>> {
        var config = mapOf(
            ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG to "localhost:9092",
            ConsumerConfig.GROUP_ID_CONFIG to "catalogue_service_group_id",
            ConsumerConfig.AUTO_OFFSET_RESET_CONFIG to "latest",
            JsonDeserializer.TRUSTED_PACKAGES to "*"
        )
        val objectMapper = ObjectMapper()
        val type = objectMapper.typeFactory.constructParametricType(Set::class.java, String::class.java)

        return DefaultKafkaConsumerFactory(config, StringDeserializer(), JsonDeserializer(type, objectMapper, false))
    }

    @Bean
    fun getAllAdminEmailsReplyingKafkaTemplate(@Qualifier("getAllAdminEmailsProducerFactory") producerFactory: ProducerFactory<String, Void>, @Qualifier("getAllAdminEmailsConcurrentMessageListenerContainer") container: ConcurrentMessageListenerContainer<String, Set<String>>): ReplyingKafkaTemplate<String, Void, Set<String>> {
        val replyingKafkaTemplate = ReplyingKafkaTemplate(producerFactory, container)
        replyingKafkaTemplate.setSharedReplyTopic(true)
        // don't use the replyTimeout parameter of sendAndReceive: it is neglected, probably for a bug
        replyingKafkaTemplate.setDefaultReplyTimeout(Duration.ofSeconds(15))
        return replyingKafkaTemplate
    }

}
