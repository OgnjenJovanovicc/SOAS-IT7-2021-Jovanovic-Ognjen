FROM eclipse-temurin:17-jdk
EXPOSE 8765
ADD target/naming-server-1.0.0.jar /app/naming-server.jar
ENTRYPOINT [ "java", "-jar", "/app/naming-server.jar" ]
