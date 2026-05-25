package com.codewithkael.aivideoprocessor.capture

import android.graphics.Bitmap
import org.webrtc.VideoFrame

/**
 * Simple factory to create WebRTC [VideoFrame] instances from processed [Bitmap]s.
 *
 * This is intentionally minimal; host apps can adapt or replace this if they have
 * a more efficient pipeline. For now, we just wrap the bitmap using
 * VideoFrame.Buffer and propagate rotation.
 */
object AiVideoFrameFactory {

    fun fromBitmap(bitmap: Bitmap, rotation: Int): VideoFrame {
        val buffer: VideoFrame.Buffer = BitmapVideoFrameBuffer(bitmap)
        return VideoFrame(buffer, rotation, System.nanoTime())
    }
}

/**
 * Very basic VideoFrame.Buffer implementation that wraps a [Bitmap].
 * In a real app, you may want a GPU-backed buffer for performance.
 */
class BitmapVideoFrameBuffer(
    private val bitmap: Bitmap
) : VideoFrame.Buffer {

    override fun getWidth(): Int = bitmap.width

    override fun getHeight(): Int = bitmap.height

    override fun toI420(): VideoFrame.I420Buffer {
        throw UnsupportedOperationException("BitmapVideoFrameBuffer.toI420 is not implemented")
    }

    override fun retain() {
        // no-op; nothing to retain
    }

    override fun release() {
        // no-op; caller owns bitmap lifecycle
    }

    override fun cropAndScale(
        x: Int,
        y: Int,
        width: Int,
        height: Int,
        scaledWidth: Int,
        scaledHeight: Int
    ): VideoFrame.Buffer {
        throw UnsupportedOperationException("BitmapVideoFrameBuffer.cropAndScale is not implemented")
    }
}
