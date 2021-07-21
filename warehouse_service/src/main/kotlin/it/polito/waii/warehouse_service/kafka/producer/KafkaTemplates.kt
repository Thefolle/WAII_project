package it.polito.waii.warehouse_service.kafka.producer

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

}