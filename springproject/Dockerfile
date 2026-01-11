# TO DO before building image!: mvn clean package -DskipTests
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY target/springproject-*.jar producer.jar
ENTRYPOINT ["java", "-jar", "producer.jar"]