# ---- Stage 1: Build ----
FROM crpi-27zlqugq2208c0pz.cn-hangzhou.personal.cr.aliyuncs.com/xiaofeiyang930112/maven:3.9-eclipse-temurin-21-alpine AS build
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
RUN mvn clean package -Dmaven.test.skip=true -pl nexora-app -am -q && \
    cd /app/nexora-app/target && \
    cp nexora-app-*.jar app.jar

# ---- Stage 2: Run ----
FROM crpi-27zlqugq2208c0pz.cn-hangzhou.personal.cr.aliyuncs.com/xiaofeiyang930112/eclipse-temurin:21-jre-alpine
RUN addgroup -S nexora && adduser -S nexora -G nexora
USER nexora
WORKDIR /app

COPY --from=build /app/nexora-app/target/app.jar app.jar

EXPOSE 8080
HEALTHCHECK --interval=30s --timeout=3s --retries=3 \
  CMD wget -q -O /dev/null http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]
