# LaLeMa（拉了吗）

一个基于 Android 的宠物/个人排便记录应用，帮助你养成良好的排便习惯。采用 Jetpack Compose + Material 3 构建，支持 Liquid Glass 液态玻璃设计风格。

## 功能特性

### 核心功能
- 📅 每日排便打卡记录
- 📊 连续打卡天数统计
- 📈 本月排便次数和打卡天数统计
- 📅 日历视图查看历史记录
- 🔄 补打卡功能（支持近7天）

### 详细记录
- ⏰ 自定义时间选择（TimePickerDialog）
- 📏 排便量记录（少量、正常、大量）
- 💧 干稀程度（非常干、较干、正常、偏软、很软、稀便）
- 🎨 颜色记录（8种颜色：棕色、深棕、黄色、绿色、红色、黑色、灰白、橙色）
- 👃 气味记录（正常、稍有气味、气味较重、非常臭）
- 🤕 疼痛程度（无疼痛、轻微、中等、严重）
- 🩸 特殊标记（有血、有粘液）
- 📝 备注信息

### 数据展示
- 🔥 连续打卡天数
- 📊 月度排便次数
- 📈 月度打卡率
- 📱 支持同一天多条记录

### 界面设计
- 🍸 **Liquid Glass 液态玻璃设计** - 现代化毛玻璃效果
- 🎨 **蓝紫配色方案** - 日间/夜间模式自适应
- ✨ 流畅的入场动画和按钮弹动效果
- 📐 Material 3 设计规范

### 提醒功能
- ⏰ **闹钟提醒** - 支持自定义时间，重启后自动恢复
- 📅 **日历提醒** - 在系统日历中创建每日提醒事件
- 🔔 开机自动恢复闹钟设置（BootReceiver）

## 技术栈

| 技术 | 版本 |
|------|------|
| 语言 | Kotlin 1.9.22 |
| UI 框架 | Jetpack Compose (BOM 2024.01.00) |
| 架构 | MVVM + Repository |
| 依赖注入 | Hilt 2.50 |
| 数据库 | Room 2.6.1 |
| 导航 | Navigation Compose 2.7.6 |
| 最低 SDK | Android 8.0 (API 26) |
| 目标 SDK | Android 14 (API 34) |

## 环境要求

- Java 17+
- Gradle 8.2+
- Android SDK 34+

## 构建方式

### 使用本地 Gradle

```bash
./gradle-local/gradle-8.2/bin/gradle assembleRelease
```

### 使用 Gradle Wrapper

```bash
./gradlew assembleRelease
```

APK 输出路径：`app/build/outputs/apk/release/app-release.apk`

## 项目结构

```
app/src/main/java/com/lalema/app/
├── data/                    # 数据层
│   ├── AppDatabase.kt       # Room 数据库定义
│   ├── PoopRecord.kt        # 实体类 + 枚举定义
│   ├── PoopRecordDao.kt     # DAO 接口
│   └── di/
│       └── DatabaseModule.kt # Hilt 依赖注入模块
├── domain/
│   └── PoopRepository.kt   # 业务逻辑层（打卡、统计、连续天数）
├── ui/
│   ├── home/
│   │   ├── HomeScreen.kt    # 主页 UI
│   │   ├── HomeViewModel.kt # 主页状态管理
│   │   └── PoopRecordForm.kt# 记录表单（底部弹窗）
│   ├── calendar/
│   │   ├── CalendarScreen.kt # 日历视图
│   │   └── CalendarViewModel.kt
│   ├── settings/
│   │   └── SettingsScreen.kt # 设置页（闹钟/日历提醒）
│   ├── navigation/
│   │   ├── MainScreen.kt    # 主框架 + 底部导航
│   │   └── Screen.kt        # 路由定义
│   └── theme/
│       ├── Color.kt         # 颜色定义
│       ├── Theme.kt         # 主题配置
│       ├── Type.kt          # 字体样式
│       └── LiquidGlass.kt   # 液态玻璃组件库
├── reminder/
│   ├── ReminderManager.kt   # 闹钟管理 + 日历事件
│   └── BootReceiver.kt      # 开机恢复闹钟
├── LaLeMaApplication.kt     # Hilt 入口
└── MainActivity.kt          # 主 Activity
```

## 版本历史

### v1.5.0
- 🐛 **修复连续打卡无限循环** - 添加365天上限保护
- 🐛 **修复导航返回数据不刷新** - 基于路由感知的智能刷新
- 🧹 **代码清理** - 移除重复调用、死代码和无效注解
- 📦 **版本升级** - versionCode 5, versionName 1.5.0

### v1.4.0
- 🎨 **全新蓝紫配色** - 更现代、更清新的视觉体验
- 🌙 **完善深色模式** - 自动适配系统日间/夜间主题
- 🔔 **修复闹钟重启丢失** - 添加 BootReceiver，开机自动恢复
- 📅 **修复日历创建失败** - 优化日历账户检测和错误提示
- 🍸 **优化液态玻璃效果** - 更好的透明度和边框效果

### v1.3.0
- ✨ 全新 Liquid Glass 设计风格
- 🎨 优化界面 UI 和配色方案
- 📐 重新设计主页和日历布局
- 💫 添加毛玻璃效果组件
- 📊 改进统计卡片展示
- 🔔 支持自定义提醒时间

### v1.2.0
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
- [x] 排便提醒功能（闹钟、日历）
- [ ] 数据导出功能
- [ ] 健康数据分析报告
- [ ] 多语言支持
