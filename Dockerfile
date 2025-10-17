# 前端构建
FROM node:18-alpine AS frontend-builder

WORKDIR /frontend

COPY free-novel-web/package*.json ./
RUN npm install --registry=https://registry.npmmirror.com

COPY free-novel-web/ ./

RUN npm run build

# 服务端构建
FROM maven:3.9-eclipse-temurin-17 AS backend-builder

WORKDIR /app

COPY novel/pom.xml ./
RUN mvn dependency:go-offline -B

COPY novel/src ./src

RUN mkdir -p src/main/resources/static
COPY --from=frontend-builder /frontend/dist/ ./src/main/resources/static/

RUN mvn clean package -DskipTests -Pdev

# 运行环境构建
FROM eclipse-temurin:17-jdk

WORKDIR /app

RUN groupadd -r appuser && useradd -r -g appuser appuser

COPY --from=backend-builder /app/target/*.jar app.jar

RUN chown -R appuser:appuser /app

USER appuser

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

ENV TZ=Asia/Shanghai

ENV JAVA_OPTS="-Xms512m -Xmx1024m \
    -XX:+UseG1GC \
    -XX:MaxGCPauseMillis=200 \
    -Djava.security.egd=file:/dev/./urandom \
    -Dspring.profiles.active=dev"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
