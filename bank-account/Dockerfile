FROM eclipse-temurin:17-jdk
EXPOSE 8200
ADD target/bank-account-service-1.0.0.jar /app/bank-account.jar
ENTRYPOINT [ "java", "-jar", "/app/bank-account.jar" ]

