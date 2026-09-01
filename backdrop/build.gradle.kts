plugins {
    // AGP 9.0+ 已内置 Kotlin 支持，无需再 apply org.jetbrains.kotlin.android
    id("com.android.library")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "com.kyant.backdrop"
    compileSdk = 37

    defaultConfig {
        minSdk = 21
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    // AGP 9 内置 Kotlin：用 kotlin {} 配置 JVM 工具链（取代旧的 kotlinOptions.jvmTarget）
    kotlin {
        jvmToolchain(17)
        // backdrop 源码使用 context 参数（context(node: DelegatableNode)），Kotlin 2.3 仍为实验特性，需显式开启
        compilerOptions {
            freeCompilerArgs.add("-Xcontext-parameters")
        }
    }

    buildFeatures {
        compose = true
    }

    lint {
        // blur 依赖 Android S (API 31) 的 RenderEffect，lens 依赖 TIRAMISU (API 33)
        // 的 RuntimeShader。库内已用 isRenderEffectSupported() / isRuntimeShaderSupported()
        // 做运行时降级：低版本设备仅失去模糊与折射，不会崩溃。
        disable += "NewApi"
    }
}

dependencies {
    // enforcedPlatform：强制把 Compose 锁定在 BOM 2026.08.00（= Compose 1.12.0），
    // 与 kyant/backdrop 2.0.1 + shapes 1.2.1 的要求一致。
    val composeBom = enforcedPlatform("androidx.compose:compose-bom:2026.08.00")
    implementation(composeBom)

    implementation("androidx.annotation:annotation:1.9.0")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-util")

    // backdrop 的 SDF 圆角形状，lens 折射效果依赖它（Compose 1.12.0 兼容）
    implementation("io.github.kyant0:shapes:1.2.1")

    // @Language("AGSL") 由 kotlin-stdlib 传递的 org.jetbrains:annotations 提供，
    // 不再显式 pin 版本，交由 Gradle 与 Compose 1.12 依赖树一致解析，避免版本冲突。
}
