FROM maven:3.9.9-eclipse-temurin-21-alpine AS builder
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn dependency:go-offline -DskipTests
RUN mvn clean package -DskipTests
FROM eclipse-temurin:21-jre-alpine
ENV TZ=Asia/Shanghai
WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar
CMD ["java", "-Dfile.encoding=UTF-8", "-Duser.timezone=Asia/Shanghai", "-Xms256m", "-Xmx512m", "-jar", "app.jar"]