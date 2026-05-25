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

    // Coroutines - align with Kotlin compiler 2.0.x used by JitPack
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.8.1")

    // LibYUV-Android for YUV<->ARGB conversions
    implementation("io.github.crow-misia.libyuv:libyuv-android:0.43.2")
}
