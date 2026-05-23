package com.codewithkael.aivideoprocessor.frame

import android.graphics.Bitmap
import android.graphics.ImageFormat
import android.graphics.Matrix
import android.graphics.Rect
import android.graphics.YuvImage
import android.graphics.BitmapFactory
import org.webrtc.VideoFrame
import java.io.ByteArrayOutputStream

/**
 * Helper for converting WebRTC VideoFrame buffers to Bitmaps without external YUV libraries.
 */
class YuvFrame(
    var width: Int = 0,
    var height: Int = 0,
    var nv21Buffer: ByteArray? = null,
    var rotationDegree: Int = 0,
    var timestampNs: Long = 0L
) {
    private val planeLock = Any()

    companion object {
        const val PROCESSING_NONE: Int = 0
        const val PROCESSING_CROP_TO_SQUARE: Int = 1
    }

    fun fromVideoFrame(
        frame: VideoFrame,
        processingFlags: Int = PROCESSING_NONE,
        timestampNs: Long = System.nanoTime()
    ) {
        synchronized(planeLock) {
            this.timestampNs = timestampNs
            this.rotationDegree = frame.rotation

            val buffer = frame.buffer
            val i420 = buffer.toI420()
            try {
                if ((processingFlags and PROCESSING_CROP_TO_SQUARE) != 0) {
                    copyPlanesCropped(i420)
                } else {
                    copyPlanes(i420)
                }
            } finally {
                i420.release()
            }
        }
    }

    private fun copyPlanes(i420: VideoFrame.I420Buffer) {
        width = i420.width
        height = i420.height

        val yPlane = i420.dataY
        val uPlane = i420.dataU
        val vPlane = i420.dataV
        val yStride = i420.strideY
        val uStride = i420.strideU
        val vStride = i420.strideV

        val sizeY = width * height
        val chromaWidth = (width + 1) / 2
        val chromaHeight = (height + 1) / 2
        val nv21Size = sizeY + width * chromaHeight

        if (nv21Buffer == null || nv21Buffer!!.size != nv21Size) {
            nv21Buffer = ByteArray(nv21Size)
        }
        val out = nv21Buffer!!

        // Copy Y plane
        var dstIndex = 0
        for (y in 0 until height) {
            val rowStart = y * yStride
            for (x in 0 until width) {
                out[dstIndex++] = yPlane[rowStart + x]
            }
        }

        // Interleave V and U to NV21
        var uvOffset = sizeY
        for (y in 0 until chromaHeight) {
            val uRowStart = y * uStride
            val vRowStart = y * vStride
            for (x in 0 until chromaWidth) {
                val v = vPlane[vRowStart + x]
                val u = uPlane[uRowStart + x]
                out[uvOffset++] = v
                out[uvOffset++] = u
            }
        }
    }

    private fun copyPlanesCropped(i420: VideoFrame.I420Buffer) {
        val srcWidth = i420.width
        val srcHeight = i420.height

        val cropBuffer: VideoFrame.Buffer = if (srcWidth > srcHeight) {
            val offsetX = (srcWidth - srcHeight) / 2
            i420.cropAndScale(offsetX, 0, srcHeight, srcHeight, srcHeight, srcHeight)
        } else {
            val offsetY = (srcHeight - srcWidth) / 2
            i420.cropAndScale(0, offsetY, srcWidth, srcWidth, srcWidth, srcWidth)
        }

        val croppedI420 = cropBuffer.toI420()
        try {
            copyPlanes(croppedI420)
        } finally {
            croppedI420.release()
            cropBuffer.release()
        }
    }

    fun toBitmap(): Bitmap? {
        val data = nv21Buffer ?: return null
        if (width <= 0 || height <= 0) return null

        val yuvImage = YuvImage(data, ImageFormat.NV21, width, height, null)
        val jpegOutput = ByteArrayOutputStream()
        yuvImage.compressToJpeg(Rect(0, 0, width, height), 100, jpegOutput)
        val jpegBytes = jpegOutput.toByteArray()

        var bitmap = BitmapFactory.decodeByteArray(jpegBytes, 0, jpegBytes.size)

        if (bitmap != null && rotationDegree != 0) {
            val m = Matrix().apply { postRotate(rotationDegree.toFloat()) }
            bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, m, true)
        }

        return bitmap
    }

    fun clear() {
        nv21Buffer = null
    }
}
