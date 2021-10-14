package io.agora.edu.core.internal.rte.data

import android.view.View
import io.agora.rtc.video.VideoCanvas

class RteVideoCanvas(view: View?) : VideoCanvas(view) {

    constructor(view: View?, renderMode: Int, uid: Int) : this(view) {
        this.renderMode = renderMode
        this.uid = uid
        this.mirrorMode = 0
    }

    constructor(view: View?, renderMode: Int, channelId: String, uid: Int) : this(view, renderMode, uid) {
        this.channelId = channelId
        this.mirrorMode = 0
    }

    constructor(view: View?, renderMode: Int, uid: Int, mirrorMode: Int) : this(view, renderMode, uid) {
        this.mirrorMode = mirrorMode
    }

    constructor(view: View?, renderMode: Int, channelId: String, uid: Int, mirrorMode: Int)
            : this(view, renderMode, uid) {
        this.channelId = channelId
        this.mirrorMode = mirrorMode
    }
}