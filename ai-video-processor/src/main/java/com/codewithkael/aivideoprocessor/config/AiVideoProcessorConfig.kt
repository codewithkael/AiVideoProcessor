package com.codewithkael.aivideoprocessor.config

import com.codewithkael.aivideoprocessor.config.blur.BlurBackgroundConfig
import com.codewithkael.aivideoprocessor.config.replace.ReplaceBackgroundConfig
import com.codewithkael.aivideoprocessor.config.shared.WatermarkPosition
import com.codewithkael.aivideoprocessor.config.watermark.WatermarkConfig

// Shared enums
package com.codewithkael.aivideoprocessor.config.shared

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

// Top-level config
package com.codewithkael.aivideoprocessor.config

data class AiVideoProcessorConfig(
    val blurBackground: BlurBackgroundConfig = BlurBackgroundConfig(),
    val replaceBackground: ReplaceBackgroundConfig = ReplaceBackgroundConfig(),
    val watermark: WatermarkConfig = WatermarkConfig(),
    val mirrorFrontCamera: Boolean = true,
    val maxProcessingFps: Int = 30
)
