package io.agora.edu.core.internal.rte.data

import android.view.View
import io.agora.rtc2.video.VideoCanvas

class RteVideoCanvas(view: View?) : VideoCanvas(view) {

    constructor(view: View?, renderMode: Int) : this(view) {
        this.renderMode = renderMode
        uid = 0
    }

    constructor(view: View?, renderMode: Int, uid: Int) : this(view, renderMode) {
        this.uid = uid
    }

    constructor(view: View?, renderMode: Int, mirrorMode: Int, uid: Int) : this(view, renderMode, uid) {
        this.mirrorMode = mirrorMode
    }

    constructor(view: View?, renderMode: Int, mirrorMode: Int, sourceType: Int, uid: Int) : this(
            view, renderMode, mirrorMode, uid) {
        this.sourceType = sourceType
    }

    constructor(view: View?, renderMode: Int, mirrorMode: Int, sourceType: Int, sourceId: Int,
                uid: Int) : this(view, renderMode, mirrorMode, sourceType, uid) {
        this.sourceId = sourceId
    }
}