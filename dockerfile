FROM openjdk:17-jdk-slim
ARG JAR_FILE=target/fitness-service-0.0.1.jar
COPY ${JAR_FILE} fitness-service-0.0.1.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "fitness-service-0.0.1.jar"]
