plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "app.myvision"
    compileSdk = 35

    defaultConfig {
        applicationId = "app.myvision"
        minSdk = 28
        targetSdk = 35
        versionCode = 2
        versionName = "1.0.0"
    }

    // release 서명: keystore와 비밀번호는 저장소 밖에 둔다(공개 저장소).
    // 값은 Gradle 사용자 설정(GRADLE_USER_HOME/gradle.properties)이나 환경변수 ORG_GRADLE_PROJECT_MYVISION_*에서 읽는다.
    // 값이 없으면(CI, 새 클론) 서명 설정 없이 빌드된다(assembleRelease는 unsigned APK)
    val keystore = providers.gradleProperty("MYVISION_KEYSTORE").orNull
    signingConfigs {
        if (keystore != null) create("release") {
            storeFile = file(keystore)
            storePassword = providers.gradleProperty("MYVISION_KEYSTORE_PASSWORD").get()
            keyAlias = providers.gradleProperty("MYVISION_KEY_ALIAS").get()
            keyPassword = storePassword // PKCS12는 저장소와 키 비밀번호가 같다
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false // 기기 검증 전이라 R8을 켜지 않는다
            signingConfig = signingConfigs.findByName("release")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }
    buildFeatures { compose = true }
}

dependencies {
    // Pleos SDK: 공모전 제공 SDK(라이선스 예외, AGENTS.md)
    implementation(libs.pleos.vehicle)
    implementation(libs.pleos.navihelper)
    implementation(libs.pleos.tts)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    debugImplementation(libs.androidx.ui.tooling)

    testImplementation(libs.junit)
}
