package com.codewithkael.aivideoprocessor.ml

import android.graphics.Bitmap

interface SegmentationEngine {
    suspend fun segment(input: Bitmap): SegmentationMask
}

data class SegmentationMask(
    val width: Int,
    val height: Int,
    /**
     * Per-pixel confidence that this pixel belongs to the foreground/person.
     * Size is width * height.
     */
    val confidence: FloatArray
) {
    fun isForeground(x: Int, y: Int, threshold: Float): Boolean {
        val index = y * width + x
        val value = confidence.getOrNull(index) ?: 0f
        return value >= threshold
    }
}
