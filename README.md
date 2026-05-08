# LaLeMa（拉了吗）

一个基于 Android 的个人排便记录应用，帮助你养成良好的排便习惯。采用 Jetpack Compose + Material 3 构建，支持真正的 Liquid Glass 液态玻璃设计风格。

## 功能特性

### 核心功能
- 📅 每日排便打卡记录
- 📊 连续打卡天数统计
- 📈 本月排便次数和打卡率统计
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
- 📈 月度打卡率（打卡天数/当月总天数）
- 📱 支持同一天多条记录

### 界面设计
- 🍸 **真正的 Liquid Glass 液态玻璃设计**
  - 真实玻璃半透明磨砂质感
  - 光影折射反射与流体悬浮流动感
  - 界面分层悬浮、底层内容穿透有景深
  - 自适应明暗背景和深浅色模式
  - 柔和大圆角无硬边（24dp）
  - 细腻渐变玻璃光泽描边
  - 物理级自然缓动动效
- 🎨 **6套主题配色** - 蓝紫、樱花粉、薄荷绿、琥珀橙、靛蓝、玫瑰红
- 🌓 **三种主题模式** - 跟随系统/浅色/深色
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
| 目标 SDK | Android 16 (API 36) |

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
│   │   ├── SettingsScreen.kt # 设置页（主题/提醒/更新）
│   │   └── SettingsViewModel.kt
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

### v1.7.0
- 🍸 **真正的 Liquid Glass 液态玻璃设计**
  - 真实玻璃半透明磨砂质感，带光影折射反射
  - 流体悬浮流动感，界面分层悬浮有景深
  - 底层内容穿透，自适应明暗背景
  - 柔和大圆角无硬边，细腻渐变玻璃光泽
  - 物理级自然缓动动效
- 🐛 **修复主题切换失效** - 使用 key 强制重组
- 🐛 **修复打卡率计算** - 改为打卡天数/当月总天数
- 🐛 **修复日历显示不全** - 增加 LazyVerticalGrid 高度
- 🐛 **修复提醒功能消失** - 恢复 ReminderManager 调度

### v1.6.0
- 🎨 **增强文字可读性** - 优化配色对比度
- 🌓 **主题模式切换** - 支持跟随系统/浅色/深色三种模式
- 🎨 **6套配色方案** - 蓝紫、樱花粉、薄荷绿、琥珀橙、靛蓝、玫瑰红
- 🔢 **修复应用内版本号** - 从 BuildConfig 动态读取
- 📱 **安卓16实况通知** - API 36+ 使用 ProgressStyle 显示打卡进度
- 🔄 **应用内检查更新** - 通过 GitHub API 检查最新版本
- 📦 **升级目标SDK** - 从 API 34 升级到 API 36

### v1.5.0
- 🐛 **修复连续打卡无限循环** - 添加365天上限保护
- 🐛 **修复导航返回数据不刷新** - 基于路由感知的智能刷新
- 🧹 **代码清理** - 移除重复调用、死代码和无效注解

### v1.4.0
- 🎨 **全新蓝紫配色** - 更现代、更清新的视觉体验
- 🌙 **完善深色模式** - 自动适配系统日间/夜间主题
- 🔔 **修复闹钟重启丢失** - 添加 BootReceiver，开机自动恢复
- 📅 **修复日历创建失败** - 优化日历账户检测和错误提示

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

- [x] Android 16 Live Activities 实况通知
- [x] 排便提醒功能（闹钟、日历）
- [x] 多主题配色方案
- [x] 应用内检查更新
- [x] 真正的 Liquid Glass 液态玻璃设计
- [ ] 数据导出功能
- [ ] 健康数据分析报告
- [ ] 多语言支持
