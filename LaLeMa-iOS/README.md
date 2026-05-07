# LaLeMa-iOS 构建指南

## 项目概述

LaLeMa（拉了吗）是一个排便记录应用，现在已成功重构为原生 iOS 应用。

### 技术栈

- **UI 框架**: SwiftUI
- **架构**: MVVM
- **数据库**: SQLite (原生 iOS)
- **目标系统**: iOS 16.0+
- **开发语言**: Swift 5.9

---

## 快速开始

### 前提条件

1. **macOS 设备** (需要完整的 macOS 开发环境)
2. **Xcode 15.0+**
3. **命令行工具**: `xcodegen`

### 安装 XcodeGen

如果你还没有安装 XcodeGen，请先安装：

```bash
brew install xcodegen
```

### 编译步骤

1. **打开终端**，进入项目目录：

```bash
cd /path/to/LaLeMa-iOS
```

2. **生成 Xcode 项目**：

```bash
xcodegen generate
```

3. **打开 Xcode 项目**：

```bash
open LaLeMa.xcodeproj
```

4. **在 Xcode 中编译**：

   - 选择目标设备（模拟器或真机）
   - 按 `Cmd + B` 编译项目
   - 按 `Cmd + R` 运行应用

---

## 构建 IPA 安装包

### 方法一：使用 Xcode 图形界面

1. 在 Xcode 中，选择 **Product** → **Archive**
2. 等待编译完成
3. 在 Organizer 窗口中，选择你的 App
4. 点击 **Distribute App**
5. 选择 **Development** 或 **Ad Hoc**
6. 按照提示完成签名和导出

### 方法二：使用命令行

1. **编译项目**：

```bash
xcodebuild -project LaLeMa.xcodeproj -scheme LaLeMa -configuration Release -destination 'generic/platform=iOS' build
```

2. **导出 IPA**：

```bash
xcodebuild -project LaLeMa.xcodeproj -scheme LaLeMa -configuration Release -destination 'generic/platform=iOS' -archivePath build/LaLeMa.xcarchive exportArchive -exportPath output -exportOptionsPlist exportOptions.plist
```

### 创建 exportOptions.plist

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE plist PUBLIC "-//Apple//DTD PLIST 1.0//EN" "http://www.apple.com/DTDs/PropertyList-1.0.dtd">
<plist version="1.0">
<dict>
    <key>method</key>
    <string>development</string>
    <key>teamID</key>
    <string>YOUR_TEAM_ID</string>
</dict>
</plist>
```

---

## 安装 IPA

### 安装到已连接的真机

使用 Xcode 自动安装（运行即可）

或使用命令行：

```bash
xcrun simctl boot "iPhone 15"
xcrun simctl install booted /path/to/LaLeMa.ipa
```

### 使用 AltStore 安装（免签名）

1. 下载并安装 [AltServer](https://altstore.io/)
2. 将 IPA 文件拖入 AltServer
3. 选择设备进行安装

### 使用第三方工具安装

- **爱思助手**
- **PP助手**
- **同步助手**

---

## 项目结构

```
LaLeMa-iOS/
├── Sources/
│   ├── App/
│   │   └── LaLeMaApp.swift          # App 入口
│   ├── Models/
│   │   └── PoopRecord.swift          # 数据模型
│   ├── Database/
│   │   └── DatabaseManager.swift     # SQLite 数据库管理
│   ├── Services/
│   │   ├── PoopRepository.swift      # 数据仓库
│   │   └── ReminderService.swift     # 提醒服务
│   ├── ViewModels/
│   │   ├── HomeViewModel.swift       # 主页 ViewModel
│   │   └── CalendarViewModel.swift   # 日历 ViewModel
│   └── Views/
│       ├── Home/
│       │   └── HomeScreen.swift      # 主页
│       ├── Calendar/
│       │   └── CalendarScreen.swift   # 日历
│       ├── Settings/
│       │   └── SettingsScreen.swift  # 设置
│       ├── Components/
│       │   ├── Components.swift      # 通用组件
│       │   └── RecordFormView.swift  # 记录表单
│       └── Theme/
│           └── AppColors.swift       # 主题颜色
├── Resources/
│   └── Assets.xcassets/              # 资源文件
└── project.yml                       # XcodeGen 配置
```

---

## 功能特性

- ✅ 每日排便打卡记录
- ✅ 连续打卡天数统计
- ✅ 本月排便次数统计
- ✅ 日历视图查看历史
- ✅ 补打卡功能（近7天）
- ✅ 详细记录（时间、量、干稀、颜色、气味、疼痛等）
- ✅ 闹钟提醒功能
- ✅ 液态玻璃设计风格

---

## 常见问题

### Q: 编译报错 "No such module 'SQLite3'"

确保在项目中正确链接了 libsqlite3.tbd：

在 Xcode 中：
1. 选择项目 → TARGETS → LaLeMa
2. 选择 "Build Phases" → "Link Binary With Libraries"
3. 点击 "+" 添加 libsqlite3.tbd

### Q: 无法签名

在 Xcode 中配置签名：
1. 选择项目 → TARGETS → LaLeMa
2. 选择 "Signing & Capabilities"
3. 勾选 "Automatically manage signing"
4. 选择你的 Team

### Q: 真机运行提示 "No devices"

确保：
1. 连接了 iOS 设备
2. 设备已授权此电脑
3. 设备已解锁

---

## 技术支持

如有问题，请提交 Issue 或联系开发者。
