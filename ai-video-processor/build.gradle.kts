plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.codewithkael.aivideoprocessor"
    compileSdk = 35

    defaultConfig {
        minSdk = 23
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {
    // WebRTC - Mesibo
    implementation("com.mesibo.api:webrtc:1.0.5")

    // ML Kit segmentation
    implementation("com.google.mlkit:segmentation-selfie:16.0.0-beta3")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.1")

    // LibYUV from Maven Central (replaces local core-0.0.7.aar)
    implementation("io.github.zncmn:libyuv:0.0.7")
}
