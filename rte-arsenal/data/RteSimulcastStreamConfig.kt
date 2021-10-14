package io.agora.edu.core.internal.rte.data

import io.agora.rtc2.SimulcastStreamConfig
import io.agora.rtc2.video.VideoEncoderConfiguration

data class RteSimulcastStreamConfig(
        val dimensionsWidth: Int,
        val dimensionsHeight: Int,
        val bitrate: Int,
        val framerate: Int
) {
    fun convert(): SimulcastStreamConfig {
        return SimulcastStreamConfig(VideoEncoderConfiguration.VideoDimensions(
                dimensionsWidth, dimensionsHeight
        ), bitrate, framerate)
    }
}
