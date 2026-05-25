package com.codewithkael.aivideoprocessor.capture

import android.content.Context
import com.codewithkael.aivideoprocessor.config.AiVideoProcessorConfig
import com.codewithkael.aivideoprocessor.frame.FrameProcessor
import com.codewithkael.aivideoprocessor.frame.defaultFrameProcessingHelpers
import io.github.crow_misia.libyuv.AbgrBuffer
import io.github.crow_misia.libyuv.Nv21Buffer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.webrtc.CapturerObserver
import org.webrtc.VideoFrame

/**
 * Drop-in [CapturerObserver] that applies AI video effects
 * (blur background, replace background, watermark) before forwarding frames
 * to the downstream observer.
 */
class AiVideoCapturerObserver(
    private val context: Context,
    private val downstream: CapturerObserver,
    private val config: AiVideoProcessorConfig
) : CapturerObserver {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val helpers by lazy { defaultFrameProcessingHelpers(context) }
    private val frameProcessor by lazy { FrameProcessor.fromConfig(config, helpers) }

    @Volatile
    private var isProcessing = false

    override fun onCapturerStarted(success: Boolean) {
        downstream.onCapturerStarted(success)
    }

    override fun onCapturerStopped() {
        downstream.onCapturerStopped()
    }

    override fun onFrameCaptured(frame: VideoFrame) {
        if (isProcessing) {
            downstream.onFrameCaptured(frame)
            return
        }

        isProcessing = true
        frame.retain()

        scope.launch {
            try {
                val nv21 = toNv21(frame)
                val bitmap = nv21ToBitmap(nv21)

                val processedFrame = if (bitmap != null) {
                    val processedBitmap = frameProcessor.process(bitmap)
                    AiVideoFrameFactory.fromBitmap(processedBitmap, frame.rotation)
                } else {
                    frame
                }

                withContext(Dispatchers.Main) {
                    downstream.onFrameCaptured(processedFrame)
                }
            } finally {
                frame.release()
                isProcessing = false
            }
        }
    }

    private fun toNv21(frame: VideoFrame): Nv21Buffer? {
        val buffer = frame.buffer.toI420()
        val width = buffer.width
        val height = buffer.height

        val nv21 = Nv21Buffer.allocate(width, height)
        val nv21Buf = nv21.asBuffer()

        val yPlane = buffer.dataY
        val uPlane = buffer.dataU
        val vPlane = buffer.dataV
        val yStride = buffer.strideY
        val uStride = buffer.strideU
        val vStride = buffer.strideV

        val sizeY = width * height
        val chromaWidth = (width + 1) / 2
        val chromaHeight = (height + 1) / 2
        val chromaStride = width

        // Y
        for (y in 0 until height) {
            val rowStart = y * yStride
            for (x in 0 until width) {
                nv21Buf.put(y * width + x, yPlane[rowStart + x])
            }
        }

        // UV interleaved (VU for NV21)
        for (y in 0 until chromaHeight) {
            val uRowStart = y * uStride
            val vRowStart = y * vStride
            for (x in 0 until chromaWidth) {
                val u = uPlane[uRowStart + x]
                val v = vPlane[vRowStart + x]
                val index = sizeY + y * chromaStride + 2 * x
                nv21Buf.put(index, v)
                nv21Buf.put(index + 1, u)
            }
        }

        buffer.release()
        return nv21
    }

    private fun nv21ToBitmap(nv21: Nv21Buffer?): android.graphics.Bitmap? {
        if (nv21 == null) return null

        val width = nv21.width
        val height = nv21.height

        val abgr = AbgrBuffer.allocate(width, height)
        nv21.convertTo(abgr)

        val bitmap = android.graphics.Bitmap.createBitmap(width, height, android.graphics.Bitmap.Config.ARGB_8888)
        bitmap.copyPixelsFromBuffer(abgr.asBuffer())

        return bitmap
    }
}
