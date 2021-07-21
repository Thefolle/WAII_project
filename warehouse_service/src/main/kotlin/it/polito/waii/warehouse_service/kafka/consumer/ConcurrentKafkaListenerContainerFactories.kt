package it.polito.waii.warehouse_service.kafka.consumer

import org.springframework.context.annotation.Bean
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory
import org.springframework.kafka.core.ConsumerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.support.converter.MessageConverter
import org.springframework.stereotype.Component

@Component
class ConcurrentKafkaListenerContainerFactories {

    @Bean
    fun updateQuantityConcurrentKafkaListenerContainerFactory(consumerFactory: ConsumerFactory<Any, Any>, messageConverter: MessageConverter, replyKafkaTemplate: KafkaTemplate<String, Long>): ConcurrentKafkaListenerContainerFactory<Any, Any> {
        val concurrentKafkaListenerContainerFactory = ConcurrentKafkaListenerContainerFactory<Any, Any>()
        concurrentKafkaListenerContainerFactory.consumerFactory = consumerFactory
        concurrentKafkaListenerContainerFactory.setMessageConverter(messageConverter)
        concurrentKafkaListenerContainerFactory.setReplyTemplate(replyKafkaTemplate)

        return concurrentKafkaListenerContainerFactory
    }

}