package com.codewithkael.aivideoprocessor.frame

import android.content.Context
import android.graphics.Bitmap
import com.codewithkael.aivideoprocessor.config.AiVideoProcessorConfig
import com.codewithkael.aivideoprocessor.effect.BlurBackgroundEffect
import com.codewithkael.aivideoprocessor.effect.ReplaceBackgroundEffect
import com.codewithkael.aivideoprocessor.effect.VideoEffect
import com.codewithkael.aivideoprocessor.effect.WatermarkEffect
import com.codewithkael.aivideoprocessor.effect.watermark.WatermarkDrawer
import com.codewithkael.aivideoprocessor.ml.SegmentationEngine

class FrameProcessor(
    private val effects: List<VideoEffect>
) {
    suspend fun process(input: Bitmap): Bitmap {
        var current = input
        for (effect in effects) {
            current = effect.apply(current)
        }
        return current
    }

    companion object {
        fun fromConfig(
            config: AiVideoProcessorConfig,
            helpers: FrameProcessingHelpers
        ): FrameProcessor {
            val effects = buildList {
                if (config.blurBackground.enabled) {
                    add(
                        BlurBackgroundEffect(
                            helpers.appContext,
                            config.blurBackground,
                            helpers.segmentationEngineFactory(),
                        )
                    )
                }
                if (config.replaceBackground.enabled) {
                    add(
                        ReplaceBackgroundEffect(
                            config.replaceBackground,
                            helpers.segmentationEngineFactory()
                        )
                    )
                }
                if (config.watermark.enabled) {
                    add(
                        WatermarkEffect(
                            config.watermark,
                            helpers.watermarkDrawer
                        )
                    )
                }
            }
            return FrameProcessor(effects)
        }
    }
}

data class FrameProcessingHelpers(
    val appContext: Context,
    val segmentationEngineFactory: () -> SegmentationEngine,
    val watermarkDrawer: WatermarkDrawer
)
