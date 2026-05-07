# LaLeMa App

一个基于 Android 的拉屎记录应用，帮助你养成良好的排便习惯。

## 功能特性

### 核心功能
- 📅 每日排便打卡记录
- 📊 连续打卡天数统计
- 📈 本月排便次数统计
- 📅 日历视图查看历史记录
- 🔄 补打卡功能（支持近7天）

### 详细记录
- ⏰ 自定义时间选择
- 📏 排便量记录（少量、正常、大量）
- 💧 干稀程度（干、正常、稀、水样）
- 🎨 颜色记录（6种颜色选项）
- 👃 气味记录（无味、正常、轻微、强烈）
- 🤕 疼痛程度（无疼痛、轻微、中等、严重）
- 🩸 特殊标记（有血、有粘液）
- 📝 备注信息

### 数据展示
- 🔥 连续打卡天数
- 📊 月度排便次数
- 📈 月度打卡率
- 📱 支持同一天多条记录

### 界面设计
- 🍸 **Liquid Glass 设计** - 现代化毛玻璃效果
- 🎨 精心优化的配色方案
- ✨ 流畅的动画效果
- 📐 合理的间距和布局

### 提醒功能（开发中）
- ⏰ 闹钟提醒
- 📅 日历事件提醒
- 🔔 Android 16 Live Activities 支持（计划中）

## 技术栈

- **框架**: Android Jetpack Compose
- **语言**: Kotlin
- **架构**: MVVM + Repository
- **依赖注入**: Hilt
- **数据库**: Room
- **最低 SDK**: Android 8.0 (API 26)
- **目标 SDK**: Android 14 (API 34)

## 环境要求

- Java 17+
- Gradle 8.2+
- Android SDK 34+

## 构建方式

### 使用本地 Gradle

```bash
./gradle-local/gradle-8.2/bin/gradle assembleRelease
```

### 使用 gradle wrapper

```bash
./gradlew assembleRelease
```

## 项目结构

```
app/
├── src/main/java/com/lalema/app/
│   ├── data/              # 数据层
│   │   ├── data/         # 数据库、DAO、实体类
│   │   └── di/           # 依赖注入模块
│   ├── domain/            # 业务逻辑层
│   │   └── PoopRepository.kt
│   ├── ui/                # UI 层
│   │   ├── home/         # 主页
│   │   ├── calendar/     # 日历页
│   │   ├── navigation/   # 导航配置
│   │   └── theme/        # 主题配置（含Liquid Glass组件）
│   ├── LaLeMaApplication.kt
│   └── MainActivity.kt
├── src/main/res/          # 资源文件
└── build.gradle.kts      # 模块配置
```

## 版本历史

### v1.2.0
- ✨ 全新 Liquid Glass 设计风格
- 🎨 优化界面 UI 和配色方案
- 📐 重新设计主页和日历布局
- 💫 添加毛玻璃效果组件
- 📊 改进统计卡片展示
- 🔔 准备支持提醒功能

### v1.1.0
- ✨ 优化主页统计：本月次数改为统计排便次数
- 🎨 新增详细记录表单（时间、量、干稀、颜色、气味、疼痛、血、粘液等）
- ⏰ 新增自定义时间选择器
- 📊 支持同一天多条排便记录
- 🗑️ 日历支持删除单条记录
- 🐛 修复底部导航栏切换问题

### v1.0.0
- 初始版本
- 基础打卡功能
- 日历视图
- 补打卡功能

## 开发路线图

- [ ] Android 16 Live Activities 实况通知
- [ ] 排便提醒功能（闹钟、日历）
- [ ] 数据导出功能
- [ ] 健康数据分析报告
- [ ] 多语言支持
