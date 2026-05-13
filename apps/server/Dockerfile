# Stage 1: Build
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN apk add --no-cache maven && \
    mvn dependency:go-offline -q && \
    mvn package -DskipTests -q

# Stage 2: Runtime
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
RUN addgroup -S taskhive && adduser -S taskhive -G taskhive
USER taskhive
COPY --from=builder /app/target/*.jar app.jar
EXPOSE 8080
ENV JAVA_OPTS="-XX:+UseSerialGC -Xss512k -XX:MaxRAMPercentage=75"
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar app.jar"]
