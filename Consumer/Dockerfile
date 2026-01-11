# TO DO before building image!: mvn clean package -DskipTests
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY target/consumer-*.jar consumer.jar
ENTRYPOINT ["java", "-jar", "consumer.jar"]