package it.polito.waii.orchestrator.kafka.producer

import it.polito.waii.orchestrator.dtos.UpdateQuantityDtoKafka
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.core.ProducerFactory
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer
import org.springframework.kafka.requestreply.ReplyingKafkaTemplate

@Configuration
class ReplyingKafkaTemplates {

    @Bean
    fun updateOrderDtoVoidReplyingKafkaTemplate(producerFactory: ProducerFactory<String, UpdateQuantityDtoKafka>, container: ConcurrentMessageListenerContainer<String, Long>): ReplyingKafkaTemplate<String, UpdateQuantityDtoKafka, Long> {
        val replyingKafkaTemplate = ReplyingKafkaTemplate(producerFactory, container)
        replyingKafkaTemplate.setSharedReplyTopic(true)
        return replyingKafkaTemplate
    }

}