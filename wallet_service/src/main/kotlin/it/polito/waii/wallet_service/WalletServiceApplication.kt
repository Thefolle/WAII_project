package it.polito.waii.wallet_service

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cloud.netflix.eureka.EnableEurekaClient

@SpringBootApplication
@EnableEurekaClient
class WalletServiceApplication

fun main(args: Array<String>) {
    runApplication<WalletServiceApplication>(*args)
}
