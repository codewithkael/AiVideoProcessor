package com.codewithkael.aivideoprocessor.capture

import android.content.Context
import com.codewithkael.aivideoprocessor.config.AiVideoProcessorConfig
import com.codewithkael.aivideoprocessor.frame.FrameProcessor
import com.codewithkael.aivideoprocessor.frame.defaultFrameProcessingHelpers
import io.github.crow_misia.libyuv.AbgrBuffer
import io.github.crow_misia.libyuv.PlanePrimitive
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

    /**
     * Updates the processor configuration in real-time.
     * Call this when you change filter settings in your UI.
     */
    fun updateConfig(newConfig: AiVideoProcessorConfig) {
        frameProcessor.updateConfig(newConfig, helpers)
    }

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
                // High-performance conversion using libyuv
                val bitmap = i420ToBitmap(frame)
                val processedBitmap = frameProcessor.process(bitmap)
                val processedFrame = AiVideoFrameFactory.fromBitmap(processedBitmap, frame.rotation)

                withContext(Dispatchers.Main) {
                    downstream.onFrameCaptured(processedFrame)
                }
            } finally {
                frame.release()
                isProcessing = false
            }
        }
    }

    private fun i420ToBitmap(frame: VideoFrame): android.graphics.Bitmap {
        val buffer = frame.buffer.toI420()
        val width = buffer.width
        val height = buffer.height

        // Wrap WebRTC I420Buffer into LibYuv I420Buffer using PlanePrimitive
        val planeY = PlanePrimitive.create(buffer.strideY, buffer.dataY)
        val planeU = PlanePrimitive.create(buffer.strideU, buffer.dataU)
        val planeV = PlanePrimitive.create(buffer.strideV, buffer.dataV)
        
        val libyuvI420 = io.github.crow_misia.libyuv.I420Buffer.wrap(
            planeY, planeU, planeV, width, height
        )

        // Use libyuv for high-performance I420 -> ABGR conversion
        val abgr = AbgrBuffer.allocate(width, height)
        
        // Native libyuv conversion call
        libyuvI420.convertTo(abgr)
        
        val bitmap = android.graphics.Bitmap.createBitmap(width, height, android.graphics.Bitmap.Config.ARGB_8888)
        bitmap.copyPixelsFromBuffer(abgr.asBuffer())

        abgr.close()
        libyuvI420.close()
        buffer.release()
        return bitmap
    }
}
