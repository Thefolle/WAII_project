package it.polito.waii.gateway

import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig
import io.github.resilience4j.timelimiter.TimeLimiterConfig
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cloud.circuitbreaker.resilience4j.ReactiveResilience4JCircuitBreakerFactory
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JConfigBuilder
import org.springframework.cloud.gateway.route.RouteLocator
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder
import org.springframework.cloud.netflix.eureka.EnableEurekaClient
import org.springframework.context.annotation.Bean
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException
import java.time.Duration


@SpringBootApplication
@EnableEurekaClient
@RestController
class GatewayApplication{

    //handle service unavailability - circuit breaker definition
    @Bean
    fun defaultCustomizer(): org.springframework.cloud.client.circuitbreaker.Customizer<ReactiveResilience4JCircuitBreakerFactory> {
        return org.springframework.cloud.client.circuitbreaker.Customizer {
                factory -> factory.configureDefault {
                id -> Resilience4JConfigBuilder(id)
            .circuitBreakerConfig(CircuitBreakerConfig.ofDefaults())
            .timeLimiterConfig(TimeLimiterConfig.custom().timeoutDuration(Duration.ofSeconds(15)).build()) //requests that takes more than 15 seconds will open the circuit
            .build()
            }
        }
    }

    @Bean
    fun routes(builder: RouteLocatorBuilder): RouteLocator {
        //ogni route() identifica un redirect per un servizio

        return builder.routes()
            .route("Catalogue"){
                it -> it.path("/catalogue/**") //end point esterno da rimappare
                .filters { f -> f.circuitBreaker { it -> it.setFallbackUri("forward:/failureCatalogue") } //circuit breaker to handle unavailability
                                f.rewritePath("/catalogue", "/") }
                .uri("lb://catalogue") //nome del service interno
            }
            .route("Orders"){
                it -> it.path("/orders/**") //end point esterno da rimappare
                .filters { f -> f.circuitBreaker { it -> it.setFallbackUri("forward:/failureOrder") } //circuit breaker to handle unavailability
                    f.rewritePath("/orders", "/") }
                .uri("lb://order") //nome del service interno
            }
            .route("Products"){
                    it -> it.path("/products/**") //end point esterno da rimappare
                .filters { f -> f.circuitBreaker { it -> it.setFallbackUri("forward:/failureWarehouse") } //circuit breaker to handle unavailability
                    f.rewritePath("/products", "/products") }
                .uri("lb://warehouse") //nome del service interno
            }
            .route("Warehouses"){
                    it -> it.path("/warehouses/**") //end point esterno da rimappare
                .filters { f -> f.circuitBreaker { it -> it.setFallbackUri("forward:/failureWarehouse") } //circuit breaker to handle unavailability
                    f.rewritePath("/warehouses", "/") }
                .uri("lb://warehouse") //nome del service interno
            }
            .route("Wallets"){
                    it -> it.path("/wallets/**") //end point esterno da rimappare
                .filters { f -> f.circuitBreaker { it -> it.setFallbackUri("forward:/failureWallet") } //circuit breaker to handle unavailability
                                f.rewritePath("/wallets", "/") }
                .uri("lb://wallet") //nome del service interno
            }
            .build()
    }

    @GetMapping("/failureCatalogue")
    fun failureCatalogueGet() {
        throw ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "We are sorry, the Catalogue Service you are trying to reach is currently unavailable. Try again later!")
    }

    @PostMapping("/failureCatalogue")
    fun failureCataloguePost() {
        throw ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "We are sorry, the Catalogue Service you are trying to reach is currently unavailable. Try again later!")
    }

    @GetMapping("/failureOrder")
    fun failureOrderGet() {
        throw ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,"We are sorry, the Order Service you are trying to reach is currently unavailable. Try again later!")
    }

    @PostMapping("/failureOrder")
    fun failureOrderPost() {
        throw ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,"We are sorry, the Order Service you are trying to reach is currently unavailable. Try again later!")
    }

    @GetMapping("/failureWarehouse")
    fun failureWarehouseGet() {
        throw ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,"We are sorry, the Warehouse Service you are trying to reach is currently unavailable. Try again later!")
    }

    @PostMapping("/failureWarehouse")
    fun failureWarehousePost() {
        throw ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,"We are sorry, the Warehouse Service you are trying to reach is currently unavailable. Try again later!")
    }

    @GetMapping("/failureWallet")
    fun failureWalletGet() {
        throw ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,"We are sorry, the Wallet Service you are trying to reach is currently unavailable. Try again later!")
    }

    @PostMapping("/failureWallet")
    fun failureWalletPost() {
        throw ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,"We are sorry, the Wallet Service you are trying to reach is currently unavailable. Try again later!")
    }

}

fun main(args: Array<String>) {
    runApplication<GatewayApplication>(*args)
}
