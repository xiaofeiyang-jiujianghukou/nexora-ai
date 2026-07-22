# ---- Nexora Backend Dockerfile ----
FROM crpi-27zlqugq2208c0pz.cn-hangzhou.personal.cr.aliyuncs.com/xiaofeiyang930112/maven:3.9-eclipse-temurin-21-alpine AS build
WORKDIR /app

# 配置阿里云 Maven 镜像 + 依赖缓存层
RUN echo '<settings xmlns="http://maven.apache.org/SETTINGS/1.2.0">\
  <mirrors>\
    <mirror>\
      <id>aliyun</id>\
      <mirrorOf>central</mirrorOf>\
      <url>https://maven.aliyun.com/repository/public</url>\
    </mirror>\
  </mirrors>\
</settings>' > /root/.m2/settings.xml

COPY pom.xml .
COPY nexora-common/pom.xml nexora-common/
COPY nexora-api/pom.xml nexora-api/
COPY nexora-app/pom.xml nexora-app/
COPY nexora-module-user/pom.xml nexora-module-user/
COPY nexora-module-news/pom.xml nexora-module-news/
COPY nexora-module-ai/pom.xml nexora-module-ai/
COPY nexora-module-search/pom.xml nexora-module-search/
COPY nexora-module-crawler/pom.xml nexora-module-crawler/
COPY nexora-module-notification/pom.xml nexora-module-notification/
RUN mvn dependency:go-offline -q

COPY . .
RUN mvn package -Dmaven.test.skip=true -pl nexora-app -am -q && \
    cp nexora-app/target/nexora-app-*.jar app.jar

FROM crpi-27zlqugq2208c0pz.cn-hangzhou.personal.cr.aliyuncs.com/xiaofeiyang930112/eclipse-temurin:21-jre-alpine
RUN addgroup -S nexora && adduser -S nexora -G nexora
USER nexora
WORKDIR /app
COPY --from=build /app/app.jar app.jar
EXPOSE 8080
HEALTHCHECK --interval=30s --timeout=3s --retries=3 \
  CMD wget -q -O /dev/null http://localhost:8080/actuator/health || exit 1
ENTRYPOINT ["java", "-jar", "app.jar"]
