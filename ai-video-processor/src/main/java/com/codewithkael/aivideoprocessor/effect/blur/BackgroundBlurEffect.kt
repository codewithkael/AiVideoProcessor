package com.codewithkael.aivideoprocessor.effect.blur

import android.content.Context
import android.graphics.Bitmap
import com.codewithkael.aivideoprocessor.config.blur.BlurBackgroundConfig
import com.codewithkael.aivideoprocessor.ml.SegmentationEngine
import com.codewithkael.aivideoprocessor.ml.SegmentationMask
import com.hoko.blur.HokoBlur

class BackgroundBlurEffect(
    private val context: Context,
    private val segmentationEngine: SegmentationEngine,
    private val config: BlurBackgroundConfig
) {
    suspend fun apply(input: Bitmap): Bitmap {
        val mask: SegmentationMask = segmentationEngine.segment(input)

        val width = input.width
        val height = input.height

        val blurred = HokoBlur.with(context)
            .scheme(HokoBlur.SCHEME_NATIVE)
            .mode(HokoBlur.MODE_BOX)
            .radius((width / 32).coerceAtLeast(1))
            .sampleFactor(2f)
            .forceCopy(true)
            .blur(input)

        val output = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

        val total = width * height
        val finalPixels = IntArray(total)

        val origPix = IntArray(total)
        val blurPix = IntArray(total)

        input.getPixels(origPix, 0, width, 0, 0, width, height)
        blurred.getPixels(blurPix, 0, width, 0, 0, width, height)

        val threshold = config.minPersonConfidence

        for (y in 0 until height) {
            for (x in 0 until width) {
                val index = y * width + x
                val isForeground = mask.isForeground(x, y, threshold)
                finalPixels[index] = if (isForeground) origPix[index] else blurPix[index]
            }
        }

        output.setPixels(finalPixels, 0, width, 0, 0, width, height)
        return output
    }
}
