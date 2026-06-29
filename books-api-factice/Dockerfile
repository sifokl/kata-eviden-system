FROM eclipse-temurin:21-jre

WORKDIR /app

COPY target/books-api-factice-1.0.0-SNAPSHOT.jar app.jar

EXPOSE 8082

ENTRYPOINT ["java", "-jar", "app.jar"]