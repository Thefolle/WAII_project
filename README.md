# WAII project

## Saga

Explanation: https://developers.redhat.com/blog/2018/10/01/patterns-for-distributed-transactions-within-a-microservices-architecture#conclusion

## Kafka general configuration

The four microservices communicate through a Kafka server, where they send and receive messages.

### Installation and setup
A Kafka server is not run through a Gradle dependency; instead it is necessary to download this package: https://kafka.apache.org/downloads

You can run a server instance by opening a shell into the main folder of the zip file and run the command: `.\bin\windows\zookeeper-server-start.bat .\config\zookeeper.properties`

Then, open another shell and issue the command: `.\bin\windows\kafka-server-start.bat .\config\server.properties`

If you are on Linux, omit the _windows_ folder.

### Initial configuration and example

#### Producer

The first two beans configure a producer, whereas KafkaTemplate is used to send a message.

```kotlin
    @Bean
    fun producerFactory(): ProducerFactory<String, String> {
        var config = mapOf(
            Pair(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092"),
            Pair(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer::class.java),
            Pair(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer::class.java)
        )

        return DefaultKafkaProducerFactory(config);
    }

    @Bean
    fun kafkaTemplate(): KafkaTemplate<String, String> {
        return KafkaTemplate(producerFactory())
    }

    @Bean
    fun applicationRunner(template: KafkaTemplate<String?, String?>): ApplicationRunner {
        return ApplicationRunner { template.send("topic1", "test") }
    }
   ```

#### Consumer

Here is the initial configuration

```kotlin
@EnableKafka
@Configuration
class KafkaConfiguration {

    @Bean
    fun consumerFactory(): ConsumerFactory<String, String> {
        var config = mapOf(
            Pair(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092"),
            Pair(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer::class.java),
            Pair(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer::class.java)
        )

        return DefaultKafkaConsumerFactory(config)
    }

    @Bean
    fun concurrentKafkaListenerContainerFactory(): ConcurrentKafkaListenerContainerFactory<String, String> {
        var container = ConcurrentKafkaListenerContainerFactory<String, String>()
        container.consumerFactory = consumerFactory()

        return container
    }

}
```

And this is the callback called when the topic _topic1_ receives a message.

```kotlin
    @KafkaListener(id = "consumer", topics = ["topic1"])
        fun listen(value: String?) {
            println(value);
    }
```

#### Configurer

The project is furnished with a module to properly configure the Kafka server, such as by creating topics. This service acts as a script, since it startups, configures Kafka and shutdowns immediately afterwards. In other words, it only works during the startup of the system.

