package it.polito.waii.order_service.kafka.consumer

import com.fasterxml.jackson.databind.ObjectMapper
import it.polito.waii.order_service.dtos.OrderDto
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.kafka.common.serialization.StringSerializer
import org.apache.kafka.common.serialization.VoidDeserializer
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory
import org.springframework.kafka.core.*
import org.springframework.kafka.support.serializer.JsonDeserializer
import org.springframework.kafka.support.serializer.JsonSerializer

@Configuration
class GetOrders {

    @Bean
    fun getOrdersConsumerFactory(): ConsumerFactory<String, Void> {
        var config = mapOf(
            ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG to "kafka:9092",
            ConsumerConfig.GROUP_ID_CONFIG to "order_service_group_id",
            ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG to StringDeserializer::class.java,
            ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG to VoidDeserializer::class.java,
            ConsumerConfig.AUTO_OFFSET_RESET_CONFIG to "latest",
            JsonDeserializer.TRUSTED_PACKAGES to "*"
        )

        return DefaultKafkaConsumerFactory(config)
    }

    @Bean
    fun getOrdersConcurrentKafkaListenerContainerFactory(@Qualifier("getOrdersConsumerFactory") consumerFactory: ConsumerFactory<String, Void>, kafkaTemplate: KafkaTemplate<String, Set<OrderDto>>): ConcurrentKafkaListenerContainerFactory<String, Void> {
        var container = ConcurrentKafkaListenerContainerFactory<String, Void>()
        container.consumerFactory = consumerFactory
        container.setReplyTemplate(kafkaTemplate)

        return container
    }

    @Bean
    fun getOrdersProducerFactory(): ProducerFactory<String, Set<OrderDto>> {
        var config = mapOf(
            ProducerConfig.BOOTSTRAP_SERVERS_CONFIG to "kafka:9092"
        )

        val objectMapper = ObjectMapper()
        val type = objectMapper.typeFactory.constructParametricType(Set::class.java, OrderDto::class.java)

        return DefaultKafkaProducerFactory(config, StringSerializer(), JsonSerializer(type, objectMapper))
    }

    @Bean
    fun getOrdersKafkaTemplate(producerFactory: ProducerFactory<String, Set<OrderDto>>): KafkaTemplate<String, Set<OrderDto>> {
        return KafkaTemplate(producerFactory)
    }

}
