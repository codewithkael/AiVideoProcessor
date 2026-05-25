package com.codewithkael.aivideoprocessor.frame

import android.content.Context
import com.codewithkael.aivideoprocessor.effect.watermark.SimpleWatermarkDrawer
import com.codewithkael.aivideoprocessor.ml.MlKitSegmentationEngine

/**
 * Convenience factory for building [FrameProcessingHelpers] with the default
 * ML Kit-based SegmentationEngine and a simple watermark drawer.
 */
fun defaultFrameProcessingHelpers(
    context: Context
): FrameProcessingHelpers {
    return FrameProcessingHelpers(
        appContext = context,
        segmentationEngineFactory = { MlKitSegmentationEngine(context) },
        watermarkDrawer = SimpleWatermarkDrawer()
    )
}
