# LaLeMa App

一个基于 Android 的拉屎记录应用。

## 功能特性

- 📅 每日拉屎打卡记录
- 📊 连续打卡天数统计
- 📋 日历视图查看历史记录
- 🔄 补打卡功能

## 技术栈

- **框架**: Android Jetpack Compose
- **语言**: Kotlin
- **架构**: MVVM + Repository
- **依赖注入**: Hilt
- **数据库**: Room

## 环境要求

- Java 17+
- Gradle 8.2+
- Android SDK 34+

## 构建方式

```bash
# 使用本地 Gradle
./gradle-local/gradle-8.2/bin/gradle assembleRelease

# 或者使用 gradle wrapper
./gradlew assembleRelease
```

## 项目结构

```
app/
├── src/main/java/com/lalema/app/
│   ├── data/          # 数据层 (数据库、DAO)
│   ├── domain/        # 业务逻辑层
│   ├── ui/            # UI 层 (Compose 屏幕)
│   ├── LaLeMaApplication.kt
│   └── MainActivity.kt
├── src/main/res/      # 资源文件
└── build.gradle.kts   # 模块配置
```

## 版本管理

### v1.0.0
- 初始版本
- 基础打卡功能
- 日历视图
- 补打卡功能