package it.polito.waii.order_service

import it.polito.waii.order_service.dtos.OrderDto
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.core.ProducerFactory
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer
import org.springframework.kafka.requestreply.ReplyingKafkaTemplate

@Configuration
class ReplyingKafkaTemplates {

    @Bean
    fun orderDtoLongReplyingKafkaTemplate(producerFactory: ProducerFactory<String, OrderDto>, container: ConcurrentMessageListenerContainer<String, Long>): ReplyingKafkaTemplate<String, OrderDto, Long> {
        return ReplyingKafkaTemplate(producerFactory, container)
    }

}