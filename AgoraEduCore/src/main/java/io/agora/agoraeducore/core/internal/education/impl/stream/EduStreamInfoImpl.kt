package io.agora.agoraeducore.core.internal.education.impl.stream

import io.agora.agoraeducore.core.internal.framework.data.EduStreamInfo
import io.agora.agoraeducore.core.internal.framework.data.VideoSourceType
import io.agora.agoraeducore.core.internal.framework.EduBaseUserInfo

internal class EduStreamInfoImpl(
        streamUuid: String,
        streamName: String?,
        videoSourceType: VideoSourceType,
        hasVideo: Boolean,
        hasAudio: Boolean,
        publisher: EduBaseUserInfo,
        var updateTime: Long?
) : EduStreamInfo(streamUuid, streamName, videoSourceType, hasVideo, hasAudio, publisher) {
}
