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
    initialEffects: List<VideoEffect>
) {
    @Volatile
    private var effects: List<VideoEffect> = initialEffects

    suspend fun process(input: Bitmap): Bitmap {
        var current = input
        val currentEffects = effects // Local reference for thread safety
        for (effect in currentEffects) {
            current = effect.apply(current)
        }
        return current
    }

    fun updateConfig(
        config: AiVideoProcessorConfig,
        helpers: FrameProcessingHelpers
    ) {
        effects = createEffectsFromConfig(config, helpers)
    }

    companion object {
        fun fromConfig(
            config: AiVideoProcessorConfig,
            helpers: FrameProcessingHelpers
        ): FrameProcessor {
            return FrameProcessor(createEffectsFromConfig(config, helpers))
        }

        private fun createEffectsFromConfig(
            config: AiVideoProcessorConfig,
            helpers: FrameProcessingHelpers
        ): List<VideoEffect> = buildList {
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
    }
}

data class FrameProcessingHelpers(
    val appContext: Context,
    val segmentationEngineFactory: () -> SegmentationEngine,
    val watermarkDrawer: WatermarkDrawer
) {
    companion object {
        /**
         * Creates a default implementation of helpers using ML Kit and standard drawers.
         * Most users should use this factory method.
         */
        fun default(context: Context): FrameProcessingHelpers {
            return defaultFrameProcessingHelpers(context)
        }
    }
}
