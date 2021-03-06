package it.polito.waii.orchestrator.kafka.producer

import com.fasterxml.jackson.databind.ObjectMapper
import it.polito.waii.orchestrator.dtos.UpdateQuantityDtoKafka
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
import org.springframework.kafka.core.*
import org.springframework.kafka.core.ProducerFactory
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
class CheckWarehouse {

    @Bean
    fun checkWarehouseProducerFactory(): ProducerFactory<String, Set<UpdateQuantityDtoKafka>> {
        var config = mapOf<String, String>(
            ProducerConfig.BOOTSTRAP_SERVERS_CONFIG to "localhost:9092",
        )

        val objectMapper = ObjectMapper()
        val type = objectMapper.typeFactory.constructParametricType(Set::class.java, UpdateQuantityDtoKafka::class.java)

        return DefaultKafkaProducerFactory(config, StringSerializer(), JsonSerializer(type, objectMapper))

        return DefaultKafkaProducerFactory(config)
    }

    @Bean
    fun checkWarehouseExceptionProducerFactory(): ProducerFactory<String, Any> {
        var config = mapOf(
            ProducerConfig.BOOTSTRAP_SERVERS_CONFIG to "localhost:9092",
            ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG to StringSerializer::class.java,
            ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG to JsonSerializer::class.java
        )

        return DefaultKafkaProducerFactory(config)
    }

    @Bean
    fun checkWarehouseKafkaTemplate(producerFactory: ProducerFactory<String, Set<UpdateQuantityDtoKafka>>): KafkaTemplate<String, Set<UpdateQuantityDtoKafka>> {
        return KafkaTemplate(producerFactory)
    }

    @Bean
    fun checkWarehouseReplyingKafkaTemplate(producerFactory: ProducerFactory<String, Set<UpdateQuantityDtoKafka>>, @Qualifier("checkWarehouseConcurrentMessageListenerContainer") container: ConcurrentMessageListenerContainer<String, Float>): ReplyingKafkaTemplate<String, Set<UpdateQuantityDtoKafka>, Float> {
        val replyingKafkaTemplate = ReplyingKafkaTemplate(producerFactory, container)
        replyingKafkaTemplate.setSharedReplyTopic(true)
        // don't use the replyTimeout parameter of sendAndReceive: it is neglected, probably for a bug
        replyingKafkaTemplate.setDefaultReplyTimeout(Duration.ofSeconds(10))
        return replyingKafkaTemplate
    }

    @Bean
    fun checkWarehouseConsumerFactory(): ConsumerFactory<String, Float> {
        var config = mapOf(
            ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG to "localhost:9092",
            ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG to StringDeserializer::class.java,
            ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG to FloatDeserializer::class.java,
            ConsumerConfig.GROUP_ID_CONFIG to "orchestrator_group_id_0",
            ConsumerConfig.AUTO_OFFSET_RESET_CONFIG to "latest"
        )

        return DefaultKafkaConsumerFactory(config)
    }

    @Bean
    fun checkWarehouseExceptionKafkaTemplate(@Qualifier("checkWarehouseExceptionProducerFactory") producerFactory: ProducerFactory<String, Any>): KafkaTemplate<String, Any> {
        return KafkaTemplate(producerFactory)
    }

    @Bean
    fun checkWarehouseConcurrentKafkaListenerContainerFactory(@Qualifier("checkWarehouseConsumerFactory") consumerFactory: ConsumerFactory<String, Float>, messageConverter: StringJsonMessageConverter, @Qualifier("checkWarehouseExceptionKafkaTemplate") exceptionReplyTemplate: KafkaTemplate<String, Any>): ConcurrentKafkaListenerContainerFactory<String, Float> {
        var containerFactory = ConcurrentKafkaListenerContainerFactory<String, Float>()
        containerFactory.containerProperties.setGroupId("orchestrator_group_id_0")
        containerFactory.consumerFactory = consumerFactory
        containerFactory.setMessageConverter(messageConverter)

        containerFactory.setErrorHandler(SeekToCurrentErrorHandler(DeadLetterPublishingRecoverer(exceptionReplyTemplate) { _, _ ->
            TopicPartition(
                "exceptions",
                0
            )
        }) { BackOffExecution { BackOffExecution.STOP } })

        return containerFactory
    }

    @Bean
    fun checkWarehouseConcurrentMessageListenerContainer(@Qualifier("checkWarehouseConcurrentKafkaListenerContainerFactory") containerFactory: ConcurrentKafkaListenerContainerFactory<String, Float>): ConcurrentMessageListenerContainer<String, Float> {
        var container = containerFactory.createContainer("warehouse_service_responses")
        container.containerProperties.setGroupId("orchestrator_group_id_0")

        val consumerAwareBatchErrorHandler = ConsumerAwareBatchErrorHandler { thrownException, data, consumer ->
            if (thrownException is SerializationException) {
                consumer.seek(TopicPartition("warehouse_service_responses", 0), consumer.position(TopicPartition("warehouse_service_responses", 0)) + 1)
            }
        }
        container.setBatchErrorHandler(consumerAwareBatchErrorHandler)

        return container
    }

}
