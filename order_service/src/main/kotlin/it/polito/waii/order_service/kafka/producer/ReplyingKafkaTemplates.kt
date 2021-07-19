package it.polito.waii.order_service.kafka.producer

import it.polito.waii.order_service.dtos.OrderDto
import it.polito.waii.order_service.dtos.PatchOrderDto
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.core.ProducerFactory
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer
import org.springframework.kafka.requestreply.ReplyingKafkaTemplate

@Configuration
class ReplyingKafkaTemplates {

    @Bean
    fun orderDtoLongReplyingKafkaTemplate(producerFactory: ProducerFactory<String, OrderDto>, @Qualifier("longConcurrentMessageListenerContainer") container: ConcurrentMessageListenerContainer<String, Long>): ReplyingKafkaTemplate<String, OrderDto, Long> {
        val replyingKafkaTemplate = ReplyingKafkaTemplate(producerFactory, container)
        replyingKafkaTemplate.setSharedReplyTopic(true)
        return replyingKafkaTemplate
    }

    @Bean
    fun orderDtoLong2ReplyingKafkaTemplate(producerFactory: ProducerFactory<String, OrderDto>, @Qualifier("long2ConcurrentMessageListenerContainer") container: ConcurrentMessageListenerContainer<String, Long>): ReplyingKafkaTemplate<String, OrderDto, Long> {
        val replyingKafkaTemplate = ReplyingKafkaTemplate(producerFactory, container)
        replyingKafkaTemplate.setSharedReplyTopic(true)
        return replyingKafkaTemplate
    }

    @Bean
    fun voidSetOrderDtoReplyingKafkaTemplate(producerFactory: ProducerFactory<String, Void>, container: ConcurrentMessageListenerContainer<String, Set<OrderDto>>): ReplyingKafkaTemplate<String, Void, Set<OrderDto>> {
        val replyingKafkaTemplate = ReplyingKafkaTemplate(producerFactory, container)
        replyingKafkaTemplate.setSharedReplyTopic(true)
        return replyingKafkaTemplate
    }

    @Bean
    fun longOrderDtoReplyingKafkaTemplate(producerFactory: ProducerFactory<String, Long>, @Qualifier("orderDtoConcurrentMessageListenerContainer") container: ConcurrentMessageListenerContainer<String, OrderDto>): ReplyingKafkaTemplate<String, Long, OrderDto> {
        val replyingKafkaTemplate = ReplyingKafkaTemplate(producerFactory, container)
        replyingKafkaTemplate.setSharedReplyTopic(true)
        return replyingKafkaTemplate
    }

    @Bean
    fun patchOrderDtoVoidReplyingKafkaTemplate(producerFactory: ProducerFactory<String, PatchOrderDto>, container: ConcurrentMessageListenerContainer<String, Void>): ReplyingKafkaTemplate<String, PatchOrderDto, Void> {
        val replyingKafkaTemplate = ReplyingKafkaTemplate(producerFactory, container)
        replyingKafkaTemplate.setSharedReplyTopic(true)
        return replyingKafkaTemplate
    }

    @Bean
    fun longVoidReplyingKafkaTemplate(producerFactory: ProducerFactory<String, Long>, container: ConcurrentMessageListenerContainer<String, Void>): ReplyingKafkaTemplate<String, Long, Void> {
        val replyingKafkaTemplate = ReplyingKafkaTemplate(producerFactory, container)
        replyingKafkaTemplate.setSharedReplyTopic(true)
        return replyingKafkaTemplate
    }

}