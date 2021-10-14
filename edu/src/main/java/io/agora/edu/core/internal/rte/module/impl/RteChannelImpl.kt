package io.agora.edu.core.internal.rte.module.impl

import android.util.Log
import androidx.annotation.NonNull
import io.agora.edu.core.internal.education.impl.Constants.Companion.AgoraLog
import io.agora.edu.core.internal.report.ReportManager
import io.agora.edu.core.internal.rte.data.*
import io.agora.edu.core.internal.rte.data.RteAudioVolumeInfo.Companion.convert
import io.agora.edu.core.internal.rte.data.RteTrapezoidCorrectionOptions.Companion.convert
import io.agora.edu.core.internal.rte.data.RteError.Companion.rtcError
import io.agora.edu.core.internal.rte.data.RteError.Companion.rtmError
import io.agora.edu.core.internal.rte.listener.*
import io.agora.edu.core.internal.rte.module.RteEngine.Companion.ok
import io.agora.edu.core.internal.rte.module.api.IRteChannel
import io.agora.rtc2.*
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
    private var channelConnection: MutableMap<String, RtcConnection> = mutableMapOf()

    internal var mediaDeviceListener: RteMediaDeviceListener? = null
    internal var audioMixingListener: RteAudioMixingListener? = null
    internal var speakerReportListener: RteSpeakerReportListener? = null

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

    private val rtcEngineEventHandler = object : IRtcEngineEventHandler() {
        override fun onWarning(warn: Int) {
            super.onWarning(warn)
            AgoraLog.w("$tag:onChannelWarning->channel:$channelId, err->$warn")
        }

        override fun onError(err: Int) {
            super.onError(err)
            AgoraLog.e("$tag:onChannelError->channel:$channelId, err->$err, errStr${RtcEngineEx.getErrorDescription(err)}")
        }

        override fun onNetworkQuality(uid: Int, txQuality: Int, rxQuality: Int) {
            super.onNetworkQuality(uid, txQuality, rxQuality)
            eventListener?.onNetworkQuality(uid, txQuality, rxQuality)
        }

        override fun onClientRoleChanged(oldRole: Int, newRole: Int) {
            super.onClientRoleChanged(oldRole, newRole)
            AgoraLog.i("$tag:onClientRoleChanged->channel:$channelId, oldRole:$oldRole, newRole:$newRole")
        }

        override fun onJoinChannelSuccess(channel: String?, uid: Int, elapsed: Int) {
            super.onJoinChannelSuccess(channel, uid, elapsed)
            AgoraLog.i("$tag:onJoinChannelSuccess->channel:$channel, uid:$uid, elapsed:$elapsed")
            ReportManager.getRteReporter().reportRtcJoinResult("1", null, null)
            joinSuccessTrigger?.countDown()
        }

        override fun onUserJoined(uid: Int, elapsed: Int) {
            super.onUserJoined(uid, elapsed)
            AgoraLog.i("$tag:onUserJoined->channel:$channelId, uid:$uid, elapsed:$elapsed")
            eventListener?.onUserJoined(uid)
        }

        override fun onUserOffline(uid: Int, reason: Int) {
            super.onUserOffline(uid, reason)
            AgoraLog.i("$tag:onUserOffline->channel:$channelId, uid:$uid, reason:$reason")
            eventListener?.onUserOffline(uid)
        }

        override fun onRemoteVideoStateChanged(uid: Int, state: Int, reason: Int, elapsed: Int) {
            super.onRemoteVideoStateChanged(uid, state, reason, elapsed)
            AgoraLog.i("$tag:onRemoteVideoStateChanged->channel:$channelId, uid:$uid, " +
                    "state:$state, reason:$reason")
            handleVideoState(uid, state, reason, elapsed)
        }

        override fun onRemoteAudioStateChanged(uid: Int, state: REMOTE_AUDIO_STATE?, reason: REMOTE_AUDIO_STATE_REASON?, elapsed: Int) {
            super.onRemoteAudioStateChanged(uid, state, reason, elapsed)
            AgoraLog.i("$tag:onRemoteAudioStateChanged->channel:$channelId, uid:$uid, " +
                    "state:${state?.name}, reason:${reason?.name}, reason:$reason")
            val audioState = RteRemoteAudioState.convert(state)
            val changeReason = RteRemoteAudioStateChangeReason.convert(reason)
            eventListener?.onRemoteAudioStateChanged(channelId, uid, audioState, changeReason, elapsed)
        }

        override fun onAudioRouteChanged(routing: Int) {
            super.onAudioRouteChanged(routing)
            AgoraLog.i("$tag:onAudioRouteChanged->routing:$routing")
            mediaDeviceListener?.onAudioRouteChanged(routing)
        }

        override fun onAudioMixingFinished() {
            super.onAudioMixingFinished()
            AgoraLog.i("$tag:onAudioMixingFinished")
            audioMixingListener?.onAudioMixingFinished()
        }

        override fun onAudioMixingStateChanged(state: Int, errorCode: Int) {
            super.onAudioMixingStateChanged(state, errorCode)
            AgoraLog.i("$tag:onAudioMixingFinished->state:$state, errorCode:$errorCode")
            audioMixingListener?.onAudioMixingStateChanged(state, errorCode)
        }

        override fun onAudioVolumeIndication(speakers: Array<out AudioVolumeInfo>?, totalVolume: Int) {
            super.onAudioVolumeIndication(speakers, totalVolume)
            speakers?.let {
                val rteSpeakers = convert(speakers)
                if (speakers.size == 1 && speakers[0].uid == 0) {
                    speakerReportListener?.onAudioVolumeIndicationOfLocalSpeaker(rteSpeakers, totalVolume)
                    eventListener?.onAudioVolumeIndicationOfLocalSpeaker(rteSpeakers, totalVolume)
                } else {
                    speakerReportListener?.onAudioVolumeIndicationOfRemoteSpeaker(rteSpeakers, totalVolume)
                    eventListener?.onAudioVolumeIndicationOfRemoteSpeaker(rteSpeakers, totalVolume)
                }
            }
        }

        override fun onLocalVideoStats(stats: LocalVideoStats?) {
            super.onLocalVideoStats(stats)
            stats?.let { sta ->
                val rteLocalVideoStats = RteLocalVideoStats.convert(sta)
                eventListener?.onLocalVideoStats(rteLocalVideoStats)
            }
        }

        override fun onLocalVideoStateChanged(localVideoState: Int, error: Int) {
            super.onLocalVideoStateChanged(localVideoState, error)
            val state = RteLocalVideoState.convert(localVideoState)
            val err = RteLocalVideoError.convert(error)
            eventListener?.onLocalVideoStateChanged(state, err)
        }

        override fun onLocalAudioStateChanged(state: LOCAL_AUDIO_STREAM_STATE, error: LOCAL_AUDIO_STREAM_ERROR) {
            super.onLocalAudioStateChanged(state, error)
            val state = RteLocalAudioState.convert(state)
            val err = RteLocalAudioError.convert(error)
            eventListener?.onLocalAudioStateChanged(state, err)
        }
    }

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
        } else if (videoState == RteRemoteVideoState.REMOTE_VIDEO_STATE_PLAYING.value) {
            videoStateBarrier = true
            videoFrozenStateCallbackTask?.cancel()
            timer?.purge()
            videoFrozenStateCallbackTask = null
        }
        eventListener?.onRemoteVideoStateChanged(channelId, uid, videoState, changeReason, elapsed)
    }

    private fun genChannelConnectionKey(uid: String): String {
        return channelId.plus("_").plus(uid.toString())
    }

    /**
     * encryptionConfig only works on the main channel
     * */
    override fun joinEx(rtcOptionalInfo: String, rtcToken: String, rtcUid: String,
                        mediaOptions: RteChannelMediaOptions, encryptionConfig: RteEncryptionConfig,
                        callback: RteCallback<Void>) {
        if (encryptionConfig.encryptionKey != null) {
            rtcEngineEx.enableEncryption(true, encryptionConfig.convert())
        }
        // generate channelConnectionKey and rtcConnection, bind key and rtcConnection
        val key = genChannelConnectionKey(rtcUid)
        val uid: Int = uidConvert(rtcUid)
        val rtcConnection = RtcConnection(channelId, uid)
        channelConnection[key] = rtcConnection
        val rtcCode = rtcEngineEx.joinChannelEx(rtcToken, rtcConnection, mediaOptions.convert(),
                rtcEngineEventHandler)
        if (rtcCode == ok()) {
            callback.onSuccess(null)
        } else {
            callback.onFailure(rtcError(rtcCode))
        }
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
                rtcEngineEx.enableEncryption(true, encryptionConfig.convert())
            }
            // generate channelConnectionKey and rtcConnection, bind key and rtcConnection
            val key = genChannelConnectionKey(rtcUid)
            val uid: Int = uidConvert(rtcUid)
            val rtcConnection = RtcConnection(channelId, uid)
            channelConnection[key] = rtcConnection
            val rtcCode = rtcEngineEx.joinChannelEx(rtcToken, rtcConnection, mediaOptions.convert(),
                    rtcEngineEventHandler)
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
        val key = genChannelConnectionKey(uid)
        val connection = channelConnection[key]
        val mediaOptions = ChannelMediaOptions()
        mediaOptions.clientRoleType = role
        return rtcEngineEx.updateChannelMediaOptionsEx(mediaOptions, connection)
    }

    /**
     * 1: setClientRole
     * 2: setLatencyLevel
     * */
    override fun setClientRole2(uid: String, role: Int): Int {
        // todo "arsenal not yet supported"
        return Int.MIN_VALUE
    }

    override fun setRemoteRenderMode(uid: String, renderMode: Int, mirrorMode: Int): Int {
        val key = genChannelConnectionKey(uid)
        val connection = channelConnection[key]
        val rtcUid: Int = uidConvert(uid)
        return rtcEngineEx.setRemoteRenderModeEx(rtcUid, renderMode, mirrorMode, connection)
    }

    override fun publish(uid: String): Int {
        val key = genChannelConnectionKey(uid)
        val connection = channelConnection[key]
        val mediaOptions = ChannelMediaOptions()
        mediaOptions.publishCameraTrack = true
        mediaOptions.publishAudioTrack = true
        return rtcEngineEx.updateChannelMediaOptionsEx(mediaOptions, connection)
    }

    override fun unPublish(uid: String): Int {
        val key = genChannelConnectionKey(uid)
        val connection = channelConnection[key]
        val mediaOptions = ChannelMediaOptions()
        mediaOptions.publishCameraTrack = false
        mediaOptions.publishAudioTrack = false
        return rtcEngineEx.updateChannelMediaOptionsEx(mediaOptions, connection)
    }

    override fun muteLocalAudioStream(uid: String, mute: Boolean): Int {
        val key = genChannelConnectionKey(uid)
        val connection = channelConnection[key]
        val mediaOptions = ChannelMediaOptions()
        mediaOptions.publishAudioTrack = !mute
        return rtcEngineEx.updateChannelMediaOptionsEx(mediaOptions, connection)
    }

    override fun muteLocalVideoStream(uid: String, mute: Boolean): Int {
        val key = genChannelConnectionKey(uid)
        val connection = channelConnection[key]
        val mediaOptions = ChannelMediaOptions()
        mediaOptions.publishCameraTrack = !mute
        return rtcEngineEx.updateChannelMediaOptionsEx(mediaOptions, connection)
    }

    override fun muteRemoteStream(uid: String, muteAudio: Boolean, muteVideo: Boolean): Int {
        val key = genChannelConnectionKey(uid)
        val connection = channelConnection[key]
        val rtcUid: Int = uidConvert(uid)
        val code0 = rtcEngineEx.muteRemoteAudioStreamEx(rtcUid, muteAudio, connection)
        val code1 = rtcEngineEx.muteRemoteVideoStreamEx(rtcUid, muteVideo, connection)
        return if (code0 == ok() && code1 == ok()) ok() else -1
    }

    override fun muteLocalStream(uid: String, muteAudio: Boolean, muteVideo: Boolean): Int {
        val key = genChannelConnectionKey(uid)
        val connection = channelConnection[key]
        val mediaOptions = ChannelMediaOptions()
        mediaOptions.publishAudioTrack = !muteAudio
        mediaOptions.publishCameraTrack = !muteVideo
        return rtcEngineEx.updateChannelMediaOptionsEx(mediaOptions, connection)
    }

    override fun setupRemoteVideo(curChannelLocalUid: String, canvas: RteVideoCanvas): Int {
        val key = genChannelConnectionKey(curChannelLocalUid)
        val connection = channelConnection[key]
        return rtcEngineEx.setupRemoteVideoEx(canvas, connection)
    }

    override fun setChannelMode(curChannelLocalUid: String, mode: Int): Int {
        val key = genChannelConnectionKey(curChannelLocalUid)
        val connection = channelConnection[key]
        val mediaOptions = ChannelMediaOptions()
        mediaOptions.channelProfile = mode
        return rtcEngineEx.updateChannelMediaOptionsEx(mediaOptions, connection)
    }

    override fun setVideoEncoderConfig(curChannelLocalUid: String, config: RteVideoEncoderConfig): Int {
        val key = genChannelConnectionKey(curChannelLocalUid)
        val connection = channelConnection[key]
        return rtcEngineEx.setVideoEncoderConfigurationEx(config.convertVideoEncoderConfig(), connection)
    }

    override fun setRemoteVideoStreamType(curChannelLocalUid: String, type: RteVideoStreamType): Int {
        val key = genChannelConnectionKey(curChannelLocalUid)
        val connection = channelConnection[key]
        val mediaOptions = ChannelMediaOptions()
        mediaOptions.defaultVideoStreamType = type.value
        return rtcEngineEx.updateChannelMediaOptionsEx(mediaOptions, connection)
    }

    override fun enableRemoteTrapezoidCorrectionEx(curChannelLocalUid: String, enabled: Boolean): Int {
        val key = genChannelConnectionKey(curChannelLocalUid)
        val connection = channelConnection[key]
        val rtcUid: Int = uidConvert(curChannelLocalUid)
        return rtcEngineEx.enableRemoteTrapezoidCorrectionEx(rtcUid, enabled, connection)
    }

    override fun setRemoteTrapezoidCorrectionOptionsEx(curChannelLocalUid: String,
                                                       options: RteTrapezoidCorrectionOptions): Int {
        val key = genChannelConnectionKey(curChannelLocalUid)
        val connection = channelConnection[key]
        val rtcUid: Int = uidConvert(curChannelLocalUid)
        return rtcEngineEx.setRemoteTrapezoidCorrectionOptionsEx(rtcUid, options.convert(), connection)
    }

    override fun getRemoteTrapezoidCorrectionOptionsEx(curChannelLocalUid: String): RteTrapezoidCorrectionOptions {
        val key = genChannelConnectionKey(curChannelLocalUid)
        val connection = channelConnection[key]
        val rtcUid: Int = uidConvert(curChannelLocalUid)
        return convert(rtcEngineEx.getRemoteTrapezoidCorrectionOptionsEx(rtcUid, connection))
    }

    override fun applyTrapezoidCorrectionToRemoteEx(curChannelLocalUid: String, enabled: Boolean): Int {
        val key = genChannelConnectionKey(curChannelLocalUid)
        val connection = channelConnection[key]
        val rtcUid: Int = uidConvert(curChannelLocalUid)
        return rtcEngineEx.applyTrapezoidCorrectionToRemoteEx(rtcUid, enabled, connection)
    }

    override fun applyVideoEncoderMirrorToRemoteEx(curChannelLocalUid: String, mirrorModeValue: Int): Int {
        val key = genChannelConnectionKey(curChannelLocalUid)
        val connection = channelConnection[key]
        val rtcUid: Int = uidConvert(curChannelLocalUid)
        return rtcEngineEx.applyVideoEncoderMirrorToRemoteEx(rtcUid, mirrorModeValue, connection)
    }

    override fun applyBrightnessCorrectionToRemoteEx(curChannelLocalUid: String, enabled: Boolean,
                                                     mode: RteBrightnessCorrectionMode): Int {
        val key = genChannelConnectionKey(curChannelLocalUid)
        val connection = channelConnection[key]
        val rtcUid: Int = uidConvert(curChannelLocalUid)
        return rtcEngineEx.applyBrightnessCorrectionToRemoteEx(rtcUid, enabled, mode.value, connection)
    }

    // called when one stream exit
    override fun leaveEx(uid: String, callback: RteCallback<Unit>) {
        val key = genChannelConnectionKey(uid)
        val connection = channelConnection[key]
        val rtcCode = rtcEngineEx.leaveChannelEx(connection)
        if (rtcCode == ok()) {
            callback.onSuccess(Unit)
        } else {
            callback.onFailure(rtcError(rtcCode))
        }
    }

    // called when completely exit channel
    override fun leave(callback: RteCallback<Unit>) {
        val rtcCode = rtcEngineEx.leaveChannel(LeaveChannelOptions())
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
        channelConnection.clear()
        mediaDeviceListener = null
        audioMixingListener = null
        speakerReportListener = null
    }

    override fun release() {
        rtmChannel.release()
    }

    override fun getRtcCallId(): String {
//        return rtcChannel.callId
        return rtcEngineEx.callId ?: ""
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
