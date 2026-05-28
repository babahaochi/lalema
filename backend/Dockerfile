FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY target/lalema-backend-1.0.0.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "-Xms256m", "-Xmx512m", "app.jar"]
