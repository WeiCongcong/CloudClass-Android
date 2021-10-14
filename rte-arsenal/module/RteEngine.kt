package io.agora.edu.core.internal.rte.module

import android.content.Context
import android.view.SurfaceView
import android.view.TextureView
import io.agora.rtc2.Constants.ERR_OK
import io.agora.rtc2.IRtcEngineEventHandler
import io.agora.rtc2.RtcEngineConfig
import io.agora.rtc2.RtcEngineEx

abstract class RteEngine {
    private val tag = "RteEngine"

    companion object {
        fun create(context: Context, appId: String, handler: IRtcEngineEventHandler): RtcEngineEx {
            return RtcEngineEx.create(context, appId, handler) as RtcEngineEx
        }

        fun create(config: RtcEngineConfig): RtcEngineEx {
            return RtcEngineEx.create(config) as RtcEngineEx
        }

        fun createRendererView(context: Context?): SurfaceView? {
            return SurfaceView(context)
        }

        fun createTextureView(context: Context): TextureView {
            return TextureView(context)
        }

        fun getErrorDescription(err: Int): String {
            return RtcEngineEx.getErrorDescription(err)
        }

        fun ok(): Int {
            return ERR_OK
        }

        fun version(): String {
            return RtcEngineEx.getSdkVersion()
        }

        fun destroy() {
            RtcEngineEx.destroy()
        }
    }
}