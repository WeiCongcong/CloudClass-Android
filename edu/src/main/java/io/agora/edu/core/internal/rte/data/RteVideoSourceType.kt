package io.agora.edu.core.internal.rte.data

import io.agora.rtc2.Constants.VideoSourceType

enum class RteVideoSourceType(val value: Int) {
    VIDEO_SOURCE_CAMERA_PRIMARY(0),
    VIDEO_SOURCE_SCREEN_PRIMARY(2),
    VIDEO_SOURCE_CUSTOM(4);

    fun convert(): VideoSourceType {
        return when (this.value) {
            VIDEO_SOURCE_CAMERA_PRIMARY.value -> VideoSourceType.VIDEO_SOURCE_CAMERA_PRIMARY
            VIDEO_SOURCE_SCREEN_PRIMARY.value -> VideoSourceType.VIDEO_SOURCE_SCREEN_PRIMARY
            VIDEO_SOURCE_CUSTOM.value -> VideoSourceType.VIDEO_SOURCE_CUSTOM
            else -> VideoSourceType.VIDEO_SOURCE_UNKNOWN
        }
    }
}