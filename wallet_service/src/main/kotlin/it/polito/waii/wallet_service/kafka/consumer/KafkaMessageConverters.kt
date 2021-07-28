package it.polito.waii.wallet_service.kafka.consumer

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.support.converter.MessageConverter
import org.springframework.kafka.support.converter.StringJsonMessageConverter

@Configuration
class KafkaMessageConverters {

    @Bean
    fun stringJsonMessageConverter(): StringJsonMessageConverter {
        return StringJsonMessageConverter()
    }

}