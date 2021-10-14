package io.agora.edu.core.internal.rte.module.impl

import android.util.Log
import androidx.annotation.NonNull
import io.agora.edu.core.internal.education.impl.Constants.Companion.AgoraLog
import io.agora.edu.core.internal.report.ReportManager
import io.agora.edu.core.internal.rte.data.*
import io.agora.edu.core.internal.rte.data.RteError.Companion.rtcError
import io.agora.edu.core.internal.rte.data.RteError.Companion.rtmError
import io.agora.edu.core.internal.rte.listener.*
import io.agora.edu.core.internal.rte.module.RteEngine
import io.agora.edu.core.internal.rte.module.RteEngine.Companion.ok
import io.agora.edu.core.internal.rte.module.api.IRteChannel
import io.agora.rtc.IRtcChannelEventHandler
import io.agora.rtc.RtcChannel
import io.agora.rtc.RtcEngineEx
import io.agora.rtc.models.ClientRoleOptions
import io.agora.rtm.*
import io.agora.rtm.RtmStatusCode.JoinChannelError.JOIN_CHANNEL_ERR_ALREADY_JOINED
import java.util.*

internal class RteChannelImpl(
        private val rtcEngineEx: RtcEngineEx,
        rtmClient: RtmClient,
        private val channelId: String,
        private var eventListener: RteChannelEventListener?
) : IRteChannel {
    private val tag = RteChannelImpl::class.java.simpleName

    private var joinSuccessTrigger: JoinSuccessCountDownTrigger? = null
    private var timer: Timer? = Timer()
    private var videoFrozenStateCallbackTask: TimerTask? = null

    @Volatile
    private var videoStateBarrier = false

    private val rtmChannelListener = object : RtmChannelListener {
        override fun onAttributesUpdated(p0: MutableList<RtmChannelAttribute>?) {

        }

        override fun onMessageReceived(p0: RtmMessage?, p1: RtmChannelMember?) {
            Log.e(tag, "Receive channel ${p1?.channelId} message->${p0?.text}")
            eventListener?.onChannelMsgReceived(p0, p1)
        }

        override fun onMemberJoined(p0: RtmChannelMember?) {

        }

        override fun onMemberLeft(p0: RtmChannelMember?) {

        }

        override fun onMemberCountUpdated(p0: Int) {

        }

        override fun onFileMessageReceived(p0: RtmFileMessage?, p1: RtmChannelMember?) {
        }

        override fun onImageMessageReceived(p0: RtmImageMessage?, p1: RtmChannelMember?) {
        }
    }
    private val rtmChannel = rtmClient.createChannel(channelId, rtmChannelListener)
    private val rtcChannelEventHandler = object : IRtcChannelEventHandler() {
        override fun onChannelError(rtcChannel: RtcChannel?, err: Int) {
            super.onChannelError(rtcChannel, err)
            AgoraLog.e("$tag:onChannelError->channel:$channelId, err->$err, errStr${RteEngine.getErrorDescription(err)}")
        }

        override fun onChannelWarning(rtcChannel: RtcChannel?, warn: Int) {
            super.onChannelWarning(rtcChannel, warn)
            AgoraLog.w("$tag:onChannelWarning->channel:$channelId, err->$warn")
        }

        override fun onNetworkQuality(rtcChannel: RtcChannel?, uid: Int, txQuality: Int, rxQuality: Int) {
            super.onNetworkQuality(rtcChannel, uid, txQuality, rxQuality)
            eventListener?.onNetworkQuality(uid, txQuality, rxQuality)
        }

        override fun onClientRoleChanged(rtcChannel: RtcChannel?, oldRole: Int, newRole: Int) {
            super.onClientRoleChanged(rtcChannel, oldRole, newRole)
            AgoraLog.i("$tag:onClientRoleChanged->channel:$channelId, oldRole:$oldRole, newRole:$newRole")
        }

        override fun onJoinChannelSuccess(rtcChannel: RtcChannel?, uid: Int, elapsed: Int) {
            super.onJoinChannelSuccess(rtcChannel, uid, elapsed)
            AgoraLog.i("$tag:onJoinChannelSuccess->channel:$channelId, uid:$uid, elapsed:$elapsed")
            ReportManager.getRteReporter().reportRtcJoinResult("1", null, null)
            joinSuccessTrigger?.countDown()
        }

        override fun onUserJoined(rtcChannel: RtcChannel?, uid: Int, elapsed: Int) {
            super.onUserJoined(rtcChannel, uid, elapsed)
            AgoraLog.i("$tag:onUserJoined->channel:$channelId, uid:$uid, elapsed:$elapsed")
            eventListener?.onUserJoined(uid)
        }

        override fun onUserOffline(rtcChannel: RtcChannel?, uid: Int, reason: Int) {
            super.onUserOffline(rtcChannel, uid, reason)
            AgoraLog.i("$tag:onUserOffline->channel:$channelId, uid:$uid, reason:$reason")
            eventListener?.onUserOffline(uid)
        }

        override fun onRemoteVideoStateChanged(rtcChannel: RtcChannel?, uid: Int, state: Int, reason: Int, elapsed: Int) {
            super.onRemoteVideoStateChanged(rtcChannel, uid, state, reason, elapsed)
            AgoraLog.i("$tag:onRemoteVideoStateChanged->channel:$channelId, uid:$uid, " +
                    "state:${state}, reason:${reason}, reason:$reason")
            handleVideoState(uid, state, reason, elapsed)
        }

        override fun onRemoteAudioStateChanged(rtcChannel: RtcChannel?, uid: Int, state: Int, reason: Int, elapsed: Int) {
            super.onRemoteAudioStateChanged(rtcChannel, uid, state, reason, elapsed)
            AgoraLog.i("$tag:onRemoteAudioStateChanged->channel:$channelId, uid:$uid, " +
                    "state:${state}, reason:${reason}, reason:$reason")
            val videoState = RteRemoteAudioState.convert(state)
            val changeReason = RteRemoteAudioStateChangeReason.convert(reason)
            eventListener?.onRemoteAudioStateChanged(channelId, uid, videoState, changeReason, elapsed)
        }
    }
    private val rtcChannel = rtcEngineEx.createRtcChannel(channelId)

    var latencyLevel: Int = RteLatencyLevel.RteLatencyLevelUltraLow.value

    private fun handleVideoState(uid: Int, state: Int, reason: Int, elapsed: Int) {
        val videoState = RteRemoteVideoState.convert(state)
        val changeReason = RteRemoteVideoStateChangeReason.convert(reason)
        // Solves a small interval between 3 and 2 , resulting in video flickering issues
        if (videoState == RteRemoteVideoState.REMOTE_VIDEO_STATE_FROZEN.value) {
            videoStateBarrier = false
            videoFrozenStateCallbackTask = object : TimerTask() {
                override fun run() {
                    if (!videoStateBarrier) {
                        eventListener?.onRemoteVideoStateChanged(channelId, uid, videoState,
                                changeReason, elapsed)
                    }
                }
            }
            timer?.schedule(videoFrozenStateCallbackTask, 1000)
            return
        } else if (videoState == RteRemoteVideoState.REMOTE_VIDEO_STATE_DECODING.value) {
            videoStateBarrier = true
            videoFrozenStateCallbackTask?.cancel()
            timer?.purge()
            videoFrozenStateCallbackTask = null
        }
        eventListener?.onRemoteVideoStateChanged(channelId, uid, videoState, changeReason, elapsed)
    }

    init {
        rtcChannel.setRtcChannelEventHandler(rtcChannelEventHandler)
    }

    /**
     * encryptionConfig only works on the main channel
     * */
    override fun join(rtcOptionalInfo: String, rtcToken: String, rtcUid: String,
                      mediaOptions: RteChannelMediaOptions, encryptionConfig: RteEncryptionConfig,
                      callback: RteCallback<Void>) {
        synchronized(this) {
            if (joinSuccessTrigger != null) {
                Log.d(tag, "join has been called, rtc uid $rtcUid")
                return
            }

            joinSuccessTrigger = JoinSuccessCountDownTrigger(2, callback)
            ReportManager.getRteReporter().reportRtcJoinStart()
            if (encryptionConfig.encryptionKey != null) {
                rtcChannel.enableEncryption(true, encryptionConfig.convert())
            }
            val uid: Int = uidConvert(rtcUid)
            val rtcCode = rtcChannel.joinChannel(rtcToken, rtcOptionalInfo, uid, mediaOptions)
            joinRtmChannel(rtcCode, callback)
        }
    }

    private fun joinRtmChannel(rtcCode: Int, @NonNull callback: RteCallback<Void>) {
        synchronized(this) {
            if (joinSuccessTrigger == null || joinSuccessTrigger?.countDownFinished() == true) {
                Log.d(tag, "join has been called, rtm channel ${rtmChannel.id}")
                return
            }
        }

        val reporter = ReportManager.getRteReporter()
        reporter.reportRtmJoinStart()
        rtmChannel.join(object : ResultCallback<Void> {
            override fun onSuccess(p0: Void?) {
                if (rtcCode == ok()) {
                    reporter.reportRtmJoinResult("1", null, null)
                    joinSuccessTrigger?.countDown()
                } else {
                    callback.onFailure(rtcError(rtcCode))
                    reporter.reportRtmJoinResult("0", rtcCode.toString(), null)
                }
            }

            override fun onFailure(p0: ErrorInfo?) {
                if (p0?.errorCode == JOIN_CHANNEL_ERR_ALREADY_JOINED) {
                    Log.i(tag, "rtm already logged in")
                    reporter.reportRtmJoinResult("1", null, null)
                    joinSuccessTrigger?.countDown()
                } else {
                    callback.onFailure(rtmError(p0 ?: ErrorInfo(-1)))
                    reporter.reportRtmJoinResult("0", p0?.errorCode.toString(), null)
                }
            }
        })
    }

    /** only setClientRole */
    override fun setClientRole(uid: String, role: Int): Int {
        return rtcChannel.setClientRole(role)
    }

    /**
     * 1: setClientRole
     * 2: setLatencyLevel
     * */
    override fun setClientRole2(uid: String, role: Int): Int {
        val options = ClientRoleOptions()
        if (latencyLevel == RteLatencyLevel.RteLatencyLevelLow.value &&
                role == RteClientRole.CLIENT_ROLE_AUDIENCE) {
            options.audienceLatencyLevel = RteLatencyLevel.RteLatencyLevelLow.value
        } else {
            options.audienceLatencyLevel = RteLatencyLevel.RteLatencyLevelUltraLow.value
        }
        return rtcChannel.setClientRole(role, options)
    }

    override fun setRemoteRenderMode(uid: String, renderMode: Int, mirrorMode: Int): Int {
        val rtcUid = uidConvert(uid)
        return rtcChannel.setRemoteRenderMode(rtcUid, renderMode, mirrorMode)
    }

    override fun publish(uid: String): Int {
        return rtcChannel.publish()
    }

    override fun unPublish(uid: String): Int {
        return rtcChannel.unpublish()
    }

    override fun muteLocalAudioStream(uid: String, mute: Boolean): Int {
        return rtcChannel.muteLocalAudioStream(mute)
    }

    override fun muteLocalVideoStream(uid: String, mute: Boolean): Int {
        return rtcChannel.muteLocalVideoStream(mute)
    }

    override fun muteRemoteStream(uid: String, muteAudio: Boolean, muteVideo: Boolean): Int {
        val rtcUid: Int = uidConvert(uid)
        val code0 = rtcChannel.muteRemoteAudioStream(rtcUid, muteAudio)
        val code1 = rtcChannel.muteRemoteVideoStream(rtcUid, muteVideo)
        return if (code0 == ok() && code1 == ok()) ok() else -1
    }

    override fun muteLocalStream(uid: String, muteAudio: Boolean, muteVideo: Boolean): Int {
        val code0 = rtcChannel.muteLocalAudioStream(muteAudio)
        val code1 = rtcChannel.muteLocalVideoStream(muteVideo)
        return if (code0 == ok() && code1 == ok()) ok() else -1
    }

    override fun setupRemoteVideo(curChannelLocalUid: String, canvas: RteVideoCanvas): Int {
        return rtcEngineEx.setupRemoteVideo(canvas)
    }

    override fun setChannelMode(curChannelLocalUid: String, mode: Int): Int {
        return rtcEngineEx.setChannelProfile(mode)
    }

    override fun setRemoteVideoStreamType(curChannelLocalUid: String, type: RteVideoStreamType): Int {
        val rtcUid: Int = uidConvert(curChannelLocalUid)
        return rtcEngineEx.setRemoteVideoStreamType(rtcUid, type.value)
    }

    /**
     * called when one stream exit
     * */
    @Deprecated(message = "not implementation")
    override fun leaveEx(uid: String, callback: RteCallback<Unit>) {
    }

    /**
     * called when exit channel
     * */
    override fun leave(callback: RteCallback<Unit>) {
        val rtcCode = rtcChannel.leaveChannel()
        Log.e(tag, if (rtcCode == ok()) "leave rtc channel success" else "leave rtc channel fail" +
                "->code:$rtcCode")
        rtmChannel.leave(object : ResultCallback<Void> {
            override fun onSuccess(p0: Void?) {
                Log.e(tag, "leave rtm channel success")
                if (rtcCode == ok()) {
                    callback.onSuccess(Unit)
                } else {
                    callback.onFailure(rtcError(rtcCode))
                }
            }

            override fun onFailure(p0: ErrorInfo?) {
                Log.e(tag, "leave rtm channel fail: ${p0?.errorDescription}")
                callback.onFailure(rtmError(p0 ?: ErrorInfo(-1)))
            }
        })
        eventListener = null
        videoFrozenStateCallbackTask?.cancel()
        timer?.purge()
        videoFrozenStateCallbackTask = null
        timer?.cancel()
        timer = null
    }

    override fun release() {
        rtmChannel.release()
        rtcChannel.destroy()
    }

    override fun getRtcCallId(): String {
        return rtcChannel.callId
    }

    private fun uidConvert(uid: String): Int {
        return (uid.toLong() and 0xffffffffL).toInt()
    }

    private inner class JoinSuccessCountDownTrigger(
            private var countDown: Int,
            private val callback: RteCallback<Void>) {

        @Synchronized
        fun countDown() {
            if (countDown == 0) {
                Log.d("JoinSuccessTrigger0", "latch has been counted down to zero, callback is invoked.")
                return
            }
            countDown--
            Log.d("JoinSuccessTrigger", "countdown to $countDown")
            if (countDown == 0) {
                Log.d("JoinSuccessTrigger1", "latch has been counted down to zero, callback is invoked.")
                callback.onSuccess(null)
            }
        }

        @Synchronized
        fun countDownFinished(): Boolean {
            return countDown == 0
        }
    }
}
