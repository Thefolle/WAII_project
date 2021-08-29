FROM adoptopenjdk/openjdk11:alpine-jre
ARG JAR_FILE=build/libs/*.jar
COPY ${JAR_FILE} app.jar

RUN addgroup -S app && adduser -S app -G app
USER app

ENTRYPOINT ["java", "-jar", "./app.jar"]


