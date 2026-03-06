# AI Notes Assistant Backend

一个基于 **Spring Boot 3.2.4 + Java 21** 的 AI 笔记助手后端服务，提供 **JWT 用户认证**、**笔记增删改查**、**Redis 缓存优化**，并集成 **通义千问（DashScope / Spring AI Alibaba）**实现**异步 AI 摘要**能力。

> 占位符：请将 README 中的 `[ ... ]` 按你的实际信息补全。

---

## 技术栈

- **基础框架**：Spring Boot 3.2.4、Java 21
- **安全认证**：Spring Security、JWT（JJWT）
- **数据访问**：Spring Data JPA、MySQL 8
- **缓存**：Spring Cache、Redis
- **AI 能力**：Spring AI Alibaba（DashScope / Qwen）
- **接口文档**：SpringDoc OpenAPI（Swagger UI）
- **构建与部署**：Maven、Docker、Docker Compose

---

## 核心功能

- 🔐 **用户认证（JWT）**：注册 / 登录 / Bearer Token 访问受保护接口  
- 📝 **笔记 CRUD**：创建、查询列表、查询详情、更新、删除（按当前登录用户隔离）  
- 🏷️ **标签系统（多对多）**：[待补全：标签表结构 / 关联接口]  
- ⚡ **Redis 缓存优化**：对查询接口使用缓存，写操作自动清理缓存  
- 🤖 **异步 AI 摘要**：笔记创建/更新后异步生成摘要并回写到 `summary` 字段（通义千问）  

---

## 快速开始

### 1) 克隆与构建

```bash
git clone [你的仓库地址]
cd [你的项目目录]
mvn -DskipTests package
```

### 2) 配置环境变量

#### MySQL / Redis（示例）

- `DB_USERNAME`
- `DB_PASSWORD`
- `REDIS_HOST`
- `REDIS_PORT`
- `REDIS_PASSWORD`

#### DashScope（通义千问）

在 **Windows PowerShell** 中设置：

```powershell
$env:AI_DASHSCOPE_API_KEY="sk-你的真实key"
```

对应配置位于 `src/main/resources/application.yml`：

```yaml
spring:
  ai:
    dashscope:
      api-key: ${AI_DASHSCOPE_API_KEY:}
      chat:
        options:
          model: qwen-plus
          temperature: 0.7
```

> 提示：请勿把真实 Key 写入仓库；推荐使用环境变量或密钥管理系统。

### 3) 启动服务

```bash
mvn spring-boot:run
```

服务默认监听：`http://localhost:8080`

---

## API 文档（Swagger）

启动后访问：

- Swagger UI：`http://localhost:8080/swagger-ui.html`

---

## Docker 部署

> 如果你已经准备好了 `Dockerfile` / `docker-compose.yml`，可以直接按以下命令启动；否则请先创建对应文件（或把你的现有文件贴给我，我可以帮你补齐）。

### 方式 A：Docker 构建并运行

```bash
docker build -t ai-notes-backend:latest .
docker run --rm -p 8080:8080 \
  -e DB_USERNAME=[...] \
  -e DB_PASSWORD=[...] \
  -e REDIS_HOST=[...] \
  -e REDIS_PORT=[...] \
  -e REDIS_PASSWORD=[...] \
  -e AI_DASHSCOPE_API_KEY=[...] \
  ai-notes-backend:latest
```

### 方式 B：Docker Compose（推荐）

```bash
docker compose up -d
```

建议在 `docker-compose.yml` 中配置：

- MySQL 服务（端口/初始化库名/账号密码）
- Redis 服务（端口/密码）
- 本服务（环境变量注入 DB/Redis/AI Key）

---

## 常见问题（FAQ）

### 1) 启动时报 “No setter found for property: max-tokens”

DashScope 的 `DashScopeChatOptions` 在当前版本里不支持通过配置文件绑定 `max-tokens`。请不要在 `application.yml` 中配置该项（通过提示词控制摘要长度即可）。

---

## License

MIT License

