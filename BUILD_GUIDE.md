# 拉了吗 - 构建指南

## 前提条件

1. **Android Studio** (推荐 Hedgehog 或更新版本)
2. **JDK 17** (Android Studio 自带或单独安装)
3. **Android SDK** (通过 Android Studio 自动下载)

## 在本地构建

### 方法 1: 使用 Android Studio (推荐)

1. 打开 Android Studio
2. 选择 "Open an existing project"
3. 选择 `/workspace/lalema` 文件夹
4. 等待 Gradle Sync 完成（会自动下载依赖）
5. 点击菜单 "Build" > "Build Bundle(s) / APK(s)" > "Build APK(s)"
6. APK 文件将生成在 `app/build/outputs/apk/debug/` 目录

### 方法 2: 使用命令行

```bash
cd /path/to/lalema

# macOS/Linux
./gradlew assembleDebug

# Windows
gradlew.bat assembleDebug

# 或者使用系统 Gradle (需要 Gradle 8.x)
gradle assembleDebug
```

## APK 输出位置

```
app/build/outputs/apk/debug/app-debug.apk
```

## 安装到手机

```bash
# 通过 adb 安装
adb install app/build/outputs/apk/debug/app-debug.apk

# 或者直接拖拽 APK 到模拟器/手机
```

## 构建变体

- **Debug**: `./gradlew assembleDebug` → `app-debug.apk` (可调试)
- **Release**: `./gradlew assembleRelease` → `app-release-unsigned.apk` (需要签名)

## 签名 APK (Release)

```bash
# 生成签名密钥
keytool -genkey -v -keystore my-release-key.jks -keyalg RSA -keysize 2048 -validity 10000 -alias my-key-alias

# 在 app/build.gradle.kts 中配置签名
# 或者使用 Android Studio: Build > Generate Signed Bundle / APK
```

## 常见问题

### 依赖下载失败
- 检查网络连接
- 配置国内镜像: 在 `build.gradle.kts` 的 `repositories` 中添加:
  ```
  maven { url = uri("https://maven.aliyun.com/repository/public") }
  ```

### Kotlin 版本不兼容
- 确保 Kotlin 版本与 Compose Compiler 版本匹配
- 当前配置: Kotlin 1.9.22, Compose Compiler 1.5.8

### SDK 版本问题
- 确保 compileSdk = 34
- 确保 minSdk = 26 (Android 8.0)
