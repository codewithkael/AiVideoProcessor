package com.codewithkael.aivideoprocessor.frame

import android.graphics.Bitmap
import com.codewithkael.aivideoprocessor.config.AiVideoProcessorConfig
import com.codewithkael.aivideoprocessor.config.BlurBackgroundConfig
import com.codewithkael.aivideoprocessor.config.ReplaceBackgroundConfig
import com.codewithkael.aivideoprocessor.config.WatermarkConfig
import com.codewithkael.aivideoprocessor.effect.VideoEffect
import com.codewithkael.aivideoprocessor.effect.BlurBackgroundEffect
import com.codewithkael.aivideoprocessor.effect.ReplaceBackgroundEffect
import com.codewithkael.aivideoprocessor.effect.WatermarkEffect
import com.codewithkael.aivideoprocessor.ml.SegmentationEngine

class FrameProcessor(
    private val config: AiVideoProcessorConfig,
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
                            config.blurBackground,
                            helpers.segmentationEngineFactory()
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
            return FrameProcessor(config, effects)
        }
    }
}

data class FrameProcessingHelpers(
    val segmentationEngineFactory: () -> SegmentationEngine,
    val watermarkDrawer: com.codewithkael.aivideoprocessor.effect.WatermarkDrawer
)

interface FrameConverters {
    fun videoFrameToBitmap(frame: org.webrtc.VideoFrame): Bitmap?
    fun bitmapToVideoFrame(bitmap: Bitmap, originalFrame: org.webrtc.VideoFrame): org.webrtc.VideoFrame
}
