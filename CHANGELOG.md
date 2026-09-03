# Changelog

所有重要的项目更改都将在此文件中记录。

格式基于 [Keep a Changelog](https://keepachangelog.com/en/1.0.0/)。

## [2.3.0] - 2026-09-03

### Added
- [新增] 动效令牌系统 `GlassMotion`（`ui/theme/LiquidGlass.kt`）：13 个时长常量 + press/control/enter 三组 spring 行为，统一全局动画参数
- [新增] 玻璃输入框组件 `LiquidGlassTextField`（BasicTextField + drawBackdrop 背景 + 聚焦高亮边框），取代全 App 原生 TextField/OutlinedTextField
- [新增] 玻璃文字按钮 `LiquidGlassTextButton`，用于对话框操作按钮

### Changed
- [改进] 全部硬编码动画（6 种 dampingRatio / 9 种 stiffness / 12 种 tween 时长）收敛到 `GlassMotion` 令牌
- [改进] 统一 loading 旋转时长（500/600ms 分歧 → 600ms）、列表入场节奏（Home/Friends/Calendar 对齐）、Settings 展开动画
- [改进] AiConfig 原生 Switch → `LiquidGlassSwitch`
- [改进] 8 处原生 IconButton → `LiquidGlassIconButton`（密码可见性按钮保留在玻璃输入框内）
- [改进] NavHost 页面过渡与 BottomBar 切换动画接入令牌
- [改进] versionName 2.2.0 → 2.3.0，versionCode 12 → 13

### Fixed
- [修复] 清理 6 处死导入（CircularProgressIndicator×4、CardDefaults、LinearEasing）及清理过程产生的重复导入
- [修复] `GlassMotion` 动画令牌返回类型统一为 `FiniteAnimationSpec<T>`，解决 slideInVertically 类型推断失败

## [1.1.0] - Development

### Added
- 准备开发新版本

### Changed

### Fixed

### Changed

### Deprecated

### Removed

### Fixed

### Security

## [1.0.0] - 2026-05-07

### Added
- 基础打卡功能
- 连续打卡天数统计
- 日历视图
- 补打卡功能
- 数据持久化 (Room)
- 项目配置文件完善（README, CHANGELOG, .gitignore）
- 发布脚本 release.bat