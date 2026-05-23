package com.codewithkael.aivideoprocessor.config

import android.graphics.Bitmap
import android.graphics.Color

enum class BackgroundScaleType {
    FIT,
    FILL,
    CROP
}

enum class WatermarkPosition {
    TOP_LEFT,
    TOP_RIGHT,
    BOTTOM_LEFT,
    BOTTOM_RIGHT,
    CENTER
}

data class BlurBackgroundConfig(
    val enabled: Boolean = false,
    val blurRadius: Float = 18f,
    val maxBlurFps: Int = 30,
    val minPersonConfidence: Float = 0.5f
)

data class ReplaceBackgroundConfig(
    val enabled: Boolean = false,
    val backgroundBitmap: Bitmap? = null,
    val backgroundColor: Int? = null,
    val scaleType: BackgroundScaleType = BackgroundScaleType.CROP,
    val minPersonConfidence: Float = 0.5f,
    val featherRadius: Float = 4f
)

data class WatermarkConfig(
    val enabled: Boolean = false,
    val bitmap: Bitmap? = null,
    val bitmapScale: Float = 0.15f,
    val text: String? = null,
    val textSizeSp: Float = 14f,
    val textColor: Int = Color.WHITE,
    val textShadowColor: Int = Color.BLACK,
    val textShadowRadius: Float = 3f,
    val position: WatermarkPosition = WatermarkPosition.BOTTOM_RIGHT,
    val marginDp: Float = 8f,
    val alpha: Float = 0.8f
)

data class AiVideoProcessorConfig(
    val blurBackground: BlurBackgroundConfig = BlurBackgroundConfig(),
    val replaceBackground: ReplaceBackgroundConfig = ReplaceBackgroundConfig(),
    val watermark: WatermarkConfig = WatermarkConfig(),
    val mirrorFrontCamera: Boolean = true,
    val maxProcessingFps: Int = 30
)
