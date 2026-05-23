package com.codewithkael.aivideoprocessor.config.blur

data class BlurBackgroundConfig(
    val enabled: Boolean = false,
    val blurRadius: Float = 18f,
    val maxBlurFps: Int = 30,
    val minPersonConfidence: Float = 0.5f
)
