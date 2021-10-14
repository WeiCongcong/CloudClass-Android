package io.agora.edu.core.internal.rte.module.api

import android.content.Context
import androidx.annotation.NonNull
import io.agora.edu.core.internal.rte.data.*
import io.agora.edu.core.internal.rte.listener.*

interface IRteEngine {
    fun init(context: Context, appId: String, logFileDir: String, rtcRegion: String?,
             rtmRegion: String?)

    fun setRtcParameters(parameters: String): Int

    fun loginRtm(rtmUid: String, rtmToken: String, @NonNull callback: RteCallback<Unit>)

    fun logoutRtm()

    /**作用于rteChannel*/
    fun createChannel(channelId: String, eventListener: RteChannelEventListener,
                      mixingListener: RteAudioMixingListener): IRteChannel

    fun getRtcCallId(id: String): String

    fun getRtmSessionId(): String

    fun setLocalRenderMode(renderMode: Int, mirrorMode: Int): Int

    fun setRemoteRenderMode(channelId: String, uid: String, renderMode: Int, mirrorMode: Int): Int

    fun setLatencyLevel(channelId: String, level: Int)

    /**作用于子rtcChannel*/
    fun setClientRole(channelId: String, uid: String, role: Int): Int

    /**作用于子rtcChannel;setLatencyLevel*/
    fun setClientRole2(channelId: String, uid: String, role: Int): Int

    /**作用于mainChannel*/
    fun setClientRole(role: Int): Int

    fun publish(channelId: String, uid: String): Int

    fun unpublish(channelId: String, uid: String): Int

    fun updateLocalStream(channelId: String, uid: String, hasAudio: Boolean, hasVideo: Boolean): Int

    fun updateLocalAudioStream(channelId: String, uid: String, hasAudio: Boolean): Int

    fun updateLocalVideoStream(channelId: String, uid: String, hasVideo: Boolean): Int

    fun muteRemoteStream(channelId: String, uid: String, muteAudio: Boolean, muteVideo: Boolean): Int

    fun muteLocalStream(channelId: String, uid: String, muteAudio: Boolean, muteVideo: Boolean): Int

    fun muteLocalAudioStream(channelId: String, uid: String, muteAudio: Boolean): Int

    fun muteLocalVideoStream(channelId: String, uid: String, muteVideo: Boolean): Int

    fun startPreview(): Int

    fun stopPreview(): Int

    /**
     * switch high/low stream
     * */
    fun enableDualStreamMode(enabled: Boolean): Int

    fun setRemoteVideoStreamType(channelId: String, uid: String, type: RteVideoStreamType): Int

    /**used in main channel*/
    fun setVideoEncoderConfiguration(config: RteVideoEncoderConfig): Int

    /**used in sub channel*/
    fun setVideoEncoderConfiguration(channelId: String, uid: String, config: RteVideoEncoderConfig): Int

    fun enableLocalVideo(enabled: Boolean): Int
    fun enableLocalAudio(enabled: Boolean): Int

    /**used in global*/
    fun switchCamera(): Int

    /**used in global*/
    fun setupLocalVideo(local: RteVideoCanvas): Int

    /**used in sub channel*/
    fun setupRemoteVideo(channelId: String, curChannelLocalUid: String, canvas: RteVideoCanvas): Int
    fun setEnableSpeakerphone(enabled: Boolean): Int

    fun isSpeakerphoneEnabled(): Boolean

    /*AudioMixing*/
    fun startAudioMixing(filePath: String, loopback: Boolean, replace: Boolean, cycle: Int): Int

    fun enableLocalTrapezoidCorrection(enabled: Boolean): Int

    fun setLocalTrapezoidCorrectionOptions(options: RteTrapezoidCorrectionOptions): Int

    fun getLocalTrapezoidCorrectionOptions(): RteTrapezoidCorrectionOptions?

    fun enableRemoteTrapezoidCorrectionEx(channelId: String, curChannelLocalUid: String, enabled: Boolean): Int

    fun setRemoteTrapezoidCorrectionOptionsEx(channelId: String, curChannelLocalUid: String,
                                              options: RteTrapezoidCorrectionOptions): Int

    fun getRemoteTrapezoidCorrectionOptionsEx(channelId: String, curChannelLocalUid: String): RteTrapezoidCorrectionOptions?

    fun applyTrapezoidCorrectionToRemoteEx(channelId: String, curChannelLocalUid: String, enabled: Boolean): Int

    fun applyVideoEncoderMirrorToRemoteEx(channelId: String, curChannelLocalUid: String, mirrorModeValue: Int): Int

    fun enableBrightnessCorrection(enabled: Boolean, mode: RteBrightnessCorrectionMode): Int

    fun applyBrightnessCorrectionToRemoteEx(channelId: String, curChannelLocalUid: String,
                                            enabled: Boolean, mode: RteBrightnessCorrectionMode): Int

    fun setAudioMixingPosition(pos: Int): Int

    fun pauseAudioMixing(): Int

    fun resumeAudioMixing(): Int

    fun stopAudioMixing(): Int

    fun getAudioMixingDuration(): Int

    fun getAudioMixingCurrentPosition(): Int

    /*AudioEffect*/
    fun setLocalVoiceChanger(voiceManager: RteAudioVoiceChanger): Int

    fun setLocalVoiceReverbPreset(preset: RteAudioReverbPreset): Int

    /*MediaDevice*/
    fun enableInEarMonitoring(enabled: Boolean): Int

    fun enableAudioVolumeIndication(interval: Int, smooth: Int, report_vad: Boolean)

    fun setMediaDeviceListener(channelId: String, listener: RteMediaDeviceListener): Int
    fun setAudioMixingListener(channelId: String, listener: RteAudioMixingListener): Int
    fun setSpeakerReportListener(channelId: String, listener: RteSpeakerReportListener): Int

    fun setChannelMode(channelId: String, curChannelLocalUid: String, mode: Int): Int

    fun getError(code: Int): String

    fun version(): String

    fun dispose()
}
