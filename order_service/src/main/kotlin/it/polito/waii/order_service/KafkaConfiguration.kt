package it.polito.waii.order_service

import it.polito.waii.order_service.dtos.OrderDto
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.kafka.common.serialization.VoidDeserializer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.annotation.EnableKafka
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory
import org.springframework.kafka.core.ConsumerFactory
import org.springframework.kafka.core.DefaultKafkaConsumerFactory
import org.springframework.kafka.support.KafkaNull
import org.springframework.kafka.support.converter.JsonMessageConverter
import org.springframework.kafka.support.converter.StringJsonMessageConverter
import org.springframework.kafka.support.serializer.JsonDeserializer

@Configuration
@EnableKafka
class KafkaConfiguration {

    @Bean
    fun orderDtoConsumerFactory(): ConsumerFactory<String, OrderDto> {
        var config = mapOf(
            Pair(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092"),
            ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG to StringDeserializer::class.java,
            ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG to JsonDeserializer::class.java,
            Pair(ConsumerConfig.GROUP_ID_CONFIG, "OrderGroupId"),
            ConsumerConfig.AUTO_OFFSET_RESET_CONFIG to "latest",
            JsonDeserializer.TRUSTED_PACKAGES to "*"
        )

        return DefaultKafkaConsumerFactory(config)
    }

    @Bean
    fun orderDtoConcurrentKafkaListenerContainerFactory(consumerFactory: ConsumerFactory<String, OrderDto>): ConcurrentKafkaListenerContainerFactory<String, OrderDto> {
        var container = ConcurrentKafkaListenerContainerFactory<String, OrderDto>()
        container.consumerFactory = consumerFactory

        return container
    }

    @Bean
    fun voidConsumerFactory(): ConsumerFactory<String, Void> {
        var config = mapOf(
            Pair(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092"),
            Pair(ConsumerConfig.GROUP_ID_CONFIG, "OrderGroupId"),
            Pair(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer::class.java),
            Pair(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, VoidDeserializer::class.java),
            ConsumerConfig.AUTO_OFFSET_RESET_CONFIG to "latest",
        )

        return DefaultKafkaConsumerFactory(config)
    }

    @Bean
    fun voidConcurrentKafkaListenerContainerFactory(consumerFactory: ConsumerFactory<String, Void>): ConcurrentKafkaListenerContainerFactory<String, Void> {
        var container = ConcurrentKafkaListenerContainerFactory<String, Void>()
        container.consumerFactory = consumerFactory

        return container
    }



}