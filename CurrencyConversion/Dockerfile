FROM eclipse-temurin:17-jdk
EXPOSE 8765
ADD target/currency-conversion-1.0.0.jar /app/currency-conversion.jar
ENTRYPOINT [ "java", "-jar", "/app/currency-conversion.jar" ]
