package it.polito.waii.catalogue_service.controllers

import it.polito.waii.catalogue_service.services.UserService
import org.springframework.http.HttpStatus
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.annotation.TopicPartition
import org.springframework.messaging.Message
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.messaging.handler.annotation.SendTo
import org.springframework.messaging.support.MessageBuilder
import org.springframework.stereotype.Component
import org.springframework.web.server.ResponseStatusException

@Component
class KafkaController(val userService: UserService) {

    @SendTo("catalogue_service_responses")
    @KafkaListener(
        containerFactory = "getAllAdminEmailsConcurrentKafkaListenerContainerFactory",
        topicPartitions = [TopicPartition(topic = "catalogue_service_requests", partitions = ["0"])],
        splitIterables = false
    )
    fun getAllAdminEmails(@Payload(required = false) empty: Void?): Message<Set<String>> {
        return MessageBuilder
            .withPayload(
                userService
                    .getAllAdminEmails()
            )
            .setHeader("hasException", false)
            .build()
    }

    @SendTo("catalogue_service_responses")
    @KafkaListener(
        containerFactory = "getUserEmailConcurrentKafkaListenerContainerFactory",
        topicPartitions = [TopicPartition(topic = "catalogue_service_requests", partitions = ["1"])]
    )
    fun getUserEmail(username: String): Message<String> {
        try {
            return MessageBuilder
                .withPayload(
                    userService
                        .getUserEmail(username)
                )
                .setHeader("hasException", false)
                .build()
        } catch (exception: ResponseStatusException) {
            return MessageBuilder
                .withPayload("")
                .setHeader("hasException", true)
                .setHeader("exceptionMessage", exception.reason)
                .setHeader("exceptionStatus", exception.rawStatusCode)
                .build()
        }
    }

}