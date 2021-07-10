/**
 * This package has the purpose of binding together Kafka beans
 *
 * The subpackage <i>consumer</i> defines the beans to let the KafkaListener-annotated methods to work;
 * the subpackage <i>producer</i> is intended to test KafkaListener-annotated methods ONLY.
 *
 *  Beware that the beans defined in the two subpackages partially
 *  depend on beans of the other package. For instance, a ProducerFactory may be used both to set up
 *  a ReplyingKafkaTemplate and a KafkaTemplate. So separating them will require some additional work.
 */

package it.polito.waii.order_service.kafka;