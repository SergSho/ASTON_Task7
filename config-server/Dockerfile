# TO DO before building image!: mvn clean package -DskipTests
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY target/config-server-*.jar config-server.jar
ENTRYPOINT ["java", "-jar", "config-server.jar"]

#FROM maven:3.8.1-openjdk-17 AS builder
#WORKDIR /build
#COPY . .
#RUN mvn clean package -DskipTests
#
#FROM eclipse-temurin:17-jre-alpine
#WORKDIR /app
#COPY --from=builder /target/*.jar eureka-server.jar
#ENTRYPOINT ["java", "-jar", "eureka-server.jar"]