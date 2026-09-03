# LaLeMa（拉了吗）

一个基于 Android 的个人排便记录应用，帮助你养成良好的排便习惯。采用 Jetpack Compose + Material 3 构建，支持真正的 Liquid Glass 液态玻璃设计风格，内置 AI 健康分析功能。

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

### AI 健康分析（v2.0.0）
- 🧠 **AI 健康评分** - 基于排便记录的健康评分（0-100分）
- 🍎 **AI 饮食建议** - 根据排便情况推荐个性化饮食方案
- 📈 **AI 趋势预测** - 预测未来排便趋势和异常预警
- ⏰ **AI 智能提醒** - 基于历史数据推荐最佳提醒时间
- 💬 **AI 对话助手** - 支持与大模型对话咨询肠道健康问题
- 🔧 **混合 AI 模式** - 端侧规则引擎 + 云端大模型 API
- 🌐 **多模型支持** - OpenAI / DeepSeek / 通义千问 / 自定义
- 🔒 **数据安全** - API Key 加密存储，支持仅本地 AI 模式

### 好友功能（v2.1.0）
- 👥 **好友系统** - 搜索用户、发送好友请求、接受/拒绝请求
- 📋 **好友列表** - 查看所有好友，支持删除好友
- 📨 **请求管理** - 查看收到的和发送的好友请求
- 🏆 **排便排行榜** - 与好友比拼连续打卡天数和本月打卡次数
- 🔒 **隐私保护** - 仅展示昵称和ID，不暴露敏感信息

### 后端服务
- 🔐 **用户认证** - JWT 无状态认证，支持 Token 自动刷新
- ☁️ **数据同步** - 多设备数据同步，冲突自动解决
- 📊 **统计分析** - 服务端月度统计、趋势分析
- 👥 **好友服务** - 好友关系管理、排行榜计算
- 📦 **Docker 部署** - 一键部署 MySQL + App + Nginx
- 🔒 **HTTPS 支持** - Nginx 反向代理，SSL 证书配置

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

### Android 客户端

| 技术 | 版本 |
|------|------|
| 语言 | Kotlin 2.3.21 |
| UI 框架 | Jetpack Compose (BOM 2026.08.00 / Compose 1.12.0) |
| 架构 | MVVM + Repository |
| 依赖注入 | Hilt 2.60.1 |
| 数据库 | Room 2.8.0 |
| 网络 | Retrofit 2.9.0 + OkHttp 4.12.0 |
| 安全 | EncryptedSharedPreferences |
| 导航 | Navigation Compose 2.9.0 |
| 最低 SDK | Android 8.0 (API 26) |
| 目标 SDK | Android 16 (API 36) |

### 后端服务

| 技术 | 版本 |
|------|------|
| 框架 | Spring Boot 3.2.5 |
| 安全 | Spring Security + JWT |
| ORM | MyBatis-Plus 3.5.5 |
| 数据库 | MySQL 8.0 |
| 文档 | Knife4j 4.4.0 |
| 容器 | Docker + Docker Compose |
| 反向代理 | Nginx + SSL |

## 项目结构

```
lalema/
├── app/src/main/java/com/lalema/app/
│   ├── ai/                    # AI 模块
│   │   ├── AiConfig.kt        # AI 配置数据类
│   │   ├── AiConfigManager.kt # 加密存储管理
│   │   ├── AiModels.kt        # AI 分析结果数据类
│   │   ├── CloudAiService.kt  # 云端 API 调用
│   │   └── LocalAiEngine.kt   # 端侧规则引擎
│   ├── api/                   # 后端 API 对接
│   │   ├── ApiClient.kt       # Retrofit + JWT 拦截器
│   │   ├── ApiModels.kt       # API 数据类
│   │   └── ApiService.kt      # API 接口定义
│   ├── data/                  # 数据层
│   │   ├── AppDatabase.kt     # Room 数据库
│   │   ├── PoopRecord.kt      # 实体类 + 枚举
│   │   ├── PoopRecordDao.kt   # DAO 接口
│   │   ├── SyncManager.kt     # 数据同步管理
│   │   └── di/DatabaseModule.kt
│   ├── domain/
│   │   └── PoopRepository.kt  # 业务逻辑层
│   ├── ui/
│   │   ├── ai/                # AI 功能页面
│   │   │   ├── AiScreen.kt    # AI 助手主页
│   │   │   ├── AiChatScreen.kt# AI 对话页面
│   │   │   └── AiConfigScreen.kt # AI 配置页面
│   │   ├── auth/
│   │   │   └── AuthScreen.kt  # 登录/注册页面
│   │   ├── friends/           # 好友功能页面
│   │   │   └── FriendsScreen.kt # 好友列表/请求/排行榜
│   │   ├── home/              # 主页
│   │   ├── calendar/          # 日历
│   │   ├── settings/          # 设置
│   │   ├── navigation/        # 导航框架
│   │   └── theme/             # 主题 + Liquid Glass 组件
│   ├── reminder/              # 提醒功能
│   └── LaLeMaApplication.kt  # Hilt 入口
│
└── backend/                   # Spring Boot 后端
    ├── pom.xml
    ├── Dockerfile
    ├── docker-compose.yml
    └── src/main/
        ├── java/com/lalema/backend/
        │   ├── config/        # Security + MyBatis 配置
        │   ├── controller/    # REST 控制器
        │   ├── service/       # 业务逻辑
        │   ├── mapper/        # MyBatis Mapper
        │   ├── entity/        # 数据库实体
        │   ├── dto/           # 请求/响应 DTO
        │   └── util/          # JWT 工具类
        └── resources/
            ├── application.yml
            └── schema.sql     # 建表 SQL
```

## 后端 API

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/auth/register` | 注册 |
| POST | `/api/auth/login` | 登录 |
| GET | `/api/auth/me` | 获取当前用户 |
| POST | `/api/records` | 保存记录 |
| POST | `/api/records/sync` | 批量同步 |
| GET | `/api/records` | 分页查询 |
| GET | `/api/records/date/{date}` | 按日期查询 |
| GET | `/api/records/stats` | 月度统计 |
| DELETE | `/api/records/{id}` | 删除记录 |
| **好友接口** ||
| GET | `/api/friends/search` | 搜索用户 |
| POST | `/api/friends/request` | 发送好友请求 |
| POST | `/api/friends/accept/{requestId}` | 接受请求 |
| POST | `/api/friends/reject/{requestId}` | 拒绝请求 |
| DELETE | `/api/friends/{friendId}` | 删除好友 |
| GET | `/api/friends/list` | 获取好友列表 |
| GET | `/api/friends/requests` | 获取待处理请求 |
| GET | `/api/friends/requests/count` | 获取请求数量 |
| GET | `/api/friends/leaderboard` | 获取排行榜 |
| GET | `/api/friends/stats/{friendId}` | 获取好友统计 |

## 环境要求

- Java 17+
- Gradle 9.6+
- Android SDK 34+

## 构建方式

### Android 客户端

```bash
# 使用本地 Gradle
./gradle-local/gradle-8.2/bin/gradle assembleRelease

# 使用 Gradle Wrapper
./gradlew assembleRelease
```

APK 输出路径：`app/build/outputs/apk/release/app-release.apk`

### 后端服务

```bash
cd backend
docker-compose up -d
```

## 版本历史

### v2.3.0
- ✨ **动效令牌系统** - 新增 `GlassMotion`（13 个时长常量 + press/control/enter 三组 spring），全部硬编码动画收敛统一
- 🧩 **组件补齐** - 新建 `LiquidGlassTextField`（玻璃输入框，聚焦高亮边框）与 `LiquidGlassTextButton`（玻璃对话框按钮）
- 🔄 **全量组件迁移** - 原生 Switch/IconButton/TextField/TextButton 全部替换为液态玻璃组件，视觉残留清零
- 🧹 **清理** - 6 处死导入移除，`FiniteAnimationSpec` 类型推断修复

### v2.2.0
- 🍸 **Liquid Glass 重构** - 引入 kyant/backdrop 2.0.1 设计系统，全 App 玻璃化视觉统一（替换手写玻璃效果）
- 🛠 **构建升级** - AGP 9.3.2 / Kotlin 2.3.21 / Compose BOM 2026.08.00 / Hilt 2.60.1 / Room 2.8.0
- 🧹 **代码清理** - 移除死代码 NavGraph、未使用的 Redis 依赖

### v2.1.1
- 🐛 **修复添加好友无响应** - 后端返回强类型 DTO 替代 Map，解决 Gson 反序列化问题
- 🔒 **SSL 证书部署** - 配置 HTTPS 访问，支持域名 www.5ichat.online
- 🎨 **优化底栏样式** - 底栏左右更宽，视觉更平衡
- 👤 **设置页个人信息** - 登录后显示头像、昵称、用户名

### v2.1.0
- 👥 **好友系统** - 搜索用户、发送/接受/拒绝好友请求、删除好友
- 📨 **请求管理** - 查看收到和发送的好友请求
- 🏆 **排便排行榜** - 与好友比拼连续打卡天数和本月打卡次数
- 📱 **5个底部导航 Tab** - 主页、日历、好友、AI 助手、设置

### v2.0.0
- 🧠 **AI 健康分析** - 健康评分、饮食建议、趋势预测、智能提醒、对话助手
- 🔧 **混合 AI 模式** - 端侧规则引擎 + 云端大模型 API（OpenAI/DeepSeek/通义千问）
- 🔐 **用户系统** - 注册/登录，JWT 认证，Token 加密存储
- ☁️ **数据同步** - 多设备数据同步，冲突自动解决
- 📦 **后端服务** - Spring Boot + MySQL + Redis + Docker 一键部署
- 📱 **4个底部导航 Tab** - 主页、日历、AI 助手、设置

### v1.9.0
- 📐 全面优化界面布局，缩小底栏和各组件尺寸
- 🎯 统一间距规范，提升视觉紧凑度

### v1.8.0
- 🐛 修复打卡成功提醒重复弹出
- 🐛 修复日历添加记录无响应
- 🐛 修复打卡率计算错误
- 🎨 统一弹窗样式（Glass 风格）

### v1.7.0
- 🍸 真正的 Liquid Glass 液态玻璃设计
- 🐛 修复主题切换失效
- 🐛 修复打卡率计算
- 🐛 修复日历显示不全
- 🐛 修复提醒功能消失

### v1.6.0
- 🎨 增强文字可读性
- 🌓 主题模式切换（跟随系统/浅色/深色）
- 🎨 6套配色方案
- 📱 安卓16实况通知
- 🔄 应用内检查更新

### v1.5.0
- 🐛 修复连续打卡无限循环
- 🐛 修复导航返回数据不刷新

### v1.4.0
- 🎨 全新蓝紫配色
- 🌙 完善深色模式
- 🔔 修复闹钟重启丢失

### v1.3.0
- ✨ 全新 Liquid Glass 设计风格
- 🎨 优化界面 UI 和配色方案
- 🔔 支持自定义提醒时间

### v1.2.0
- 🎨 新增详细记录表单
- ⏰ 新增自定义时间选择器
- 📊 支持同一天多条记录
- 🗑️ 日历支持删除单条记录

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
- [x] AI 健康分析功能
- [x] 用户系统 + 数据同步
- [x] Spring Boot 后端服务
- [x] 好友系统 + 排行榜
- [x] HTTPS + 域名部署
- [ ] 数据导出功能
- [ ] 多语言支持

## License

MIT License
