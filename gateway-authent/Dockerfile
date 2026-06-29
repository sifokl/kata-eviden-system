FROM eclipse-temurin:21-jre

WORKDIR /app

COPY gateway-launcher/target/gateway-launcher-1.0.0-SNAPSHOT.jar app.jar

EXPOSE 8081
EXPOSE 5005

ENTRYPOINT ["java", "-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005", "-jar", "app.jar"]