package com.codewithkael.aivideoprocessor.effect

import android.graphics.Bitmap
import com.codewithkael.aivideoprocessor.config.BlurBackgroundConfig
import com.codewithkael.aivideoprocessor.config.ReplaceBackgroundConfig
import com.codewithkael.aivideoprocessor.config.WatermarkConfig
import com.codewithkael.aivideoprocessor.ml.SegmentationEngine

interface VideoEffect {
    suspend fun apply(input: Bitmap): Bitmap
}

class BlurBackgroundEffect(
    private val config: BlurBackgroundConfig,
    private val segmentationEngine: SegmentationEngine
) : VideoEffect {
    override suspend fun apply(input: Bitmap): Bitmap {
        // TODO: Implement blur background using segmentationEngine and config
        return input
    }
}

class ReplaceBackgroundEffect(
    private val config: ReplaceBackgroundConfig,
    private val segmentationEngine: SegmentationEngine
) : VideoEffect {
    override suspend fun apply(input: Bitmap): Bitmap {
        // TODO: Implement background replacement using segmentationEngine and config
        return input
    }
}

interface WatermarkDrawer {
    fun drawWatermark(input: Bitmap, config: WatermarkConfig): Bitmap
}

class WatermarkEffect(
    private val config: WatermarkConfig,
    private val watermarkDrawer: WatermarkDrawer
) : VideoEffect {
    override suspend fun apply(input: Bitmap): Bitmap {
        if (!config.enabled) return input
        return watermarkDrawer.drawWatermark(input, config)
    }
}
