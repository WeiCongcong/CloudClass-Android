package io.agora.edu.core.internal.education.impl.media

import android.view.SurfaceView
import android.view.ViewGroup
import io.agora.edu.core.internal.education.api.media.EduCameraVideoTrack
import io.agora.edu.core.internal.education.api.media.EduMicrophoneAudioTrack
import io.agora.edu.core.internal.rte.data.RteRenderConfig
import io.agora.edu.core.internal.rte.data.RteRenderMode
import io.agora.edu.core.internal.rte.data.RteVideoEncoderConfig
import io.agora.edu.core.internal.rte.module.impl.RteEngineImpl
import io.agora.edu.core.internal.rte.data.RteClientRole.CLIENT_ROLE_BROADCASTER
import io.agora.edu.core.internal.rte.data.RteVideoCanvas
import io.agora.edu.core.internal.rte.module.RteEngine
import io.agora.edu.core.internal.rte.module.RteEngine.Companion.ok

internal class EduCameraVideoTrackImpl : EduCameraVideoTrack {
    private var renderConfig: RteRenderConfig? = null

    private var previewSurface: SurfaceView? = null

    override fun start(): Int {
        return RteEngineImpl.enableLocalVideo(true)
    }

    override fun stop(): Int {
        return RteEngineImpl.enableLocalVideo(false)
    }

    override fun switchCamera(): Int {
        return RteEngineImpl.switchCamera()
    }

    override fun setView(container: ViewGroup?): Int {
        val videoCanvas: RteVideoCanvas
        var renderMode: Int = renderConfig?.rteRenderMode?.value ?: RteRenderMode.FIT.value
        removePreviewSurface()
        if (container == null) {
            videoCanvas = RteVideoCanvas(null, renderMode, 0)
        } else {
            previewSurface = RteEngine.createRendererView(container.context)
            previewSurface!!.setZOrderMediaOverlay(true)
            previewSurface!!.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT)
            container.addView(previewSurface)
            videoCanvas = RteVideoCanvas(previewSurface, renderMode, 0)
        }
        val a = RteEngineImpl.setupLocalVideo(videoCanvas)
        if (a < ok()) {
            return a
        }
        container?.let {
            val b = RteEngineImpl.setClientRole(CLIENT_ROLE_BROADCASTER)
            if (b < ok()) {
                return b
            }
        }
        val c = if (container == null) {
            RteEngineImpl.stopPreview()
        } else {
            RteEngineImpl.startPreview()
        }
        return c
    }

    private fun removePreviewSurface() {
        previewSurface?.let {
            (it.parent as? ViewGroup)?.removeView(it)
            previewSurface = null
        }
    }

    override fun setRenderConfig(config: RteRenderConfig): Int {
        this.renderConfig = config
        return RteEngineImpl.setLocalRenderMode(config.rteRenderMode.value, config.rteMirrorMode.value)
    }

    override fun setVideoEncoderConfig(config: RteVideoEncoderConfig): Int {
        return RteEngineImpl.setVideoEncoderConfiguration(config)
    }
}

internal class EduMicrophoneAudioTrackImpl : EduMicrophoneAudioTrack {
    override fun start(): Int {
        return RteEngineImpl.enableLocalAudio(true)
    }

    override fun stop(): Int {
        return RteEngineImpl.enableLocalAudio(false)
    }
}