package io.agora.edu.core.internal.education.api.media

import android.view.ViewGroup
import io.agora.edu.core.internal.rte.data.RteRenderConfig
import io.agora.edu.core.internal.rte.data.RteVideoEncoderConfig

interface EduMediaTrack {
    fun start(): Int
    fun stop(): Int
}

interface EduCameraVideoTrack : EduMediaTrack {
    fun switchCamera(): Int
    fun setView(container: ViewGroup?): Int
    fun setRenderConfig(config: RteRenderConfig): Int
    fun setVideoEncoderConfig(configRte: RteVideoEncoderConfig): Int
}

interface EduMicrophoneAudioTrack : EduMediaTrack {
}