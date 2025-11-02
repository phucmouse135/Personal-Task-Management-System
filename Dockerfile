##### Stage 1: Build #####
FROM maven:3.8.3-openjdk-17 AS builder
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn clean package -Dmaven.test.skip=true

##### Stage 2: Runtime #####
FROM eclipse-temurin:17-jre-ubi9-minimal AS runtime


# Copy file jar từ stage build sang stage runtime
COPY --from=builder /app/target/*.jar /run/app.jar

# Cấu hình cổng chạy
EXPOSE 8080

# Cấu hình Java options (sử dụng ENV để dễ override khi run container)
ENV JAVA_OPTS="-Xmx2048m -Xms256m"

# Entry point
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /run/app.jar"]

