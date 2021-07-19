package it.polito.waii.orchestrator.kafka.consumer

import it.polito.waii.orchestrator.dtos.OrderDtoOrchestrator
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory
import org.springframework.kafka.core.ConsumerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.support.converter.StringJsonMessageConverter

@Configuration
class ConcurrentKafkaListenerContainerFactories {

    @Bean
    fun createOrderOrchestratorConcurrentKafkaListenerContainerFactory(consumerFactory: ConsumerFactory<String, String>, messageConverter: StringJsonMessageConverter, replyTemplate: KafkaTemplate<String, Long>): ConcurrentKafkaListenerContainerFactory<String, String> {
        var container = ConcurrentKafkaListenerContainerFactory<String, String>()
        container.consumerFactory = consumerFactory
        container.setMessageConverter(messageConverter)
        container.setReplyTemplate(replyTemplate)

        return container
    }

}