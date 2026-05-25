package com.codewithkael.aivideoprocessor.effect.replace

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import com.codewithkael.aivideoprocessor.config.replace.BackgroundScaleMode
import com.codewithkael.aivideoprocessor.config.replace.ReplaceBackgroundConfig
import com.codewithkael.aivideoprocessor.ml.SegmentationEngine
import com.codewithkael.aivideoprocessor.ml.SegmentationMask
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class BackgroundReplaceEffect(
    private val segmentationEngine: SegmentationEngine,
    private val config: ReplaceBackgroundConfig
) {
    suspend fun apply(input: Bitmap): Bitmap = suspendCancellableCoroutine { cont ->
        if (config.backgroundBitmap == null) {
            cont.resume(input)
            return@suspendCancellableCoroutine
        }

        val background = config.backgroundBitmap!!

        segmentationEngine.segmentAsync(input) { mask: SegmentationMask? ->
            if (mask == null) {
                cont.resume(input)
                return@segmentAsync
            }

            val width = input.width
            val height = input.height

            val bgPrepared = when (config.scaleMode) {
                BackgroundScaleMode.CENTER_CROP -> centerCrop(background, width, height)
                BackgroundScaleMode.STRETCH -> Bitmap.createScaledBitmap(background, width, height, true)
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
            cont.resume(output)
        }
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
