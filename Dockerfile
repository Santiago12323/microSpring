# Imagen base con Maven y Java 17
FROM maven:3.8.6-eclipse-temurin-17 AS build

WORKDIR /app

COPY . .

RUN mvn clean package -DskipTests

FROM eclipse-temurin:17-jdk

WORKDIR /app

COPY --from=build /app/target/*.jar micro-spring-boot-1.0-SNAPSHOT.jar

EXPOSE 8080

CMD ["java", "-jar", "micro-spring-boot-1.0-SNAPSHOT.jar"]
