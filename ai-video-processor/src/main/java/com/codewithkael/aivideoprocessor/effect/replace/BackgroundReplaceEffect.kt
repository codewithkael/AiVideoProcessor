package com.codewithkael.aivideoprocessor.effect.replace

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import com.codewithkael.aivideoprocessor.config.replace.ReplaceBackgroundConfig
import com.codewithkael.aivideoprocessor.config.shared.BackgroundScaleType
import com.codewithkael.aivideoprocessor.ml.SegmentationEngine
import com.codewithkael.aivideoprocessor.ml.SegmentationMask

class BackgroundReplaceEffect(
    private val segmentationEngine: SegmentationEngine,
    private val config: ReplaceBackgroundConfig
) {
    suspend fun apply(input: Bitmap): Bitmap {
        if (config.backgroundBitmap == null) {
            return input
        }

        val background = config.backgroundBitmap!!
        val mask: SegmentationMask = segmentationEngine.segment(input)

        val width = input.width
        val height = input.height

        val bgPrepared = when (config.scaleType) {
            BackgroundScaleType.CROP -> centerCrop(background, width, height)
            BackgroundScaleType.FIT, BackgroundScaleType.FILL ->
                Bitmap.createScaledBitmap(background, width, height, true)
        }

        val output = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

        val total = width * height
        val finalPixels = IntArray(total)

        val origPix = IntArray(total)
        val bgPix = IntArray(total)

        input.getPixels(origPix, 0, width, 0, 0, width, height)
        bgPrepared.getPixels(bgPix, 0, width, 0, 0, width, height)

        val threshold = config.minPersonConfidence

        for (y in 0 until height) {
            for (x in 0 until width) {
                val index = y * width + x
                val isForeground = mask.isForeground(x, y, threshold)
                finalPixels[index] = if (isForeground) origPix[index] else bgPix[index]
            }
        }

        output.setPixels(finalPixels, 0, width, 0, 0, width, height)
        return output
    }

    private fun centerCrop(source: Bitmap, targetWidth: Int, targetHeight: Int): Bitmap {
        val result = Bitmap.createBitmap(targetWidth, targetHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(result)

        val scale = maxOf(
            targetWidth.toFloat() / source.width,
            targetHeight.toFloat() / source.height
        )

        val scaledWidth = scale * source.width
        val scaledHeight = scale * source.height

        val left = (targetWidth - scaledWidth) / 2f
        val top = (targetHeight - scaledHeight) / 2f

        val matrix = Matrix().apply {
            postScale(scale, scale)
            postTranslate(left, top)
        }

        canvas.drawBitmap(source, matrix, null)
        return result
    }
}
