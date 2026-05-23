package com.codewithkael.aivideoprocessor.effect.blur

import android.graphics.Bitmap
import android.renderscript.RenderScript
import com.codewithkael.aivideoprocessor.config.blur.BlurBackgroundConfig
import com.codewithkael.aivideoprocessor.ml.SegmentationEngine
import com.codewithkael.aivideoprocessor.ml.SegmentationMask
import com.codewithkael.aivideoprocessor.effect.VideoEffect

class BlurBackgroundEffect(
    private val config: BlurBackgroundConfig,
    private val segmentationEngine: SegmentationEngine
) : VideoEffect {

    override suspend fun apply(input: Bitmap): Bitmap {
        if (!config.enabled) return input

        val mask: SegmentationMask = segmentationEngine.segment(input)
        val blurred = applySimpleBoxBlur(input, config.blurRadius)

        val width = input.width
        val height = input.height
        val output = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

        val originalPixels = IntArray(width * height)
        val blurredPixels = IntArray(width * height)

        input.getPixels(originalPixels, 0, width, 0, 0, width, height)
        blurred.getPixels(blurredPixels, 0, width, 0, 0, width, height)

        val threshold = config.minPersonConfidence

        for (y in 0 until height) {
            for (x in 0 until width) {
                val index = y * width + x
                val useOriginal = mask.isForeground(x, y, threshold)
                output.setPixel(index % width, index / width,
                    if (useOriginal) originalPixels[index] else blurredPixels[index]
                )
            }
        }

        return output
    }

    private fun applySimpleBoxBlur(src: Bitmap, radius: Float): Bitmap {
        // Placeholder blur implementation (box blur or fast approximation)
        // For now, we can just return the original until a proper blur is wired in.
        return src
    }
}
