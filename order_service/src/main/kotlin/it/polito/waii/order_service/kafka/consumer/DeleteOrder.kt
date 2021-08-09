package it.polito.waii.order_service.kafka.consumer

import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.serialization.LongDeserializer
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.kafka.common.serialization.StringSerializer
import org.apache.kafka.common.serialization.VoidSerializer
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory
import org.springframework.kafka.core.*

@Configuration
class DeleteOrder {

    @Bean
    fun deleteOrderConcurrentKafkaListenerContainerFactory(@Qualifier("deleteOrderConsumerFactory") consumerFactory: ConsumerFactory<String, Long>, @Qualifier("deleteOrderKafkaTemplate") replyTemplate: KafkaTemplate<String, Void>): ConcurrentKafkaListenerContainerFactory<String, Long> {
        var container = ConcurrentKafkaListenerContainerFactory<String, Long>()
        container.consumerFactory = consumerFactory
        container.setReplyTemplate(replyTemplate)

        return container
    }

    @Bean
    fun deleteOrderConsumerFactory(): ConsumerFactory<String, Long> {
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
    fun deleteOrderKafkaTemplate(@Qualifier("deleteOrderProducerFactory") producerFactory: ProducerFactory<String, Void>): KafkaTemplate<String, Void> {
        return KafkaTemplate(producerFactory)
    }

    @Bean
    fun deleteOrderProducerFactory(): ProducerFactory<String, Void> {
        var config = mapOf(
            ProducerConfig.BOOTSTRAP_SERVERS_CONFIG to "localhost:9092",
            ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG to StringSerializer::class.java,
            ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG to VoidSerializer::class.java
        )

        return DefaultKafkaProducerFactory(config)
    }

}