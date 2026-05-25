package com.codewithkael.aivideoprocessor.ml

import android.content.Context
import android.graphics.Bitmap
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.segmentation.Segmentation
import com.google.mlkit.vision.segmentation.selfie.SelfieSegmenterOptions
import kotlinx.coroutines.tasks.await

/**
 * Default ML Kit-based implementation of [SegmentationEngine].
 *
 * Uses Selfie Segmentation in STREAM_MODE for real-time video frames.
 */
class MlKitSegmentationEngine(
    context: Context
) : SegmentationEngine {

    private val options = SelfieSegmenterOptions.Builder()
        .setDetectorMode(SelfieSegmenterOptions.STREAM_MODE)
        .build()

    private val client = Segmentation.getClient(options)

    override suspend fun segment(input: Bitmap): SegmentationMask {
        val image = InputImage.fromBitmap(input, 0)
        val result = client.process(image).await()

        // ML Kit selfie segmentation exposes a mask as a ByteArray or FloatArray depending
        // on the API level. Here we build a confidence buffer sized to the input frame.
        val mask = result.buffer
        val maskWidth = result.width
        val maskHeight = result.height

        val confidence = FloatArray(maskWidth * maskHeight)
        mask.rewind()
        var i = 0
        while (mask.hasRemaining() && i < confidence.size) {
            confidence[i++] = mask.float
        }

        return SegmentationMask(
            width = maskWidth,
            height = maskHeight,
            confidence = confidence
        )
    }
}
