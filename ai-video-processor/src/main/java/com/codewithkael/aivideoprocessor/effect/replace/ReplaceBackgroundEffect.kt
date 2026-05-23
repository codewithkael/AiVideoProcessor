package com.codewithkael.aivideoprocessor.effect.replace

import android.graphics.Bitmap
import com.codewithkael.aivideoprocessor.config.replace.ReplaceBackgroundConfig
import com.codewithkael.aivideoprocessor.config.shared.BackgroundScaleType
import com.codewithkael.aivideoprocessor.ml.SegmentationEngine
import com.codewithkael.aivideoprocessor.ml.SegmentationMask
import com.codewithkael.aivideoprocessor.effect.VideoEffect

class ReplaceBackgroundEffect(
    private val config: ReplaceBackgroundConfig,
    private val segmentationEngine: SegmentationEngine
) : VideoEffect {

    override suspend fun apply(input: Bitmap): Bitmap {
        if (!config.enabled) return input

        val mask: SegmentationMask = segmentationEngine.segment(input)
        val width = input.width
        val height = input.height

        val output = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val originalPixels = IntArray(width * height)
        input.getPixels(originalPixels, 0, width, 0, 0, width, height)

        val backgroundPixels = IntArray(width * height)
        prepareBackgroundPixels(width, height, backgroundPixels)

        val threshold = config.minPersonConfidence

        for (y in 0 until height) {
            for (x in 0 until width) {
                val index = y * width + x
                val useOriginal = mask.isForeground(x, y, threshold)
                output.setPixel(
                    x,
                    y,
                    if (useOriginal) originalPixels[index] else backgroundPixels[index]
                )
            }
        }

        return output
    }

    private fun prepareBackgroundPixels(width: Int, height: Int, out: IntArray) {
        val bgBitmap = config.backgroundBitmap
        if (bgBitmap != null) {
            val scaled = when (config.scaleType) {
                BackgroundScaleType.FIT -> scaleFit(bgBitmap, width, height)
                BackgroundScaleType.FILL, BackgroundScaleType.CROP -> scaleFill(bgBitmap, width, height)
            }
            scaled.getPixels(out, 0, width, 0, 0, width, height)
        } else {
            val color = config.backgroundColor ?: 0xFF000000.toInt()
            out.fill(color)
        }
    }

    private fun scaleFit(src: Bitmap, width: Int, height: Int): Bitmap {
        val srcRatio = src.width.toFloat() / src.height.toFloat()
        val dstRatio = width.toFloat() / height.toFloat()

        val targetWidth: Int
        val targetHeight: Int

        if (dstRatio > srcRatio) {
            targetHeight = height
            targetWidth = (height * srcRatio).toInt()
        } else {
            targetWidth = width
            targetHeight = (width / srcRatio).toInt()
        }

        val scaled = Bitmap.createScaledBitmap(src, targetWidth, targetHeight, true)
        val output = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = android.graphics.Canvas(output)
        val left = (width - scaled.width) / 2f
        val top = (height - scaled.height) / 2f
        canvas.drawBitmap(scaled, left, top, null)
        return output
    }

    private fun scaleFill(src: Bitmap, width: Int, height: Int): Bitmap {
        val srcRatio = src.width.toFloat() / src.height.toFloat()
        val dstRatio = width.toFloat() / height.toFloat()

        val targetWidth: Int
        val targetHeight: Int

        if (dstRatio > srcRatio) {
            targetWidth = width
            targetHeight = (width / srcRatio).toInt()
        } else {
            targetHeight = height
            targetWidth = (height * srcRatio).toInt()
        }

        val scaled = Bitmap.createScaledBitmap(src, targetWidth, targetHeight, true)

        val xOffset = (scaled.width - width) / 2
        val yOffset = (scaled.height - height) / 2

        return Bitmap.createBitmap(scaled, xOffset, yOffset, width, height)
    }
}
