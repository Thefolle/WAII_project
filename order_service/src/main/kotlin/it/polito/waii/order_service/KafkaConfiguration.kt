package it.polito.waii.order_service

import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.StringDeserializer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.annotation.EnableKafka
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory
import org.springframework.kafka.core.ConsumerFactory
import org.springframework.kafka.core.DefaultKafkaConsumerFactory

@Configuration
@EnableKafka
class KafkaConfiguration {

    @Bean
    fun consumerFactory(): ConsumerFactory<String, String> {
        var config = mapOf(
            Pair(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092"),
            Pair(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer::class.java),
            Pair(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer::class.java)
        )

        return DefaultKafkaConsumerFactory(config)
    }

    @Bean
    fun concurrentKafkaListenerContainerFactory(): ConcurrentKafkaListenerContainerFactory<String, String> {
        var container = ConcurrentKafkaListenerContainerFactory<String, String>()
        container.consumerFactory = consumerFactory()

        return container
    }

}