# ShortURLPro 后端技术栈详解

## 📋 技术栈概览

| 类别 | 技术组件 | 版本 | 主要作用 |
|------|----------|------|----------|
| 核心框架 | Spring Boot | 3.3.5 | 应用程序核心框架 |
| 编程语言 | Java | 21 | 主要开发语言 |
| 构建工具 | Maven | 4.0.0 | 项目构建和依赖管理 |
| ORM 框架 | MyBatis-Plus | 3.5.5 | 数据持久化增强 |
| 前端框架 | Vue.js | 3.5.12 | 前端 UI 框架 |

---

## 🏗️ 核心框架层

### Spring Boot 3.3.5
**配置文件**: `pom.xml` (第5-9行)
```xml
<parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>3.3.5</version>
</project>
```

**关键启动类**: `src/main/java/com/example/shorturlpro/ShortUrlProApplication.java`
- 应用程序入口点
- 启用Spring Boot自动配置
- 扫描组件包

**作用**:
- 提供完整的Web应用基础设施
- 自动配置常见组件
- 内嵌Tomcat服务器
- 简化开发配置

---

## 🗄️ 数据访问层

### Spring Data JPA + Hibernate
**配置文件**: `pom.xml` (第 46-50 行)
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>
```

### MyBatis-Plus 3.5.5
**配置文件**: `pom.xml` (第 173-178 行)
```xml
<dependency>
    <groupId>com.baomidou</groupId>
    <artifactId>mybatis-plus-boot-starter</artifactId>
    <version>3.5.5</version>
</dependency>
```

**实体类文件**:
- `src/main/java/com/example/shorturlpro/entity/User.java` - 用户实体
- `src/main/java/com/example/shorturlpro/entity/ShortUrl.java` - 短链接实体
- `src/main/java/com/example/shorturlpro/entity/ShortUrlStatus.java` - 枚举状态

**仓库接口**:
- `src/main/java/com/example/shorturlpro/repository/UserRepository.java`
- `src/main/java/com/example/shorturlpro/repository/ShortUrlRepository.java`

**作用**:
- 对象关系映射 (ORM)
- 自动生成 CRUD 操作
- 连接池管理
- 事务管理
- 灵活的 SQL 查询支持
- 代码生成器支持

### MySQL 数据库
**配置文件**: `pom.xml` (第52-57行)
```xml
<dependency>
    <groupId>com.mysql</groupId>
    <artifactId>mysql-connector-j</artifactId>
    <scope>runtime</scope>
</dependency>
```

**数据库配置**: `src/main/resources/application-dev.yml`
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/shorturl_db
    username: root
    password: root
    driver-class-name: com.mysql.cj.jdbc.Driver
```

**作用**:
- 持久化数据存储
- 关系型数据库管理
- ACID事务保证

### 数据库连接池 HikariCP
**配置文件**: `pom.xml` (第84-88行)
```xml
<dependency>
    <groupId>com.zaxxer</groupId>
    <artifactId>HikariCP</artifactId>
</dependency>
```

**作用**:
- 高性能连接池
- 连接复用优化
- 自动连接健康检查

---

## ⚡ 异步处理系统

### 异步任务线程池
**配置类**: `src/main/java/com/example/shorturlpro/config/AsyncConfig.java`

**通用线程池** (`taskExecutor`):
```java
@Bean(name = "taskExecutor")
public Executor taskExecutor() {
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    executor.setCorePoolSize(Runtime.getRuntime().availableProcessors());
    executor.setMaxPoolSize(Runtime.getRuntime().availableProcessors() * 2);
    executor.setQueueCapacity(100);
    executor.setThreadNamePrefix("async-executor-");
    executor.setKeepAliveSeconds(60);
    executor.setWaitForTasksToCompleteOnShutdown(true);
    executor.setAwaitTerminationSeconds(60);
    executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
    return executor;
}
```

**点击统计专用线程池** (`clickStatsExecutor`):
- 核心线程数：2
- 最大线程数：5
- 队列容量：500
- 拒绝策略：DiscardPolicy (丢弃任务)

**作用**:
- 异步任务处理
- 并发性能优化
- 内存占用控制
- 避免 OOM 风险
- 主流程保护

---

## 📝 日志拦截系统

### 请求日志拦截器
**拦截器类**: `src/main/java/com/example/shorturlpro/interceptor/RequestLoggingInterceptor.java`

**实现接口**: `HandlerInterceptor`

**核心方法**:
- `preHandle()`: 记录请求开始时间，初始化请求上下文
- `afterCompletion()`: 计算请求耗时，记录访问日志

**日志格式**:
```
{HTTP_METHOD} {URI} -> {STATUS_CODE} (耗时：{DURATION}ms)
```

**作用**:
- HTTP 请求访问日志记录
- 请求耗时统计
- 响应状态码追踪
- 异常请求记录
- 请求链路追踪

---

## 🔐 安全认证层

### Spring Security 6.x
**配置文件**: `pom.xml` (第59-63行)
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>
```

**安全配置文件**: `src/main/java/com/example/shorturlpro/config/SecurityConfig.java`
- 配置认证授权规则
- 定义安全过滤链
- 设置CSRF保护

**JWT过滤器**: `src/main/java/com/example/shorturlpro/filter/JwtAuthenticationFilter.java`
- 拦截请求验证JWT
- 解析用户身份信息
- 设置安全上下文

**作用**:
- 身份认证和授权
- 请求安全拦截
- CSRF防护
- Session管理

### JWT (JSON Web Token)
**配置文件**: `pom.xml` (第173-194行)
```xml
<!-- JJWT核心依赖 -->
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-api</artifactId>
    <version>0.11.5</version>
</dependency>
```

**JWT 工具类**: `src/main/java/com/example/shorturlpro/util/JwtUtil.java`
- JWT生成和验证
- Token过期时间管理
- 用户信息编码解码

**JJWT 依赖组成**:
- `jjwt-api` (0.11.5): API 接口层
- `jjwt-impl` (0.11.5): 运行时实现层
- `jjwt-jackson` (0.11.5): JSON 序列化支持

**作用**:
- 无状态身份验证
- Token安全传输
- 用户会话管理

---

## 🚀 缓存系统

### 多级缓存架构

#### 一级缓存 - Caffeine (本地缓存)
**配置文件**: `pom.xml` (第 108-112 行)
```xml
<dependency>
    <groupId>com.github.ben-manes.caffeine</groupId>
    <artifactId>caffeine</artifactId>
</dependency>
```

**配置类**: `src/main/java/com/example/shorturlpro/config/MultiLevelCacheConfig.java`
- 配置本地缓存策略
- 设置缓存大小和过期时间
- 定义缓存键值对

#### 二级缓存 - Redis (分布式缓存)
**配置文件**: `pom.xml` (第 96-100 行)
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-redis</artifactId>
</dependency>
```

**Redis 配置**: `src/main/java/com/example/shorturlpro/config/RedisConfig.java`
- Redis连接配置
- 序列化器设置
- 连接池参数

**缓存管理服务**: `src/main/java/com/example/shorturlpro/service/CacheManagementService.java`

**缓存类型**:
- `shortUrl`: 短链接缓存
- `shortUrlDetail`: 短链接详情缓存
- `statistics`: 统计数据缓存

**核心功能**:
- 缓存预热机制 (`@PostConstruct`)
- 缓存监控和统计
- 缓存失效策略
- 异步缓存操作 (`CompletableFuture`)

**作用**:
- 减少数据库访问压力
- 提高响应速度
- 支持高并发访问
- 分布式缓存一致性

---

## 📊 监控运维层

### Spring Boot Actuator
**配置文件**: `pom.xml` (第 114-118 行)
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
```

**监控配置**: `src/main/resources/application.yml` (第 58-86 行)
```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus,logfile,caches
  endpoint:
    health:
      show-details: when-authorized
  metrics:
    export:
      prometheus:
        enabled: true
```

**暴露的端点**:
- `/actuator/health`: 应用健康检查
- `/actuator/info`: 应用信息
- `/actuator/metrics`: 性能指标
- `/actuator/prometheus`: Prometheus 格式指标
- `/actuator/logfile`: 日志文件访问
- `/actuator/caches`: 缓存管理

### Micrometer + Prometheus
**配置文件**: `pom.xml` (第 120-128 行)
```xml
<dependency>
    <groupId>io.micrometer</groupId>
    <artifactId>micrometer-core</artifactId>
</dependency>
<dependency>
    <groupId>io.micrometer</groupId>
    <artifactId>micrometer-registry-prometheus</artifactId>
</dependency>
```

**监控配置类**: `src/main/java/com/example/shorturlpro/config/MetricsConfig.java`

**自定义指标**:
- `SHORT_URL_GENERATE_COUNT`: 短链接生成计数器
- `SHORT_URL_REDIRECT_COUNT`: 短链接跳转计数器
- `SHORT_URL_REDIRECT_TIME`: 跳转耗时计时器
- `CACHE_HIT_RATE`: 缓存命中率仪表
- `DATABASE_QUERY_TIME`: 数据库查询耗时

**作用**:
- 应用性能监控
- 指标数据收集
- Prometheus 格式输出
- 可视化监控集成
- 自定义业务指标

---

## 📝 日志系统

### Logback 日志框架
**配置文件**: `pom.xml` (第 153-157 行)
```xml
<dependency>
    <groupId>ch.qos.logback</groupId>
    <artifactId>logback-classic</artifactId>
</dependency>
```

**日志配置**: `src/main/resources/logback-spring.xml`

**控制台日志格式**:
```
%d{yyyy-MM-dd HH:mm:ss} %-5level %logger{36} [%X{requestId}] - %msg%n
```

**特性**:
- 结构化日志输出
- 请求 ID 追踪 (`%X{requestId}`)
- 多环境日志配置
- 异步日志支持
- 日志级别动态调整

**作用**:
- 应用日志记录
- 调试信息输出
- 审计日志追踪
- 性能分析支持

---

## 🖥️ 前端技术栈

### Vue.js 3.5.12
**配置文件**: `vue/package.json`
```json
{
  "name": "shorturl-pro-frontend",
  "version": "0.0.0",
  "type": "module"
}
```

### 核心依赖
- **Vue Router 4.4.5**: 前端路由管理
- **Pinia 2.2.6**: 状态管理
- **Axios 1.7.7**: HTTP 请求库
- **VueUse 11.2.0**: Composition API 工具集

### 构建工具链
- **Vite 5.4.10**: 快速构建工具
- **TypeScript 5.6.0**: 类型系统
- **Vue TSC 2.1.10**: TypeScript 编译

### UI 样式
- **Tailwind CSS 3.4.14**: 原子化 CSS 框架
- **@tailwindcss/forms 0.5.9**: 表单插件
- **@tailwindcss/typography 0.5.15**: 排版插件
- **PostCSS 8.4.47**: CSS 预处理
- **Autoprefixer 10.4.20**: CSS 前缀自动补全

### 代码质量
- **ESLint 9.13.0**: 代码检查
- **Prettier 3.3.3**: 代码格式化
- **eslint-plugin-vue 9.29.0**: Vue ESLint 插件
- **@typescript-eslint 8.11.0**: TypeScript ESLint

### 测试工具
- **Vitest 2.1.4**: 单元测试框架
- **@vitest/coverage-v8 2.1.4**: 测试覆盖率

### 前端目录结构
```
vue/src/
├── api/           # API 接口层
│   ├── auth.ts    # 认证 API
│   ├── request.ts # 请求封装
│   └── shorturl.ts# 短链接 API
├── components/    # 组件目录
├── router/        # 路由配置
│   └── index.ts
├── stores/        # 状态管理
│   └── auth.ts    # 认证状态
├── styles/        # 样式文件
│   └── tailwind.css
├── types/         # TypeScript 类型定义
├── utils/         # 工具函数
│   └── helpers.ts
├── views/         # 页面视图
│   ├── AdminView.vue    # 管理员页面
│   ├── HomeView.vue     # 首页
│   └── LoginView.vue    # 登录页
├── App.vue        # 根组件
└── main.ts        # 入口文件
```

---

## 🖥️ 前端技术栈

### Vue.js 3.5.12
**配置文件**: `vue/package.json`
```json
{
  "name": "shorturl-pro-frontend",
  "version": "0.0.0",
  "type": "module"
}
```

### 核心依赖
- **Vue Router 4.4.5**: 前端路由管理
- **Pinia 2.2.6**: 状态管理
- **Axios 1.7.7**: HTTP 请求库
- **VueUse 11.2.0**: Composition API 工具集

### 构建工具链
- **Vite 5.4.10**: 快速构建工具
- **TypeScript 5.6.0**: 类型系统
- **Vue TSC 2.1.10**: TypeScript 编译

### UI 样式
- **Tailwind CSS 3.4.14**: 原子化 CSS 框架
- **@tailwindcss/forms 0.5.9**: 表单插件
- **@tailwindcss/typography 0.5.15**: 排版插件
- **PostCSS 8.4.47**: CSS 预处理
- **Autoprefixer 10.4.20**: CSS 前缀自动补全

### 代码质量
- **ESLint 9.13.0**: 代码检查
- **Prettier 3.3.3**: 代码格式化
- **eslint-plugin-vue 9.29.0**: Vue ESLint 插件
- **@typescript-eslint 8.11.0**: TypeScript ESLint

### 测试工具
- **Vitest 2.1.4**: 单元测试框架
- **@vitest/coverage-v8 2.1.4**: 测试覆盖率

### 前端目录结构
```
vue/src/
├── api/           # API 接口层
│   ├── auth.ts    # 认证 API
│   ├── request.ts # 请求封装
│   └── shorturl.ts# 短链接 API
├── components/    # 组件目录
├── router/        # 路由配置
│   └── index.ts
├── stores/        # 状态管理
│   └── auth.ts    # 认证状态
├── styles/        # 样式文件
│   └── tailwind.css
├── types/         # TypeScript 类型定义
├── utils/         # 工具函数
│   └── helpers.ts
├── views/         # 页面视图
│   ├── AdminView.vue    # 管理员页面
│   ├── HomeView.vue     # 首页
│   └── LoginView.vue    # 登录页
├── App.vue        # 根组件
└── main.ts        # 入口文件
```

---

## 🛠️ 开发工具

### Lombok
**配置文件**: `pom.xml` (第 138-143 行)
```xml
<dependency>
    <groupId>org.projectlombok</groupId>
    <artifactId>lombok</artifactId>
    <version>1.18.30</version>
    <optional>true</optional>
</dependency>
```

**编译器配置**: `pom.xml` (第 233-239 行)
```xml
<annotationProcessorPaths>
    <path>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
        <version>1.18.30</version>
    </path>
</annotationProcessorPaths>
```

**常用注解**:
- `@Data`: 自动生成getter/setter/toString/equals/hashCode
- `@NoArgsConstructor`: 无参构造函数
- `@AllArgsConstructor`: 全参构造函数
- `@Slf4j`: 自动注入日志对象
- `@Component`: Spring 组件标识

**使用示例** (`User.java`):
```java
@Entity
@Data  // 自动生成getter/setter/toString等方法
@NoArgsConstructor  // 无参构造函数
@AllArgsConstructor // 全参构造函数
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true)
    private String username;
}
```

**作用**:
- 减少样板代码
- 自动生成常用方法
- 提高开发效率
- 保持代码简洁

### 热部署 DevTools
**配置文件**: `pom.xml` (第130-136行)
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-devtools</artifactId>
    <scope>runtime</scope>
    <optional>true</optional>
</dependency>
```

**作用**:
- 开发时自动重启
- 类路径变更检测
- 模板文件热加载
- 提升开发体验

---

## 📈 业务功能组件

### Base62编码
**配置文件**: `pom.xml` (第71-76行)
```xml
<dependency>
    <groupId>commons-codec</groupId>
    <artifactId>commons-codec</artifactId>
    <version>1.16.1</version>
</dependency>
```

**使用位置**: `ShortUrlService.java`
```java
// 短链接生成算法中使用Base62编码
String shortCode = Base62.encode(id);
```

**作用**:
- 短链接字符编码
- URL安全字符集
- 紧凑的字符串表示

### Excel处理
**配置文件**: `pom.xml` (第196-206行)
```xml
<dependency>
    <groupId>org.apache.poi</groupId>
    <artifactId>poi</artifactId>
    <version>5.2.5</version>
</dependency>
<dependency>
    <groupId>org.apache.poi</groupId>
    <artifactId>poi-ooxml</artifactId>
    <version>5.2.5</version>
</dependency>
```

**使用位置**: 管理员导出功能
```java
// 导出短链接统计数据到Excel
Workbook workbook = new XSSFWorkbook();
Sheet sheet = workbook.createSheet("短链接统计");
```

**作用**:
- 数据导出功能
- 报表生成
- Excel文件读写

### API文档
**配置文件**: `pom.xml` (第166-171行)
```xml
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    <version>2.2.0</version>
</dependency>
```

**访问地址**: `http://localhost:8080/swagger-ui.html`

**作用**:
- 自动生成API文档
- 在线接口测试
- 接口规范说明
- 开发者友好

---

## ⚙️ 配置管理体系

### 多环境配置
**主配置文件**: `src/main/resources/application.yml`
- 定义通用配置项
- 设置默认环境为dev

**开发环境**: `src/main/resources/application-dev.yml`
- 开发数据库配置
- 调试模式开启
- 详细日志输出

**生产环境**: `src/main/resources/application-prod.yml`
- 生产数据库配置
- 性能优化设置
- 安全日志级别

### 自定义配置
**应用配置**: `application.yml` (第28-33行)
```yaml
app:
  short-url:
    base-url: ${SHORT_URL_BASE_URL:http://localhost:8080}
    code-length: 6
    default-expire-days: 30
```

**作用**:
- 环境隔离配置
- 外部化配置管理
- 运行时动态调整
- 配置中心集成准备

---

## 🔄 项目构建

### Maven构建配置
**核心插件**: `pom.xml` (第228-239行)
```xml
<plugin>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-maven-plugin</artifactId>
</plugin>
```

**编译插件**: `pom.xml` (第211-226行)
```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-compiler-plugin</artifactId>
    <version>3.11.0</version>
    <configuration>
        <source>21</source>
        <target>21</target>
    </configuration>
</plugin>
```

**测试插件**: `pom.xml` (第241-251行)
```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-surefire-plugin</artifactId>
    <version>3.1.2</version>
</plugin>
```

**作用**:
- 项目编译打包
- 依赖管理
- 测试执行
- 部署包生成

### JSR-250 注解支持
**配置文件**: `pom.xml` (第 146-151 行)
```xml
<dependency>
    <groupId>javax.annotation</groupId>
    <artifactId>javax.annotation-api</artifactId>
    <version>1.3.2</version>
</dependency>
```

**常用注解**:
- `@PostConstruct`: 初始化后执行
- `@PreDestroy`: 销毁前执行
- `@Resource`: 资源注入
- `@Nullable`: 可空标注

**作用**:
- Java 标准注解支持
- 生命周期回调
- 依赖注入标准化

---

## 📁 项目结构说明

```
src/main/java/com/example/shorturlpro/
├── config/           # 配置类目录
│   ├── MetricsConfig.java        # 监控指标配置
│   ├── MultiLevelCacheConfig.java # 多级缓存配置
│   ├── RedisConfig.java          # Redis配置
│   ├── SecurityConfig.java       # 安全配置
│   └── WebConfig.java           # Web相关配置
├── controller/       # 控制器层
│   ├── AdminController.java     # 管理员接口
│   ├── AuthController.java      # 认证接口
│   ├── RedirectController.java  # 跳转接口
│   └── ShortUrlController.java  # 短链接接口
├── dto/             # 数据传输对象
│   └── 各种请求/响应DTO
├── entity/          # 实体类
│   ├── ShortUrl.java
│   ├── ShortUrlStatus.java
│   └── User.java
├── exception/       # 异常处理
│   └── GlobalExceptionHandler.java
├── filter/          # 过滤器
│   └── JwtAuthenticationFilter.java
├── repository/      # 数据访问层
│   ├── ShortUrlRepository.java
│   └── UserRepository.java
├── service/         # 业务逻辑层
│   ├── CacheManagementService.java
│   ├── ShortUrlService.java
│   └── UserDetailsServiceImpl.java
├── util/            # 工具类
│   └── JwtUtil.java
└── ShortUrlProApplication.java  # 启动类
```

---

## 💾 JSR-250 注解支持

**配置文件**: `pom.xml` (第 146-151 行)
```xml
<dependency>
    <groupId>javax.annotation</groupId>
    <artifactId>javax.annotation-api</artifactId>
    <version>1.3.2</version>
</dependency>
```

**常用注解**:
- `@PostConstruct`: 初始化后执行 (用于缓存预热等)
- `@PreDestroy`: 销毁前执行
- `@Resource`: 资源注入
- `@Nullable`: 可空标注

**作用**:
- Java 标准注解支持
- 生命周期回调管理
- 依赖注入标准化

---

## 🔧 关键特性总结

### 高性能特点
- **多级缓存**: Caffeine+Redis 二级缓存架构
- **异步处理**: 双线程池异步任务处理
- **连接池**: HikariCP 高性能数据库连接池
- **压缩优化**: Gzip 响应压缩
- **ORM 增强**: MyBatis-Plus + JPA 双 ORM 支持

### 安全特性
- **JWT 认证**: 无状态 Token 认证
- **Spring Security**: 完整安全框架
- **参数校验**: 请求参数自动验证
- **CSRF 防护**: 跨站请求伪造保护
- **Token 刷新**: JWT Token 自动刷新机制

### 可维护性
- **分层架构**: 清晰的 MVC 分层
- **Lombok**: 减少样板代码
- **统一异常处理**: 全局异常捕获
- **详细日志**: 结构化日志记录
- **请求拦截**: 全链路请求日志追踪
- **JSR-250**: 标准化生命周期管理

### 可观测性
- **Actuator**: 应用监控端点 (health/metrics/caches 等)
- **Micrometer**: 指标收集框架
- **Prometheus**: 监控系统集成
- **健康检查**: 自动化健康检测
- **自定义指标**: 业务指标埋点
- **日志追踪**: 请求 ID 全链路追踪

### 前端技术栈
- **Vue 3**: Composition API
- **TypeScript**: 完整类型系统
- **Vite**: 极速构建工具
- **Tailwind CSS**: 原子化 CSS
- **Pinia**: 轻量级状态管理
- **Vue Router**: 前端路由
- **Axios**: HTTP 客户端
- **Vitest**: 单元测试框架

这套技术栈为企业级短链接服务提供了完整的技术解决方案，兼顾了性能、安全性、可维护性和可观测性。

---

## 📊 技术选型理由

### 后端选型
1. **Spring Boot 3.3.5**: 成熟的微服务框架，生态完善
2. **MyBatis-Plus + JPA**: 灵活性 + 便捷性的平衡
3. **Caffeine + Redis**: 本地 + 分布式二级缓存架构
4. **HikariCP**: 业界最快数据库连接池
5. **JWT**: 无状态认证，适合前后端分离

### 前端选型
1. **Vue 3 + Vite**: 现代化开发体验，热更新速度快
2. **TypeScript**: 类型安全，减少运行时错误
3. **Tailwind CSS**: 原子化 CSS，快速构建 UI
4. **Pinia**: Vue 官方推荐状态管理，轻量高效

### 运维选型
1. **Actuator + Prometheus**: 完善的监控体系
2. **Logback**: 结构化日志，便于 ELK 收集
3. **Gzip 压缩**: 减少网络传输体积
4. **Nginx**: 反向代理，负载均衡