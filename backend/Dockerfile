# backend/Dockerfile

FROM eclipse-temurin:26-jdk AS build
WORKDIR /app

COPY backend/mvnw .
COPY backend/.mvn .mvn
COPY backend/pom.xml .

RUN chmod +x mvnw
RUN ./mvnw dependency:go-offline -B

COPY backend/src src
COPY frontend src/main/resources/static

RUN ./mvnw package -DskipTests -B

FROM eclipse-temurin:26-jre AS runtime
WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
