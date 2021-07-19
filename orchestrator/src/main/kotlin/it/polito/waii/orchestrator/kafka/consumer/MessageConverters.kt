package it.polito.waii.orchestrator.kafka.consumer

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.support.converter.MessageConverter
import org.springframework.kafka.support.converter.StringJsonMessageConverter

@Configuration
class MessageConverters {

    @Bean
    fun messageConverter(): StringJsonMessageConverter {
        return StringJsonMessageConverter()
    }

}