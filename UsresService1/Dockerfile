FROM eclipse-temurin:17-jdk
EXPOSE 8765
ADD target/UsersService-1.0.0.jar /app/UsersService.jar
ENTRYPOINT [ "java", "-jar", "/app/UsersService.jar" ]