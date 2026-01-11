# TO DO before building image!: mvn clean package -DskipTests
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY target/eureka-server-*.jar eureka-server.jar
ENTRYPOINT ["java", "-jar", "eureka-server.jar"]
