# for application build stage
FROM maven:3.10.0-eclipse-temurin-17 AS build

WORKDIR /backend
COPY pom.xml .
RUN mvn dependency:go-offline
COPY src ./src
RUN mvn clean package -DskipTests

#for production build stage
FROM eclipse-temurin:17-jre
WORKDIR /backend
COPY --from=build /backend/target/*.jar ./app.jar
EXPOSE 8000
CMD ["java", "-jar", "app.jar"]