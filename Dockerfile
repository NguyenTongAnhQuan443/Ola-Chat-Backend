# Dùng image Java 17 nhẹ
FROM eclipse-temurin:17-jre-alpine

# Tạo thư mục làm việc (tùy chọn, giúp tổ chức file gọn hơn)
WORKDIR /app

# Copy file JAR đã build vào container
COPY target/*.jar OlaChat-Backend-0.0.1-SNAPSHOT.jar

# Mở port 8080
EXPOSE 8080

# Lệnh chạy app
ENTRYPOINT ["java", "-jar", "/app/OlaChat-Backend-0.0.1-SNAPSHOT.jar"]
