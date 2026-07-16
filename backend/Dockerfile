# ---- Stage 1: Build ----
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY nexora-common nexora-common
COPY nexora-api nexora-api
COPY nexora-app nexora-app
COPY nexora-module-user nexora-module-user
COPY nexora-module-news nexora-module-news
COPY nexora-module-ai nexora-module-ai
COPY nexora-module-search nexora-module-search
COPY nexora-module-crawler nexora-module-crawler
COPY nexora-module-notification nexora-module-notification
RUN mvn clean package -DskipTests -pl nexora-app -am -q

# ---- Stage 2: Run ----
FROM eclipse-temurin:17-jre-alpine
RUN addgroup -S nexora && adduser -S nexora -G nexora
USER nexora
WORKDIR /app

COPY --from=build /app/nexora-app/target/nexora-app-*.jar app.jar

EXPOSE 8080
HEALTHCHECK --interval=30s --timeout=3s --retries=3 \
  CMD wget -q -O /dev/null http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]
