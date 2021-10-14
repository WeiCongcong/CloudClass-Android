package io.agora.edu.core.internal.rte.data

import io.agora.edu.core.internal.rte.data.RteChannelProfile.CHANNEL_PROFILE_LIVE_BROADCASTING
import io.agora.rtc2.ChannelMediaOptions

class RteChannelMediaOptions(
        val autoSubscribeAudio: Boolean,
        val autoSubscribeVideo: Boolean,
        // 不自动发流，必须调用publish接口才能发流，和3.4.5之前的版本保持一致
        val publishCameraTrack: Boolean = false,
        val publishAudioTrack: Boolean = false,
        // 大重构版本，通过mediaOptions设置每个rtcConnection在频道内的通信场景
        val channelProfile: Int = CHANNEL_PROFILE_LIVE_BROADCASTING,
        // 发送侧：在发布音频流时，是否需要混合麦克风；接收侧：在订阅音频流时，是否需要本地播放
        // true：（默认）在发布音频流时，需要混合麦克风；或者在订阅音频流时，需要本地播放
        // false：在发布音频流时，不需要混合麦克风；而且在订阅音频流时，也不需要本地播放。
        val enableAudioRecordingOrPlayout: Boolean = true) {

    fun convert(): ChannelMediaOptions {
        val tmp = ChannelMediaOptions()
        tmp.autoSubscribeAudio = autoSubscribeAudio
        tmp.autoSubscribeVideo = autoSubscribeVideo
        tmp.publishCameraTrack = publishCameraTrack
        tmp.publishAudioTrack = publishAudioTrack
        tmp.channelProfile = channelProfile
        tmp.enableAudioRecordingOrPlayout = enableAudioRecordingOrPlayout
        return tmp
    }
}