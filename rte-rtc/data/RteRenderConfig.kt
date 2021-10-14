package io.agora.edu.core.internal.rte.data

import io.agora.rtc.video.VideoEncoderConfiguration

enum class RteRenderMode(var value: Int) {
    HIDDEN(1),
    FIT(2);
}

data class RteRenderConfig(
        var rteRenderMode: RteRenderMode = RteRenderMode.HIDDEN,
        var rteMirrorMode: RteMirrorMode = RteMirrorMode.AUTO
)

enum class RteMirrorMode(val value: Int) {
    AUTO(0),
    ENABLED(1),
    DISABLED(2);
}