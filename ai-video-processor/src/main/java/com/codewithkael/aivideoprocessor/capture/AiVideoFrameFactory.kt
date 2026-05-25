package com.codewithkael.aivideoprocessor.capture

import android.graphics.Bitmap
import io.github.crow_misia.libyuv.AbgrBuffer
import io.github.crow_misia.libyuv.I420Buffer
import org.webrtc.JavaI420Buffer
import org.webrtc.VideoFrame

/**
 * Factory to create WebRTC [VideoFrame] instances from processed [Bitmap]s.
 *
 * Converts the bitmap to an I420 buffer so that downstream WebRTC components
 * (VideoProcessor, VideoSource) can safely crop/scale frames.
 */
object AiVideoFrameFactory {

    fun fromBitmap(bitmap: Bitmap, rotation: Int): VideoFrame {
        val i420 = convertBitmapToI420Buffer(bitmap)
        return VideoFrame(i420, rotation, System.nanoTime())
    }

    private fun convertBitmapToI420Buffer(bitmap: Bitmap): JavaI420Buffer {
        val width = bitmap.width
        val height = bitmap.height

        // 1. Convert Bitmap to ABGR buffer
        val abgrBuffer = AbgrBuffer.allocate(width, height)
        bitmap.copyPixelsToBuffer(abgrBuffer.asBuffer())

        // 2. Use libyuv for high-performance ABGR -> I420 conversion
        val i420Buffer = I420Buffer.allocate(width, height)
        abgrBuffer.convertTo(i420Buffer)

        // 3. Wrap libyuv buffers into WebRTC JavaI420Buffer
        return JavaI420Buffer.wrap(
            width,
            height,
            i420Buffer.planeY.buffer,
            i420Buffer.planeY.rowStride.value,
            i420Buffer.planeU.buffer,
            i420Buffer.planeU.rowStride.value,
            i420Buffer.planeV.buffer,
            i420Buffer.planeV.rowStride.value,
            {
                i420Buffer.close()
                abgrBuffer.close()
            }
        )
    }
}
