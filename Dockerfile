# syntax=docker/dockerfile:1

# --- Build : compile et package le jar (tests lancés par la CI, pas ici)
FROM eclipse-temurin:21-jdk AS build
WORKDIR /app
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN chmod +x mvnw && ./mvnw -q -B dependency:go-offline
COPY src/ src/
RUN ./mvnw -q -B package -DskipTests && cp target/*.jar app.jar

# --- Runtime : JRE seul, utilisateur non root
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
RUN addgroup -S events && adduser -S events -G events
COPY --from=build /app/app.jar app.jar
USER events

EXPOSE 8080
ENV SPRING_PROFILES_ACTIVE=prod \
    JAVA_OPTS="-XX:MaxRAMPercentage=75 -XX:+ExitOnOutOfMemoryError"

HEALTHCHECK --interval=30s --timeout=5s --start-period=60s --retries=3 \
  CMD wget -qO- http://localhost:8080/actuator/health/liveness || exit 1

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/app.jar"]
