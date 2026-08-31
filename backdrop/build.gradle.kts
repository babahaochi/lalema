plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "com.kyant.backdrop"
    compileSdk = 36

    defaultConfig {
        minSdk = 21
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
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
    val composeBom = platform("androidx.compose:compose-bom:2024.12.01")
    implementation(composeBom)

    implementation("androidx.annotation:annotation:1.8.0")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-util")

    // backdrop 的 SDF 圆角形状，lens 折射效果依赖它
    implementation("io.github.kyant0:shapes:1.2.1")

    // @Language("AGSL") 注解，仅编译期需要
    compileOnly("org.jetbrains:annotations:26.1.0")
}
