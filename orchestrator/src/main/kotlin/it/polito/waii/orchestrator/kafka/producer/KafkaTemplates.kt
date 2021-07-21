package it.polito.waii.orchestrator.kafka.producer

import it.polito.waii.orchestrator.dtos.UpdateQuantityDtoKafka
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.core.ProducerFactory

@Configuration
class KafkaTemplates {

    @Bean
    fun longKafkaTemplate(producerFactory: ProducerFactory<String, Long>): KafkaTemplate<String, Long> {
        return KafkaTemplate(producerFactory)
    }

    @Bean
    fun exceptionKafkaTemplate(producerFactory: ProducerFactory<String, Any>): KafkaTemplate<String, Any> {
        return KafkaTemplate(producerFactory)
    }

    @Bean
    fun updateQuantityDtoKafkaTemplate(producerFactory: ProducerFactory<String, UpdateQuantityDtoKafka>): KafkaTemplate<String, UpdateQuantityDtoKafka> {
        return KafkaTemplate(producerFactory)
    }

}