# Order service

How to create different serializers/deserializers:

- Create a single deserializer which distinguishes object types through the topic; only one consumer factory is needed, but it returns Object instances;
- Create multiple deserializers for each possible incoming class; one consumer factory and a kafka consumer container per class is needed.