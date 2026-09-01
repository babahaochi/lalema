plugins {
    // 升级到 AGP 9.3.2：kyant/backdrop 2.0.1 源码要求 AGP 9.x（Compose 1.12.0 要求 AGP 9.1+）
    id("com.android.application") version "9.3.2" apply false
    id("com.android.library") version "9.3.2" apply false
    // Kotlin 2.3.21（AGP 9 已内置，无需单独 apply；此处仅声明版本供子模块继承）：
    // backdrop 2.0.1 的 LayerRecorder.kt 使用 context 参数语法（2.3 已稳定支持）。
    // 不能用 2.4.x：KSP 最新仅到 2.3.11（无 2.4.x KSP），而 Room/Hilt 需要 KSP。
    id("org.jetbrains.kotlin.plugin.compose") version "2.3.21" apply false
    // Hilt 2.60.1：2.54+ 支持 AGP 9 的扩展 API（旧版 apply 时报 Android BaseExtension not found）
    id("com.google.dagger.hilt.android") version "2.60.1" apply false
    // KSP 2.3.11：与 Kotlin 2.3.x 匹配（KSP 最新发布版本）
    id("com.google.devtools.ksp") version "2.3.11" apply false
}
