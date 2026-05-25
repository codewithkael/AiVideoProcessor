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
    private var cachedBackground: Bitmap? = null
    private var cachedBgPixels: IntArray? = null
    private var lastBgSource: Bitmap? = null
    private var lastTargetWidth = 0
    private var lastTargetHeight = 0

    suspend fun apply(input: Bitmap): Bitmap {
        val background = config.backgroundBitmap ?: return input
        val mask: SegmentationMask = segmentationEngine.segment(input)

        val width = input.width
        val height = input.height

        // 1. Prepare/Cache Background Pixels
        val bgPix = getOrPrepareBackgroundPixels(background, width, height)

        // 2. Prepare Output
        val output = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val total = width * height
        
        // 3. Get Original Pixels
        val origPix = IntArray(total)
        input.getPixels(origPix, 0, width, 0, 0, width, height)

        // 4. Optimized Blending
        val finalPixels = IntArray(total)
        val threshold = config.minPersonConfidence
        val confidenceArray = mask.confidence

        for (i in 0 until total) {
            // Check confidence directly from the array for speed
            val isForeground = (confidenceArray.getOrNull(i) ?: 0f) >= threshold
            finalPixels[i] = if (isForeground) origPix[i] else bgPix[i]
        }

        output.setPixels(finalPixels, 0, width, 0, 0, width, height)
        return output
    }

    private fun getOrPrepareBackgroundPixels(source: Bitmap, width: Int, height: Int): IntArray {
        // Only re-process if the source bitmap or target dimensions changed
        if (cachedBackground != null && 
            lastBgSource === source && 
            lastTargetWidth == width && 
            lastTargetHeight == height) {
            return cachedBgPixels!!
        }

        // Clean up old cache
        cachedBackground?.recycle()

        val prepared = when (config.scaleType) {
            BackgroundScaleType.CROP -> centerCrop(source, width, height)
            BackgroundScaleType.FILL -> Bitmap.createScaledBitmap(source, width, height, true)
            BackgroundScaleType.FIT -> scaleFit(source, width, height)
        }

        val pixels = IntArray(width * height)
        prepared.getPixels(pixels, 0, width, 0, 0, width, height)

        cachedBackground = prepared
        cachedBgPixels = pixels
        lastBgSource = source
        lastTargetWidth = width
        lastTargetHeight = height

        return pixels
    }

    private fun scaleFit(src: Bitmap, width: Int, height: Int): Bitmap {
        val out = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(out)
        
        // Fill background color first (e.g. black letterboxing)
        canvas.drawColor(config.backgroundColor ?: 0xFF000000.toInt())

        val srcRatio = src.width.toFloat() / src.height.toFloat()
        val dstRatio = width.toFloat() / height.toFloat()

        val tw: Int
        val th: Int
        if (dstRatio > srcRatio) {
            th = height
            tw = (height * srcRatio).toInt()
        } else {
            tw = width
            th = (width / srcRatio).toInt()
        }

        val scaled = Bitmap.createScaledBitmap(src, tw.coerceAtLeast(1), th.coerceAtLeast(1), true)
        val left = (width - scaled.width) / 2f
        val top = (height - scaled.height) / 2f
        canvas.drawBitmap(scaled, left, top, null)
        if (scaled !== src) scaled.recycle()

        return out
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
