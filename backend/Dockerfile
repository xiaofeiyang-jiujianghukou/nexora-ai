# ---- Nexora Backend Dockerfile ----
FROM crpi-27zlqugq2208c0pz.cn-hangzhou.personal.cr.aliyuncs.com/xiaofeiyang930112/maven:3.9-eclipse-temurin-21-alpine AS build
WORKDIR /app

# 阿里云 Maven 镜像加速
RUN mkdir -p /root/.m2 && echo '<settings xmlns="http://maven.apache.org/SETTINGS/1.2.0"><mirrors><mirror><id>aliyun</id><mirrorOf>central</mirrorOf><url>https://maven.aliyun.com/repository/public</url></mirror></mirrors></settings>' > /root/.m2/settings.xml

# 一次性编译（依赖自动下载）
COPY . .
RUN mvn package -Dmaven.test.skip=true -pl nexora-app -am && \
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
