# AI Video Processor SDK: Universal Android Video Effects 🚀

The **AI Video Processor SDK** is a high-performance video augmentation library for Android. It allows you to add professional AI effects to your video streams with just a few lines of code.

---

## 🌟 Key Features

*   **Universal Support**: Works with **WebRTC**, **CameraX**, **Camera2**, and custom video pipelines.
*   **Real-Time AI Effects**: 
    *   **Background Blur**: High-performance bokeh effects.
    *   **Virtual Backgrounds**: Replace backgrounds with images or colors.
    *   **Smart Watermarking**: Precise logo and text overlays.
*   **Zero-Lag Performance**: Native `libyuv` C++ engine ensures maximum FPS and low battery drain.
*   **Plug-and-Play**: Optimized default settings so you can start immediately.

---

## 📦 Installation

Add JitPack to your `settings.gradle` or root `build.gradle`:

```kotlin
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }
}
```

Add the dependency to your app's `build.gradle`:

```kotlin
dependencies {
    implementation("com.github.MasoudSarabadani:AiVideoProcessor:latest-tag")
}
```

---

## 🛠 Integration Guide

### 1. WebRTC Integration (Simplest)
If you are using WebRTC, use the `AiVideoCapturerObserver`. It handles all the complex frame conversions and AI processing for you automatically.

```kotlin
// 1. Define your effects
val config = AiVideoProcessorConfig(
    blurBackground = BlurBackgroundConfig(enabled = true, blurRadius = 15f)
)

// 2. Wrap your observer
val aiObserver = AiVideoCapturerObserver(
    context = applicationContext,
    downstream = myOriginalObserver, // The observer you already have
    config = config
)

// 3. Use it in your video capturer
videoCapturer.initialize(surfaceTextureHelper, context, aiObserver)
```

### 2. CameraX / Custom Camera Integration
For non-WebRTC features, use the `FrameProcessor`. 

> [!NOTE]
> **What is `helpers`?** 
> The `helpers` object provides the library with access to the AI engine (ML Kit) and drawing tools. You can create it with a single line using the built-in factory.

```kotlin
// 1. Create the default engine helpers (Only needed once)
val helpers = FrameProcessingHelpers.default(applicationContext)

// 2. Initialize the processor
val processor = FrameProcessor.fromConfig(config, helpers)

// 3. In your analysis loop (e.g., CameraX ImageAnalysis)
val processedBitmap = processor.process(inputBitmap)
```

### 3. Updating Filters in Real-Time
You can change settings (like turning on a watermark or changing the blur radius) while the camera is running.

```kotlin
val newConfig = currentConfig.copy(
    watermark = WatermarkConfig(enabled = true, text = "Property of Company")
)

// The change is applied instantly to the video stream
aiObserver.updateConfig(newConfig) 
```

---

## ⚙️ Configuration Reference

### `AiVideoProcessorConfig`
This is the main object you use to turn features on or off.

| Property | Description |
| :--- | :--- |
| `blurBackground` | Control the bokeh/blur intensity. |
| `replaceBackground` | Provide an image to use as a virtual background. |
| `watermark` | Add a logo or text to the corner of the video. |

---

## 📄 License
This project is licensed under the MIT License - see the LICENSE file for details.
