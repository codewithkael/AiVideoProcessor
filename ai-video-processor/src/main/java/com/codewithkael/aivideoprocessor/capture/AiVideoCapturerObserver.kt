package com.codewithkael.aivideoprocessor.capture

import android.content.Context
import com.codewithkael.aivideoprocessor.config.AiVideoProcessorConfig
import com.codewithkael.aivideoprocessor.frame.FrameProcessor
import com.codewithkael.aivideoprocessor.frame.YuvFrame
import com.codewithkael.aivideoprocessor.frame.defaultFrameProcessingHelpers
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
            // Avoid backpressure buildup: just forward original frame if busy
            downstream.onFrameCaptured(frame)
            return
        }

        isProcessing = true

        // Retain the frame while processing off-thread
        frame.retain()

        scope.launch {
            try {
                val yuv = YuvFrame()
                yuv.fromVideoFrame(frame, YuvFrame.PROCESSING_NONE, frame.timestampNs)
                val bitmap = yuv.toBitmap()

                val processedFrame = if (bitmap != null) {
                    val processedBitmap = frameProcessor.process(bitmap)
                    // Reuse original rotation; timestamp updated to now
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
}
