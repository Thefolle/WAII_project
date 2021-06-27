package it.polito.waii.catalogue_service

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class CatalogueServiceApplication

fun main(args: Array<String>) {
    runApplication<CatalogueServiceApplication>(*args)
}
