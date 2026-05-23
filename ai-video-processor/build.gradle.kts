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
}

dependencies {
    // TODO: replace with the same versions you use in WebRTCwithMLKit
    implementation("org.webrtc:google-webrtc:1.0.32006")
    implementation("com.google.mlkit:segmentation-selfie:16.0.0-beta3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.1")

    implementation(files("libs/core-0.0.7.aar"))
}
