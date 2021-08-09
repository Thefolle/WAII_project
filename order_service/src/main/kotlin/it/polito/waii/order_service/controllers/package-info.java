/**
 *
 * This package has two controllers: OrderController and TestController.
 * The former does the actual work, while the latter translates the incoming REST requests to Kafka requests.
 * Moreover, the latter allows to test the OrderController through a REST client.
 *
 * TestController has some limitations: the time needed to perform a request may exceed the reply timeout
 * of the HTTP request, leading to an inconsistent result
 */

package it.polito.waii.order_service.controllers;