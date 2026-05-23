package com.codewithkael.aivideoprocessor.config

import com.codewithkael.aivideoprocessor.config.blur.BlurBackgroundConfig
import com.codewithkael.aivideoprocessor.config.replace.ReplaceBackgroundConfig
import com.codewithkael.aivideoprocessor.config.watermark.WatermarkConfig

/**
 * Top-level configuration that bundles all feature configs.
 */
data class AiVideoProcessorConfig(
    val blurBackground: BlurBackgroundConfig = BlurBackgroundConfig(),
    val replaceBackground: ReplaceBackgroundConfig = ReplaceBackgroundConfig(),
    val watermark: WatermarkConfig = WatermarkConfig(),
    val mirrorFrontCamera: Boolean = true,
    val maxProcessingFps: Int = 30
)
