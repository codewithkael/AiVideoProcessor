package com.codewithkael.aivideoprocessor.config.watermark

import android.graphics.Color
import com.codewithkael.aivideoprocessor.config.shared.WatermarkPosition

/**
 * Configuration for drawing a watermark (bitmap and/or text) on top of video frames.
 */
data class WatermarkConfig(
    val enabled: Boolean = false,

    // Optional bitmap watermark (e.g., logo)
    val bitmap: android.graphics.Bitmap? = null,
    val bitmapScale: Float = 0.2f, // fraction of frame width

    // Optional text watermark
    val text: String? = null,
    val textColor: Int = Color.WHITE,
    val textSizeSp: Float = 14f,
    val textShadowRadius: Float = 2f,
    val textShadowColor: Int = Color.BLACK,

    // Shared placement options
    val alpha: Float = 0.8f,
    val position: WatermarkPosition = WatermarkPosition.BOTTOM_RIGHT,
    val marginDp: Float = 8f
)
