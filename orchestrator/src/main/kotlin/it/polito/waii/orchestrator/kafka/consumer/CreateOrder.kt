package it.polito.waii.orchestrator.kafka.consumer

import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.TopicPartition
import org.apache.kafka.common.serialization.FloatSerializer
import org.apache.kafka.common.serialization.LongSerializer
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.kafka.common.serialization.StringSerializer
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory
import org.springframework.kafka.core.*
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer
import org.springframework.kafka.listener.SeekToCurrentErrorHandler
import org.springframework.kafka.support.converter.StringJsonMessageConverter
import org.springframework.kafka.support.serializer.JsonSerializer
import org.springframework.util.backoff.BackOffExecution
import java.time.Instant

@Configuration
class CreateOrder {

    @Bean
    fun createOrderConsumerFactory(): ConsumerFactory<String, String> {
        var config = mapOf(
            ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG to "localhost:9092",
            ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG to StringDeserializer::class.java,
            ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG to StringDeserializer::class.java,
            ConsumerConfig.GROUP_ID_CONFIG to "orchestrator_group_id_2",
            ConsumerConfig.AUTO_OFFSET_RESET_CONFIG to "latest"
        )

        return DefaultKafkaConsumerFactory(config)
    }

    @Bean
    fun createOrderExceptionKafkaTemplate(@Qualifier("createOrderExceptionProducerFactory") producerFactory: ProducerFactory<String, Any>): KafkaTemplate<String, Any> {
        return KafkaTemplate(producerFactory)
    }

    @Bean
    fun createOrderConcurrentKafkaListenerContainerFactory(@Qualifier("createOrderConsumerFactory") consumerFactory: ConsumerFactory<String, String>, messageConverter: StringJsonMessageConverter, replyTemplate: KafkaTemplate<String, Float>, @Qualifier("createOrderExceptionKafkaTemplate") exceptionReplyTemplate: KafkaTemplate<String, Any>): ConcurrentKafkaListenerContainerFactory<String, String> {
        var containerFactory = ConcurrentKafkaListenerContainerFactory<String, String>()
        containerFactory.containerProperties.setGroupId("orchestrator_group_id_2")
        containerFactory.consumerFactory = consumerFactory
        containerFactory.setMessageConverter(messageConverter)
        containerFactory.setReplyTemplate(replyTemplate)

        // The backoff controls how many times Kafka will attempt to send the same request on a controller;
        // in this case, no additional attempts are allowed
        containerFactory.setErrorHandler(SeekToCurrentErrorHandler(DeadLetterPublishingRecoverer(exceptionReplyTemplate) { _, _ ->
            TopicPartition(
                "exceptions",
                0
            )
        }) { BackOffExecution { BackOffExecution.STOP } })

        val containerFactoryInitializationTimestamp = Instant.now().toEpochMilli()
        containerFactory.setRecordFilterStrategy {
            it.timestamp() < containerFactoryInitializationTimestamp
        }
        containerFactory.setAckDiscarded(true)

        return containerFactory
    }

    @Bean
    fun createOrderProducerFactory(): ProducerFactory<String, Float> {
        var config = mapOf(
            ProducerConfig.BOOTSTRAP_SERVERS_CONFIG to "localhost:9092",
            ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG to StringSerializer::class.java,
            ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG to FloatSerializer::class.java
        )

        return DefaultKafkaProducerFactory(config)
    }

    @Bean
    fun createOrderExceptionProducerFactory(): ProducerFactory<String, Any> {
        var config = mapOf(
            ProducerConfig.BOOTSTRAP_SERVERS_CONFIG to "localhost:9092",
            ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG to StringSerializer::class.java,
            ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG to JsonSerializer::class.java
        )

        return DefaultKafkaProducerFactory(config)
    }

    @Bean
    fun createOrderKafkaTemplate(producerFactory: ProducerFactory<String, Float>): KafkaTemplate<String, Float> {
        return KafkaTemplate(producerFactory)
    }

}
