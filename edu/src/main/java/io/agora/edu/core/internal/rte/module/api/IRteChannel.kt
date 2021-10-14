package io.agora.edu.core.internal.rte.module.api

import androidx.annotation.NonNull
import io.agora.edu.core.internal.rte.data.*
import io.agora.edu.core.internal.rte.listener.RteCallback

interface IRteChannel {

    fun join(rtcOptionalInfo: String, rtcToken: String, rtcUid: String,
             mediaOptions: RteChannelMediaOptions, encryptionConfig: RteEncryptionConfig,
             @NonNull callback: RteCallback<Void>)

    fun joinEx(rtcOptionalInfo: String, rtcToken: String, rtcUid: String,
               mediaOptions: RteChannelMediaOptions, encryptionConfig: RteEncryptionConfig,
               @NonNull callback: RteCallback<Void>)

    fun setClientRole(uid: String, role: Int): Int

    fun setClientRole2(uid: String, role: Int): Int

    fun setRemoteRenderMode(uid: String, renderMode: Int, mirrorMode: Int): Int

    fun publish(uid: String): Int

    fun unPublish(uid: String): Int

    fun muteLocalAudioStream(uid: String, mute: Boolean): Int

    fun muteLocalVideoStream(uid: String, mute: Boolean): Int

    fun muteRemoteStream(uid: String, muteAudio: Boolean, muteVideo: Boolean): Int

    fun muteLocalStream(uid: String, muteAudio: Boolean, muteVideo: Boolean): Int

    fun setupRemoteVideo(curChannelLocalUid: String, canvas: RteVideoCanvas): Int

    fun setChannelMode(curChannelLocalUid: String, mode: Int): Int

    fun setVideoEncoderConfig(curChannelLocalUid: String, config: RteVideoEncoderConfig): Int

    fun setRemoteVideoStreamType(curChannelLocalUid: String, type: RteVideoStreamType): Int

    fun enableRemoteTrapezoidCorrectionEx(curChannelLocalUid: String, enabled: Boolean): Int

    fun setRemoteTrapezoidCorrectionOptionsEx(curChannelLocalUid: String,
                                              options: RteTrapezoidCorrectionOptions): Int

    fun getRemoteTrapezoidCorrectionOptionsEx(curChannelLocalUid: String): RteTrapezoidCorrectionOptions

    fun applyTrapezoidCorrectionToRemoteEx(curChannelLocalUid: String, enabled: Boolean): Int

    fun applyVideoEncoderMirrorToRemoteEx(curChannelLocalUid: String, mirrorModeValue: Int): Int

    fun applyBrightnessCorrectionToRemoteEx(curChannelLocalUid: String, enabled: Boolean, mode: RteBrightnessCorrectionMode): Int

    fun leave(callback: RteCallback<Unit>)

    fun leaveEx(uid: String, callback: RteCallback<Unit>)

    fun release()

    fun getRtcCallId(): String

}
