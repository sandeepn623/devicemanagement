FROM eclipse-temurin:25-jdk-alpine
WORKDIR /app
COPY target/deviceManagement-1.0-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
