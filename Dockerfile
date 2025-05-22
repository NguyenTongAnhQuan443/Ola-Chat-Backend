# Dùng image Java 17 nhẹ
FROM eclipse-temurin:17-jre-alpine

# Copy file JAR đã build vào container
COPY target/*.jar OlaChat-Backend-0.0.1-SNAPSHOT.jar

# Mở port 8080
EXPOSE 8080

# Lệnh chạy app
ENTRYPOINT ["java", "-jar", "/OlaChat-Backend-0.0.1-SNAPSHOT.jar"]
