package com.codewithkael.aivideoprocessor.config.replace

import android.graphics.Bitmap
import com.codewithkael.aivideoprocessor.config.shared.BackgroundScaleType

data class ReplaceBackgroundConfig(
    val enabled: Boolean = false,
    val backgroundBitmap: Bitmap? = null,
    val backgroundColor: Int? = null,
    val scaleType: BackgroundScaleType = BackgroundScaleType.CROP,
    val minPersonConfidence: Float = 0.5f,
    val featherRadius: Float = 4f
)
