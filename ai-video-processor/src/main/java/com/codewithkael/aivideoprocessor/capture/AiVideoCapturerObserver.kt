package com.codewithkael.aivideoprocessor.capture

import android.content.Context
import com.codewithkael.aivideoprocessor.config.AiVideoProcessorConfig
import com.codewithkael.aivideoprocessor.frame.FrameProcessor
import com.codewithkael.aivideoprocessor.frame.defaultFrameProcessingHelpers
import io.github.crow_misia.libyuv.AbgrBuffer
import io.github.crow_misia.libyuv.I420Buffer
import io.github.crow_misia.libyuv.PlanePrimitive
import io.github.crow_misia.libyuv.RotateMode
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
                // 1. Convert and Physically Rotate the frame to "Upright" (Portrait)
                // This ensures AI filters and watermarks align with the screen.
                val bitmap = i420ToBitmapNormalized(frame)
                
                // 2. Apply AI Effects
                val processedBitmap = frameProcessor.process(bitmap)
                
                // 3. Create a new frame (rotation is now 0 as pixels are already rotated)
                val processedFrame = AiVideoFrameFactory.fromBitmap(processedBitmap, 0)

                withContext(Dispatchers.Main) {
                    downstream.onFrameCaptured(processedFrame)
                }
            } finally {
                frame.release()
                isProcessing = false
            }
        }
    }

    private fun i420ToBitmapNormalized(frame: VideoFrame): android.graphics.Bitmap {
        val buffer = frame.buffer.toI420()
        val width = buffer.width
        val height = buffer.height
        val rotation = frame.rotation

        // Wrap WebRTC I420Buffer into LibYuv I420Buffer
        val planeY = PlanePrimitive.create(buffer.strideY, buffer.dataY)
        val planeU = PlanePrimitive.create(buffer.strideU, buffer.dataU)
        val planeV = PlanePrimitive.create(buffer.strideV, buffer.dataV)
        
        val libyuvI420 = I420Buffer.wrap(planeY, planeU, planeV, width, height)

        // Determine if we need to swap dimensions for rotation
        val isSwapped = rotation == 90 || rotation == 270
        val targetWidth = if (isSwapped) height else width
        val targetHeight = if (isSwapped) width else height

        val rotateMode = when (rotation) {
            90 -> RotateMode.ROTATE_90
            180 -> RotateMode.ROTATE_180
            270 -> RotateMode.ROTATE_270
            else -> RotateMode.ROTATE_0
        }

        // Physically rotate the pixels
        val uprightI420 = if (rotateMode != RotateMode.ROTATE_0) {
            val rotated = I420Buffer.allocate(targetWidth, targetHeight)
            libyuvI420.rotate(rotated, rotateMode)
            rotated
        } else {
            libyuvI420
        }

        // Convert to Bitmap (ABGR)
        val abgr = AbgrBuffer.allocate(targetWidth, targetHeight)
        uprightI420.convertTo(abgr)
        
        val bitmap = android.graphics.Bitmap.createBitmap(targetWidth, targetHeight, android.graphics.Bitmap.Config.ARGB_8888)
        bitmap.copyPixelsFromBuffer(abgr.asBuffer())

        // Clean up
        abgr.close()
        if (uprightI420 !== libyuvI420) uprightI420.close()
        libyuvI420.close()
        buffer.release()

        return bitmap
    }
}
