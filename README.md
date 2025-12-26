# Web-Crawl

一个基于 Spring Boot + Kotlin + WebMagic 的企业级网页爬虫项目，支持静态和动态页面爬取、链接检查、请求限流等功能。

## 技术栈

- **Kotlin** 2.1.21
- **Spring Boot** 3.4.5
- **Spring Data JPA** - 数据持久化
- **MySQL** - 关系型数据库
- **WebMagic** 1.0.3 - 轻量级爬虫框架
- **Selenium** 4.35.0 - 动态页面爬取
- **Hutool** 6.0.0 - Java 工具类库
- **Gradle** 8.13 - 构建工具
- **JDK** 21

## 主要功能

- ✅ 网页链接检查与验证
- ✅ 静态页面爬取（WebMagic）
- ✅ 动态页面爬取（Selenium WebDriver）
- ✅ WebDriver 连接池管理
- ✅ 请求频率限流
- ✅ 链接去重与缓存
- ✅ 爬取结果持久化存储
- ✅ 多浏览器支持（Chrome、Firefox）

## 项目结构

```
web_crawl/
├── src/main/
│   ├── kotlin/me/lbb/crawl/
│   │   ├── BootApplication.kt              # Spring Boot 启动类
│   │   ├── entity/                         # 实体类
│   │   │   ├── LinkCheckResult.kt         # 链接检查结果实体
│   │   │   └── LinkCheckTask.kt           # 链接检查任务实体
│   │   ├── repository/                     # 数据访问层
│   │   │   ├── LinkCheckResultRepository.kt
│   │   │   └── LinkCheckTaskRepository.kt
│   │   ├── service/                        # 服务层
│   │   │   ├── LinkCheckService.kt        # 服务接口
│   │   │   └── impl/
│   │   │       └── LinkCheckServiceImpl.kt # 服务实现
│   │   └── suport/                         # 工具与支持类
│   │       ├── CacheHelper.kt             # 缓存助手
│   │       ├── EnhanceHttpClientDownloader.kt # 增强 HTTP 下载器
│   │       ├── LinkCheckListener.kt       # 链接检查监听器
│   │       ├── LinkCheckPageProcessor.kt  # 页面处理器
│   │       ├── LinkCheckPipeline.kt       # 数据管道
│   │       ├── LinkType.kt                # 链接类型枚举
│   │       ├── LinkValidator.kt           # 链接验证器
│   │       ├── RequestRateLimiter.kt      # 请求限流器
│   │       ├── SeleniumDownloader.kt      # Selenium 下载器
│   │       ├── UrlHelper.kt               # URL 工具类
│   │       ├── ValidationResult.kt        # 验证结果
│   │       └── WebDriverPool.kt           # WebDriver 连接池
│   └── resources/
│       └── application.yml                 # 应用配置文件
├── build.gradle.kts                        # Gradle 构建配置
├── settings.gradle.kts                     # Gradle 设置
└── gradle.properties                       # Gradle 属性配置
```

## 环境要求

### 必需软件
- JDK 21+
- MySQL 8.0+
- Gradle 8.13+（已包含 Gradle Wrapper）

### 浏览器驱动（可选，用于动态页面爬取）
- **ChromeDriver**: [下载地址](https://chromedriver.chromium.org/)
- **GeckoDriver** (Firefox): [下载地址](https://github.com/mozilla/geckodriver/releases)

## 快速开始

### 1. 克隆项目
```bash
git clone <repository-url>
cd web_crawl
```

### 2. 配置数据库
在 MySQL 中创建数据库：
```sql
CREATE DATABASE newdee CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### 3. 配置应用
编辑 `src/main/resources/application.yml`：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/newdee?useUnicode=true&characterEncoding=utf-8
    username: your_username
    password: your_password

crawl:
  chromedriver:
    path: /path/to/chromedriver  # 配置 ChromeDriver 路径
  geckodriver:
    path: /path/to/geckodriver   # 配置 GeckoDriver 路径
```

### 4. 构建项目
```bash
# Windows
.\gradlew.bat clean build -x test

# Linux/Mac
./gradlew clean build -x test
```

### 5. 运行项目
```bash
# Windows
.\gradlew.bat bootRun

# Linux/Mac
./gradlew bootRun
```

项目将在 `http://localhost:8080` 启动。

## 配置说明

### 数据库配置
```yaml
spring:
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://host:port/database
    username: your_username
    password: your_password
  jpa:
    hibernate:
      ddl-auto: none  # 生产环境建议使用 none
```

### 爬虫配置
```yaml
crawl:
  chromedriver:
    path: /path/to/chromedriver
  geckodriver:
    path: /path/to/geckodriver
```

### 日志配置
日志文件位置：`logs/web-crawl.log`

## 使用示例

### 链接检查服务
```kotlin
@Autowired
private lateinit var linkCheckService: LinkCheckService

// 执行链接检查
linkCheckService.checkLinks(startUrl, maxDepth)
```

## 开发指南

### 代码规范
- 遵循 Kotlin 官方编码规范
- 使用 Kotlin DSL 编写 Gradle 构建脚本
- 实体类使用 JPA 注解
- 服务层使用 Spring 事务管理

### 添加新的爬虫任务
1. 创建 `PageProcessor` 实现
2. 配置 `Pipeline` 处理数据
3. 使用 `Spider` 启动爬虫任务

## 参考资料

- [WebMagic 官方文档](https://webmagic.io/)
- [Spring Boot 官方文档](https://spring.io/projects/spring-boot)
- [Kotlin 官方文档](https://kotlinlang.org/docs/home.html)
- [Selenium WebDriver 文档](https://www.selenium.dev/documentation/)

## 许可证

[Apache License 2.0](http://www.apache.org/licenses/LICENSE-2.0)

## 贡献

欢迎提交 Issue 和 Pull Request！

## 作者

@csy
