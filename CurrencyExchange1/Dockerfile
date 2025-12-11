FROM eclipse-temurin:17-jdk
EXPOSE 8765
ADD target/currency-exchange-1.0.0.jar /app/currency-exchange.jar
ENTRYPOINT [ "java", "-jar", "/app/currency-exchange.jar" ]
