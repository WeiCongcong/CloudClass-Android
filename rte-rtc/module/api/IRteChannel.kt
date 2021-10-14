package io.agora.edu.core.internal.rte.module.api

import androidx.annotation.NonNull
import io.agora.edu.core.internal.rte.data.RteChannelMediaOptions
import io.agora.edu.core.internal.rte.data.RteEncryptionConfig
import io.agora.edu.core.internal.rte.data.RteVideoCanvas
import io.agora.edu.core.internal.rte.data.RteVideoStreamType
import io.agora.edu.core.internal.rte.listener.RteCallback
import io.agora.rtc.video.VideoCanvas

interface IRteChannel {

    fun join(rtcOptionalInfo: String, rtcToken: String, rtcUid: String,
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

    fun setRemoteVideoStreamType(curChannelLocalUid: String, type: RteVideoStreamType): Int

    fun leave(callback: RteCallback<Unit>)

    fun leaveEx(uid: String, callback: RteCallback<Unit>)

    fun release()

    fun getRtcCallId(): String

}
