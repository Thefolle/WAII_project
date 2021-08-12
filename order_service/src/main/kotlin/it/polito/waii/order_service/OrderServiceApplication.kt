package it.polito.waii.order_service

import org.neo4j.driver.Driver
import org.springframework.boot.ApplicationRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cloud.netflix.eureka.EnableEurekaClient
import org.springframework.context.annotation.Bean
import org.springframework.data.neo4j.core.ReactiveDatabaseSelectionProvider
import org.springframework.data.neo4j.core.transaction.ReactiveNeo4jTransactionManager
import org.springframework.data.neo4j.repository.config.ReactiveNeo4jRepositoryConfigurationExtension


@SpringBootApplication
@EnableEurekaClient
class OrderServiceApplication {

    // configure this bean since Spring Boot doesn't autoconfigure it automatically yet
    @Bean(ReactiveNeo4jRepositoryConfigurationExtension.DEFAULT_TRANSACTION_MANAGER_BEAN_NAME)
    fun reactiveTransactionManager(driver: Driver, reactiveDatabaseSelectionProvider: ReactiveDatabaseSelectionProvider): ReactiveNeo4jTransactionManager {
        return ReactiveNeo4jTransactionManager(driver, reactiveDatabaseSelectionProvider)
    }

}

fun main(args: Array<String>) {
    runApplication<OrderServiceApplication>(*args)
}
