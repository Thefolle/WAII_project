package it.polito.waii.order_service.kafka.consumer

import it.polito.waii.order_service.dtos.OrderDto
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.serialization.LongDeserializer
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.kafka.common.serialization.StringSerializer
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory
import org.springframework.kafka.core.*
import org.springframework.kafka.support.serializer.JsonSerializer
import java.time.Instant

@Configuration
class GetOrderById {

    @Bean
    fun getOrderByIdConsumerFactory(): ConsumerFactory<String, Long> {
        var config = mapOf(
            ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG to "localhost:9092",
            ConsumerConfig.GROUP_ID_CONFIG to "order_service_group_id",
            ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG to StringDeserializer::class.java,
            ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG to LongDeserializer::class.java,
            ConsumerConfig.AUTO_OFFSET_RESET_CONFIG to "latest"
        )

        return DefaultKafkaConsumerFactory(config)
    }

    @Bean
    fun getOrderByIdConcurrentKafkaListenerContainerFactory(@Qualifier("getOrderByIdConsumerFactory") consumerFactory: ConsumerFactory<String, Long>, @Qualifier("getOrderByIdKafkaTemplate") replyTemplate: KafkaTemplate<String, OrderDto>): ConcurrentKafkaListenerContainerFactory<String, Long> {
        var containerFactory = ConcurrentKafkaListenerContainerFactory<String, Long>()
        containerFactory.consumerFactory = consumerFactory
        containerFactory.setReplyTemplate(replyTemplate)

        val containerFactoryInitializationTimestamp = Instant.now().toEpochMilli()
        containerFactory.setRecordFilterStrategy {
            it.timestamp() < containerFactoryInitializationTimestamp
        }
        containerFactory.setAckDiscarded(true)

        return containerFactory
    }

    @Bean
    fun getOrderByIdProducerFactory(): ProducerFactory<String, OrderDto> {
        var config = mapOf(
            ProducerConfig.BOOTSTRAP_SERVERS_CONFIG to "localhost:9092",
            ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG to StringSerializer::class.java,
            ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG to JsonSerializer::class.java
        )

        return DefaultKafkaProducerFactory(config)
    }

    @Bean
    fun getOrderByIdKafkaTemplate(@Qualifier("getOrderByIdProducerFactory") producerFactory: ProducerFactory<String, OrderDto>): KafkaTemplate<String, OrderDto> {
        return KafkaTemplate(producerFactory)
    }

}
