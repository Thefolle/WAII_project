package it.polito.waii.catalogue_service.controllers

import it.polito.waii.catalogue_service.services.UserService
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.annotation.TopicPartition
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.messaging.handler.annotation.SendTo
import org.springframework.stereotype.Component

@Component
class KafkaController(val userService: UserService) {

    @SendTo("catalogue_service_responses")
    @KafkaListener(
        containerFactory = "getAllAdminEmailsConcurrentKafkaListenerContainerFactory",
        topicPartitions = [TopicPartition(topic = "catalogue_service_requests", partitions = ["0"])],
        splitIterables = false
    )
    fun getAllAdminEmails(@Payload(required = false) empty: Void?): Set<String> {
        return userService
            .getAllAdminEmails()
    }

}