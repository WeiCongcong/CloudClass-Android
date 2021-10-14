package io.agora.edu.core.internal.rte.module

import android.content.Context
import android.view.SurfaceView
import android.view.TextureView
import io.agora.rtc.Constants.ERR_OK
import io.agora.rtc.IRtcEngineEventHandler
import io.agora.rtc.RtcEngine
import io.agora.rtc.RtcEngineConfig
import io.agora.rtc.RtcEngineEx
import io.agora.rtc.internal.RtcEngineImpl

abstract class RteEngine {
    private val tag = "RteEngine"

    companion object {
        fun create(context: Context, appId: String, handler: IRtcEngineEventHandler): RtcEngineEx {
            return RtcEngine.create(context, appId, handler) as RtcEngineEx
        }

        fun create(config: RtcEngineConfig): RtcEngineEx {
            return RtcEngine.create(config) as RtcEngineEx
        }

        fun createRendererView(context: Context?): SurfaceView {
            return RtcEngine.CreateRendererView(context)
        }

        fun createTextureView(context: Context): TextureView {
            return RtcEngine.CreateTextureView(context)
        }

        fun getErrorDescription(err: Int): String {
            return RtcEngine.getErrorDescription(err)
        }

        fun ok(): Int {
            return ERR_OK
        }

        fun version(): String {
            return RtcEngine.getSdkVersion()
        }

        fun destroy() {
            RtcEngine.destroy()
        }
    }
}