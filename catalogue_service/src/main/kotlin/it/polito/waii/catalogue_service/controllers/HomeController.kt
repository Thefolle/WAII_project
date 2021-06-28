package it.polito.waii.catalogue_service.controllers

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class HomeController {
    @GetMapping("/")
    fun home()="Welcome to the WA2 Group19 Final Project Web Page!"
}
