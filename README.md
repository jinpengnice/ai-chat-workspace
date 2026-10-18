# ai-chat-workspace


### 技术栈
- 前端: Android: Compose ViewModel Retrofit + Okhttp
- 后端： Java + SpringBoot
- 数据库： PostgreSQL
- LLM: DeepSeek


### 开发环境
- Java SDK: 21
- Spring Boot: 4.1.1
- Android Studio: 2026.2.1
- Git:  2.39.5
- Gradle: 未安装
- Meven: 未安装
- Docker: 29.8.2
- Docker compose: v5.6.0
- PostgreSQL: 17.11


### PRD

#### MVP
- 用户注册、登录和退出登录。
- 创建新会话、查看历史会话、删除会话。
- AI 多轮对话与流式输出。
- 消息保存到 PostgreSQL，重启后仍能查询。
- Markdown 消息渲染、复制回答、停止生成。
- 用户之间的数据隔离。
- 后端异常处理、测试、Docker 部署及 GitHub 开源。



### 后续计划

执行原则：每次只完成一个可验证的小任务。 你可以使用 AI 编程工具辅助生成代码，但要亲自运行、阅读、测试并解释代码。这样最终得到的不只是一个开源仓库，而是一套你真正掌握、可以在 Android 面试中深入讲解的工程经验。


阶段 1：搭建后端骨架
学习 Java 必需语法、Maven 或 Gradle、Spring Boot 工程结构、HTTP 和 JSON。
具体任务：
- 创建 GitHub 仓库。
- 使用 IDEA Spring Boot 创建后端。
- 实现 GET /api/health。
- 编写第一个 JUnit 测试。
- 使用 Postman 或 curl 验证接口。
验收：后端可以启动，接口可访问，测试通过。



第 4–6 天

阶段 2：接入 AI 模型
学习 Controller、Service、DTO、配置管理、异常处理。
具体任务：
- 选择一个支持流式输出的中文模型 API。
- 将 API Key 放在环境变量中。
- 创建模型服务接口和实现类。
- 先实现普通聊天，再实现 SSE 流式响应。
- 处理模型超时和 API 错误。
验收：调用后端接口可以得到 AI 回答，并能逐段接收输出。



第 7–9 天

阶段 3：数据库与业务接口
学习 PostgreSQL、SQL、JPA、实体关系、事务和 Flyway。
具体任务：
- 设计用户、会话、消息三张核心表。
- 实现会话创建、列表查询和删除。
- 实现消息历史分页查询。
- 保存用户消息和模型回答。
- 验证会话和消息的所属关系。
验收：应用重启后历史记录仍存在，不同用户无法读取彼此的数据。



第 10–14 天

阶段 4：Android 聊天客户端
具体任务：
- 使用 Jetpack Compose 实现聊天页面。
- 使用 ViewModel + StateFlow 管理消息状态。
- 使用 Repository 封装网络访问。
- 实现 SSE 接收与增量更新。
- 实现会话列表、历史加载、发送和停止生成。
- 处理加载、空状态、错误和网络断开。
验收：可以在模拟器或真机上完成一次完整的 AI 多轮对话。



第 15–18 天

阶段 5：安全与质量
具体任务：
- 实现注册、登录、密码哈希与身份认证。
- 为受保护的 API 增加权限校验。
- 统一异常响应和参数校验。
- 编写 Service 单元测试及关键接口集成测试。
- 验证重复请求、超时、取消生成等边界情况。
验收：没有登录不能访问私有数据，关键业务逻辑有自动化测试。



第 19–21 天

阶段 6：开源与投递准备
具体任务：
- 使用 Docker Compose 部署后端和 PostgreSQL。
- 编写 .env.example 和完整启动文档。
- 添加 GitHub Actions 构建与测试流程。
- 整理 API 文档、架构图和数据库 ER 图。
- 录制演示视频，编写简历项目描述。
- 准备项目设计、数据库和流式响应相关的面试问题。
验收：项目可运行、可演示、可测试，其他开发者能按文档启动。
