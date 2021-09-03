package it.polito.waii.orchestrator

import org.springframework.beans.factory.config.ConfigurableBeanFactory
import org.springframework.beans.factory.getBeanProvider
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import java.time.Instant

@SpringBootApplication
class OrchestratorApplication {



}

fun main(args: Array<String>) {
    runApplication<OrchestratorApplication>(*args)
}
