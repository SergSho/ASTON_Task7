# TO DO before building image!: mvn clean package -DskipTests
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY target/gateway-*.jar gateway.jar
ENTRYPOINT ["java", "-jar", "gateway.jar"]