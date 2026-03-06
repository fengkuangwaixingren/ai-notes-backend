# 第一阶段：构建阶段
FROM maven:3.8.4-openjdk-17-slim AS build
WORKDIR /app

# 复制 pom.xml 并下载依赖（利用 Docker 缓存）
COPY pom.xml .
RUN mvn dependency:go-offline

# 复制源代码并打包
COPY src ./src
RUN mvn clean package -DskipTests

# 第二阶段：运行阶段
FROM openjdk:21-jdk-slim
WORKDIR /app

# 从构建阶段复制生成的 jar 文件
COPY --from=build /app/target/*.jar app.jar

# 暴露 8080 端口
EXPOSE 8080

# 启动命令
ENTRYPOINT ["java", "-jar", "app.jar"]