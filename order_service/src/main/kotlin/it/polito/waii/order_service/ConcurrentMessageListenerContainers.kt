package it.polito.waii.order_service

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer

@Configuration
class ConcurrentMessageListenerContainers {

    @Bean
    fun longConcurrentMessageListenerContainer(@Qualifier("longConcurrentKafkaListenerContainerFactory") containerFactory: ConcurrentKafkaListenerContainerFactory<String, Long>): ConcurrentMessageListenerContainer<String, Long> {
        var container = containerFactory.createContainer("order_service_responses")
        container.containerProperties.setGroupId("outer_service_group_id")

        return container
    }

    //    @Bean
//    fun orderDtoConcurrentMessageListenerContainer(@Qualifier("createOrderConcurrentKafkaListenerContainerFactory") containerFactory: ConcurrentKafkaListenerContainerFactory<String, OrderDto>): ConcurrentMessageListenerContainer<String, OrderDto> {
//        var container = containerFactory.createContainer("replies")
//        container.containerProperties.setGroupId("order_service_group_id")
//
//        return container
//    }

}