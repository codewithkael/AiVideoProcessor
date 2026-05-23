package com.codewithkael.aivideoprocessor.effect.watermark

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import com.codewithkael.aivideoprocessor.config.shared.WatermarkPosition
import com.codewithkael.aivideoprocessor.config.watermark.WatermarkConfig

interface WatermarkDrawer {
    fun drawWatermark(input: Bitmap, config: WatermarkConfig): Bitmap
}

class SimpleWatermarkDrawer : WatermarkDrawer {

    override fun drawWatermark(input: Bitmap, config: WatermarkConfig): Bitmap {
        if (!config.enabled) return input

        val mutable = input.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(mutable)

        config.bitmap?.let { watermarkBitmap ->
            drawBitmapWatermark(canvas, mutable, watermarkBitmap, config)
        }

        config.text?.takeIf { it.isNotBlank() }?.let { text ->
            drawTextWatermark(canvas, mutable, text, config)
        }

        return mutable
    }

    private fun drawBitmapWatermark(
        canvas: Canvas,
        frame: Bitmap,
        watermark: Bitmap,
        config: WatermarkConfig
    ) {
        val frameWidth = frame.width.toFloat()
        val frameHeight = frame.height.toFloat()

        val targetWidth = frameWidth * config.bitmapScale
        val aspectRatio = watermark.width.toFloat() / watermark.height.toFloat()
        val targetHeight = targetWidth / aspectRatio

        val scaled = Bitmap.createScaledBitmap(
            watermark,
            targetWidth.toInt().coerceAtLeast(1),
            targetHeight.toInt().coerceAtLeast(1),
            true
        )

        val marginPx = dpToPx(frame, config.marginDp)

        val left: Float
        val top: Float

        when (config.position) {
            WatermarkPosition.TOP_LEFT -> {
                left = marginPx
                top = marginPx
            }
            WatermarkPosition.TOP_RIGHT -> {
                left = frameWidth - scaled.width - marginPx
                top = marginPx
            }
            WatermarkPosition.BOTTOM_LEFT -> {
                left = marginPx
                top = frameHeight - scaled.height - marginPx
            }
            WatermarkPosition.BOTTOM_RIGHT -> {
                left = frameWidth - scaled.width - marginPx
                top = frameHeight - scaled.height - marginPx
            }
            WatermarkPosition.CENTER -> {
                left = (frameWidth - scaled.width) / 2f
                top = (frameHeight - scaled.height) / 2f
            }
        }

        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            alpha = (config.alpha * 255).toInt().coerceIn(0, 255)
        }

        canvas.drawBitmap(scaled, left, top, paint)
    }

    private fun drawTextWatermark(
        canvas: Canvas,
        frame: Bitmap,
        text: String,
        config: WatermarkConfig
    ) {
        val frameWidth = frame.width.toFloat()
        val frameHeight = frame.height.toFloat()

        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = config.textColor
            textSize = spToPx(frame, config.textSizeSp)
            alpha = (config.alpha * 255).toInt().coerceIn(0, 255)
            setShadowLayer(
                config.textShadowRadius,
                0f,
                0f,
                config.textShadowColor
            )
        }

        val bounds = Rect()
        paint.getTextBounds(text, 0, text.length, bounds)

        val textWidth = bounds.width().toFloat()
        val textHeight = bounds.height().toFloat()

        val marginPx = dpToPx(frame, config.marginDp)

        val x: Float
        val y: Float

        when (config.position) {
            WatermarkPosition.TOP_LEFT -> {
                x = marginPx
                y = marginPx + textHeight
            }
            WatermarkPosition.TOP_RIGHT -> {
                x = frameWidth - textWidth - marginPx
                y = marginPx + textHeight
            }
            WatermarkPosition.BOTTOM_LEFT -> {
                x = marginPx
                y = frameHeight - marginPx
            }
            WatermarkPosition.BOTTOM_RIGHT -> {
                x = frameWidth - textWidth - marginPx
                y = frameHeight - marginPx
            }
            WatermarkPosition.CENTER -> {
                x = (frameWidth - textWidth) / 2f
                y = (frameHeight + textHeight) / 2f
            }
        }

        canvas.drawText(text, x, y, paint)
    }

    private fun dpToPx(bitmap: Bitmap, dp: Float): Float {
        val density = 3f // fallback density if not provided by caller; host app can adapt
        return dp * density
    }

    private fun spToPx(bitmap: Bitmap, sp: Float): Float {
        val density = 3f
        return sp * density
    }
}
