# 前端设计系统重写：引入 kyant/backdrop 液态玻璃

将手写的 `ui/theme/LiquidGlass.kt`（约 580 行模拟实现）替换为
[kyant/backdrop](https://github.com/Kyant0/AndroidLiquidGlass)（2.0.1）真正的
"录制背景图层 → 模糊 / 折射 → 回绘"液态玻璃效果。仅动设计系统层，5 个业务页面
（Home / Calendar / Friends / AI / Settings）的调用签名保持不变。

---

## 1. 版本升级对照表

| 组件 | 升级前 | 升级后 | 说明 |
|------|--------|--------|------|
| AGP | 8.2.0 | 8.7.3 | |
| Kotlin | 1.9.22 | 2.0.21 | 改用 `org.jetbrains.kotlin.plugin.compose`，**移除** `kotlinCompilerExtensionVersion` |
| Compose BOM | 2024.01.00 (≈1.6.0) | 2024.12.01 (≈1.7.5) | **必须 ≥ 1.7.0**，`GraphicsLayer` / `drawLayer` / 链式 `BlurEffect` 均为 1.7.0 新增 |
| KSP | 1.9.22-1.0.17 | 2.0.21-1.0.27 | |
| Hilt | 2.50 | 2.53.1 | |
| Room | 2.6.x | 2.7.0 | |
| Navigation | 2.7.x | 2.8.5 | |
| Gradle wrapper | 8.2 | 8.9 | |
| compileSdk / targetSdk | 36 | 36 | 保持 |
| minSdk | 26 | 26 | 保持 |

> backdrop 库作者在 KMP 工程里锁的是 AGP 9.3.2 / Kotlin 2.4.10 / CMP 1.12（≈ Compose 1.8）。
> 本工程采用更保守且相互兼容的 **Kotlin 2.0.21 + Compose 1.7.5** 组合，已满足所有
> `GraphicsLayer` 重构 API 的最低要求。若后续运行时出现兼容性问题，可优先尝试把
> Compose BOM 提到 `2025.01.01`（Compose 1.8.0）。

---

## 2. 新建 `:backdrop` Android Library Module

`AndroidLiquidGlass-kmp` 是 Compose **Multiplatform** 工程，含 common/jvm/js/wasm/ios/macos
多平台源码。本项目是纯 Android，因此：

1. 只复制 `backdrop/src/commonMain` + `backdrop/src/androidMain` 的 Kotlin 源码到
   `backdrop/src/main/java/`。
2. 合并 4 组 `expect` / `actual` 为纯 Android 实现（去掉关键字）：
   - `Platform.kt`：`expect fun debugLog()` → 直接实现 `fun debugLog()`
   - `RuntimeShader.kt`：`expect` 的 `RuntimeShader` 封装 → Android `android.graphics.RuntimeShader`
   - `internal/Paint.kt`：平台画笔差异合并
   - `internal/RenderEffect.kt`：`expect` 的 RenderEffect 工厂 → Android `RenderEffect`
3. 外部依赖（在 `backdrop/build.gradle.kts`）：
   - `io.github.kyant0:shapes:1.2.1` —— 提供 `RoundedRectangularShape`，`lens` 折射效果依赖它
   - `org.jetbrains:annotations:26.1.0`（`compileOnly`）—— `@Language("AGSL")` 注解
   - Compose BOM `2024.12.01` + `ui` / `ui-graphics` / `ui-util` / `foundation`
4. `lint { disable += "NewApi" }` —— 库内部已用 `isRenderEffectSupported()` /
   `isRuntimeShaderSupported()` 做运行时降级，低版本设备仅失去模糊与折射，不会崩溃。

---

## 3. 玻璃效果的接入机制

backdrop 的核心是"玻璃组件模糊其背后的内容"，因此需要**一个背景内容源**：

```
MainActivity
  └─ rememberGlassBackdrop()            // 创建 LayerBackdrop（记录背景图层）
  └─ ProvideGlassBackdrop(backdrop) {   // 通过 CompositionLocal 向下传递
       Box {
         GlassBackground(Modifier.layerBackdrop(backdrop))  // 把背景渐变录制进图层
         MainScreen { ... 玻璃卡片 / 底栏 ... }             // 读取图层并模糊回绘
       }
     }
```

- `LocalGlassBackdrop`：默认值为 `emptyBackdrop()`（纯色表面，不会崩溃）。
- 每个玻璃组件（`LiquidGlassCard` / `LiquidGlassButton` / 底栏等）内部调用
  `Modifier.drawBackdrop(backdrop, shape, effects, highlight, shadow, innerShadow, ...)`。
- `effects` 链：`vibrancy()` → `blur(radius)` → `lens(height, amount)` 实现
  饱和度增强 + 背景模糊 + 边缘折射。

---

## 4. 新设计系统 API（对外签名不变）

`ui/theme/LiquidGlass.kt` 保留 8 个对外组件，签名与重写前一致：

| 组件 | 用途 |
|------|------|
| `GlassBackground(modifier)` | 全屏背景渐变，须挂 `.layerBackdrop(backdrop)` |
| `LiquidGlassCard(modifier, onClick?, cornerRadius, content)` | 卡片 |
| `LiquidGlassButton(text, onClick, modifier, enabled)` | 按钮 |
| `LiquidGlassStatCard(value, label, modifier, icon)` | 统计卡 |
| `LiquidGlassDivider(modifier)` | 分隔线（薄区域，仍用渐变） |
| `LiquidGlassSurface(modifier, cornerRadius, content)` | 表面容器 |
| `GlassInlineTimePicker(hour, minute, onHourChange, onMinuteChange)` | 时间选择器 |
| `GlassArrowButton(isDark, isUp, onClick)` | 箭头按钮 |

新增的桥接辅助（供接入使用）：
- `rememberGlassBackdrop(): LayerBackdrop`
- `ProvideGlassBackdrop(backdrop, content)`
- `Modifier.glassBackdrop(backdrop)`

---

## 5. 编译所需环境

- **Android SDK**：`local.properties` 已写入 `sdk.dir=D\:\\SDK\\SDK`
  （`D:\SDK\SDK`，含 platform 34/36/37、build-tools 37.0.0）。
- **签名**：`app/key.jks` 已生成（alias `lalema`，store/key 密码 `android`，
  与 `app/build.gradle.kts` 的 `signingConfigs.release` 硬编码一致）。
- **Gradle**：项目用 wrapper（8.9），首次构建需联网下载。
  无 Android Studio，仅命令行。
- **JDK**：17（AGP 8.7 要求）。

```bash
cd lalema-main
./gradlew :app:assembleDebug      # 或 assembleRelease
```

---

## 6. 低版本设备降级行为

| API 级别 | 模糊 (RenderEffect) | 折射 (RuntimeShader) | 表现 |
|----------|--------------------|--------------------|------|
| ≥ 33 (TIRAMISU) | ✅ | ✅ | 完整液态玻璃 |
| 31–32 (S / Sv2) | ✅ | ❌ | 有模糊无折射 |
| < 31 | ❌ | ❌ | 退化为纯色半透明表面（不崩） |

---

## 7. 已知风险与待办

1. **未做本地编译验证**：当前环境无 Android Studio、Gradle 未下载，所有改动未经
   `gradlew` 编译。请在本地同步后确认无误。
2. **Compose BOM 版本**：用的是 1.7.5（库作者建议 1.8）。如遇 `GraphicsLayer` 相关
   运行时异常，优先升级 BOM 到 `2025.01.01`。
3. **Room 迁移（MIGRATION_1_2）**：改为基于 `PRAGMA table_info` 的**动态补列**，
   只会补齐实体缺失的列（`time_hour` / `time_minute` / `amount` / `consistency` /
   `color` / `smell` / `pain_level` / `blood` / `mucus` / `notes`），不再写死旧 schema。
   旧版"加了 `count`/`timeOfDay` 但缺 `smell`/`pain_level`/`blood`/`mucus`"的崩溃已修复。
   前提：v1 表不存在这些 10 列之外的"多余列"，否则 Room 校验可能报错（属极端情况）。
4. ~~**后续页面迁移**：5 个业务页面直接复用 8 个玻璃组件即可。~~ **已完成**：
   所有页面残留的手写玻璃盒子（`Box + clip + background(白半透明) + border + drawBehind`）
   与原生 `Material3.Button` 已统一替换为 backdrop 组件，见第 8 节。

---

## 8. 页面统一收口（手写玻璃 → backdrop 组件）

第 1–5 轮只动了设计系统层；本轮把所有业务页面里**残留的手写玻璃盒子**和**原生按钮**
收口为 `LiquidGlass*` 组件，全 App 视觉统一。

| 页面 | 改动 |
|------|------|
| HomeScreen | 160dp 圆形"记录"按钮：手写 `shadow+clip+background+border+drawBehind` → `drawBackdrop` 圆形玻璃（blur+lens+highlight+内外阴影+主色表面）；成功弹窗"去生成海报"按钮 → `LiquidGlassButton` |
| PoopRecordForm | `GlassTimeCard` → `LiquidGlassCard(onClick)`；提交"确认记录" → `LiquidGlassButton` |
| CalendarScreen | `RecordDetailCard` 手动 Box → `LiquidGlassCard`；**删除确认弹窗保留红色 `Material3.Button`**（破坏性操作需警示语义） |
| AiScreen | "开始/重新分析"胶囊 → `LiquidGlassButton` |
| AiChatScreen | 发送圆形按钮 → `drawBackdrop` 玻璃圆钮；加载气泡、`Bot` 气泡 → `LiquidGlassSurface`（用户气泡保留主色） |
| AiConfigScreen | "保存配置" → `LiquidGlassButton` |
| AuthScreen | 登录/注册主按钮 → `LiquidGlassButton`（保留加载/错误逻辑） |
| SettingsScreen | 导出对话框 CSV/JSON、"保存海报"、"前往下载" → `LiquidGlassButton`；`LiquidGlassDividerThin` 调用替换为 `LiquidGlassDivider` 并删除冗余函数 |

**保留未动（仅系统级 chrome，非内容面）**：本节最初列出的"彩色 chip / 排名徽章 / 用户气泡 / 错误色按钮 / 主题色与深浅模式选项 / `SwitchButton` / 好友 Tab 选择器"等语义化元素，已在本轮（第 9 节）按用户"全局设计都要统一"的要求一并纳入玻璃系统，仅靠 `tint` + `glassContentColor` 保证对比度，原"保留不动"判断被推翻。真正保留不动的只有系统级 chrome（见第 9 节）。

**新增导入**（局部，确保编译通过）：
- `HomeScreen` / `AiChatScreen`：`LocalGlassBackdrop`、`com.kyant.backdrop.drawBackdrop` 及
  `effects.blur/lens/vibrancy`、`highlight.Highlight`、`shadow.Shadow/InnerShadow`、`ui.unit.DpOffset`。
- 各页按需补 `LiquidGlassButton` / `LiquidGlassSurface` / `LiquidGlassDivider`。

> 仍**未做本地编译验证**（环境无 Android Studio / Gradle 未下载）。请本地
> `./gradlew :app:assembleDebug` 同步确认；低版本设备（< API 31）自动退化为纯色半透明。

---

## 9. 全量玻璃化（全局统一 + 颜色对比度保证可读性）

用户明确推翻了第 8 节"保留语义化元素不动"的折中方案，要求 **"全局设计都要统一，可读性不好调整颜色对比度之类的就行了"**。
因此所有控件（含彩色 chip、排名徽章、用户气泡、错误色按钮、主题/深浅模式选项、`SwitchButton`、好友 Tab 选择器、表单症状复选框、导出对话框时间范围选择器等）
一律进入玻璃系统，可读性不靠"排除元素"解决，而靠 **着色（tint）+ 对比文字色** 解决。

### 9.1 设计系统扩展（`ui/theme/LiquidGlass.kt`）

为支持着色玻璃，给三个组件加了**向后兼容**的 `tint: Color? = null` 参数（默认 `null` = 白玻璃），
并新增对比色辅助函数：

```kotlin
// 根据着色色相返回对比文字色：高明度（金/银等）用深色字，其余用白色
fun glassContentColor(tint: Color): Color {
    val luminance = 0.2126f * tint.red + 0.7152f * tint.green + 0.0722f * tint.blue
    return if (luminance > 0.6f) Color(0xFF1A1A1A) else Color.White
}
```

着色表面 alpha（保留色相辨识度的同时仍"透"）：

| 组件 | 暗色 tint | 亮色 tint | 暗色 白玻璃 | 亮色 白玻璃 |
|------|-----------|-----------|-------------|-------------|
| `LiquidGlassCard` | 0.55 | 0.42 | 0.07 | 0.26 |
| `LiquidGlassButton` | 0.55 | 0.45 | — (用主色) | — |
| `LiquidGlassSurface` | 0.55 | 0.42 | 0.05 | 0.22 |

- 文字色：`tint == null` 时用主题色 / `onSurface`；`tint != null` 时一律 `glassContentColor(tint)`，保证可读。
- `LiquidGlassSurface` 额外暴露 `contentAlignment: Alignment`（默认 `TopStart`）与 `contentPadding: Dp`，
  便于把 `Box+clip+background+border` 的小控件平移进来。

### 9.2 本轮纳入玻璃系统的元素

| 页面 | 元素 | 改动 |
|------|------|------|
| FriendsScreen | 好友 / 请求 / 排行 Tab 选择器 | `Box+clip+background+border` → `LiquidGlassSurface(tint=选中时主色)`，文字 `glassContentColor`；移除死变量 `isDark` |
| PoopRecordForm | "有血" / "有黏液" 复选框 | `Box+clip+background+border` → `LiquidGlassSurface(tint=选中时 error)`，对勾 `glassContentColor(error)`；移除死变量 `checkboxShape` |
| SettingsScreen | `SwitchButton` 轨道 | 原 `bgColor`/`borderColor` 死局部变量删除，轨道纯用 `LiquidGlassSurface(tint=checked?主色:null)` |
| SettingsScreen | 主题模式选项 / 配色预设选项 | `ThemeModeOption` / `ColorPresetOption` 整体包 `LiquidGlassSurface(tint=选中时主色/预设色)`，文字 `glassContentColor` |
| SettingsScreen | 导出对话框时间范围选择器 | `FlowRow` 内 `Box+clip+background+border` → `LiquidGlassSurface(tint=选中时主色)`，文字 `glassContentColor` |
| SettingsScreen | 删除确认弹窗 | "删除"按钮 `Material3.Button(colors=error)` → `LiquidGlassButton(tint=error)`（红色语义靠 tint 保留，文字自动白） |
| FriendsScreen | 排名徽章 / "我" 标记 | `Box+background` → `LiquidGlassSurface(tint=前三名 rankColor / 主色)`，文字 `glassContentColor` |
| PoopRecordForm | `ChoiceChip` / `ColorChip` | 选中态 `LiquidGlassSurface(tint=主色)`，文字 `glassContentColor`；色块 `Box` 保留作数据 |
| AiChatScreen | 用户气泡 | `Box+background(主色0.85)` → `LiquidGlassSurface(tint=主色)`，文字 `glassContentColor` |
| AiConfigScreen | 服务商下拉框 | `Box+clip+background+border` → `LiquidGlassSurface` |

### 9.3 仍保留不动的元素（系统 chrome / 数据本身，非内容面）

- 底部抽屉拖拽条、顶栏设置图标、`OutlinedTextField` 容器色、抽屉遮罩 —— 系统级 chrome。
- 颜色**色板环**（`ColorPresetOption` / `ColorChip` 的描边环）、日历"今天"环、图例色点 —— 展示的是**数据色本身**，套玻璃会掩盖信息，保留实心/描边。
- 导出对话框"生成海报"预览框 —— 预览的是实际海报内容，非 UI 控制面。
- `RadioButtonDefaults` —— 标准选择控件，非玻璃候选。
- 加载指示器（`CircularProgressIndicator` / 点状 loading）—— 动画 chrome，非内容面。

### 9.4 校验结论（源码级，未编译）

- `ui/` 下已无 `material3.Button` 残留。
- `tint =` 着色用法共 53 处，分布于 11 个文件。
- `LiquidGlassSurface` / `glassContentColor` 的调用点均已补导入。
- 死变量（`rankBg` / `bgColor` / `borderColor` / `checkboxShape` / `switchBg` / `switchBorder` / `rangeBg/Border/Text`）已全部清除。
- **仍须用户在本地执行 `./gradlew :app:assembleDebug` 做最终编译验证**（本环境无 Android Studio / Gradle 未下载）。
