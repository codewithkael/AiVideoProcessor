package com.codewithkael.aivideoprocessor.effect

import android.content.Context
import android.graphics.Bitmap
import com.codewithkael.aivideoprocessor.config.blur.BlurBackgroundConfig
import com.codewithkael.aivideoprocessor.config.replace.ReplaceBackgroundConfig
import com.codewithkael.aivideoprocessor.config.watermark.WatermarkConfig
import com.codewithkael.aivideoprocessor.effect.blur.BackgroundBlurEffect
import com.codewithkael.aivideoprocessor.effect.replace.BackgroundReplaceEffect
import com.codewithkael.aivideoprocessor.effect.watermark.WatermarkDrawer
import com.codewithkael.aivideoprocessor.ml.SegmentationEngine

interface VideoEffect {
    suspend fun apply(input: Bitmap): Bitmap
}

class BlurBackgroundEffect(
    private val context: Context,
    private val config: BlurBackgroundConfig,
    private val segmentationEngine: SegmentationEngine
) : VideoEffect {
    override suspend fun apply(input: Bitmap): Bitmap {
        if (!config.enabled) return input
        val engineEffect = BackgroundBlurEffect(context, segmentationEngine, config)
        return engineEffect.apply(input)
    }
}

class ReplaceBackgroundEffect(
    private val config: ReplaceBackgroundConfig,
    private val segmentationEngine: SegmentationEngine
) : VideoEffect {
    override suspend fun apply(input: Bitmap): Bitmap {
        if (!config.enabled) return input
        val engineEffect = BackgroundReplaceEffect(segmentationEngine, config)
        return engineEffect.apply(input)
    }
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
