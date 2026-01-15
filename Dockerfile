FROM eclipse-temurin:25-jdk-alpine
ARG JAR_FILE=target/*-SNAPSHOT.jar
WORKDIR /app
COPY ${JAR_FILE} app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
