package io.agora.edu.core.internal.rte.module.impl

import android.content.Context
import android.util.Log
import io.agora.edu.core.internal.education.impl.Constants.Companion.AgoraLog
import io.agora.edu.core.internal.framework.data.EduCallback
import io.agora.edu.core.internal.framework.data.EduError
import io.agora.edu.core.internal.report.ReportManager
import io.agora.rtc2.IRtcEngineEventHandler
import io.agora.edu.core.internal.rte.data.*
import io.agora.edu.core.internal.rte.data.RteError.Companion.rtmError
import io.agora.edu.core.internal.rte.data.RteRegion.rtcRegion
import io.agora.edu.core.internal.rte.data.RteRegion.rtmRegion
import io.agora.edu.core.internal.rte.data.RteTrapezoidCorrectionOptions.Companion.convert
import io.agora.edu.core.internal.rte.listener.*
import io.agora.edu.core.internal.rte.module.RteEngine
import io.agora.edu.core.internal.rte.module.RteEngine.Companion.ok
import io.agora.edu.core.internal.rte.module.api.IRteChannel
import io.agora.edu.core.internal.rte.module.api.IRteEngine
import io.agora.rtc2.RtcEngineConfig
import io.agora.rtc2.RtcEngineEx
import io.agora.rtm.*
import io.agora.rtm.RtmStatusCode.LoginError.LOGIN_ERR_ALREADY_LOGIN
import io.agora.rtm.internal.RtmManager
import java.io.File

object RteEngineImpl : IRteEngine, IRtmServerDelegate {
    private val tag = RteEngineImpl::javaClass.name

    var eventListener: RteEngineEventListener? = null

    internal lateinit var rtmClient: RtmClient
    internal lateinit var rtcEngineEx: RtcEngineEx
    private val channelMap = mutableMapOf<String, IRteChannel>()
    private val channelLatencyMap = mutableMapOf<String, Int>()
    private var rtmLoginSuccess = false

    @Synchronized
    private fun setRtmLoginSuccess(success: Boolean) {
        rtmLoginSuccess = success
    }

    @Synchronized
    private fun rtmLoginSuccess(): Boolean {
        return rtmLoginSuccess
    }

    private val rtmClientListener = object : RtmClientListener {
        override fun onTokenExpired() {
        }

        override fun onPeersOnlineStatusChanged(p0: MutableMap<String, Int>?) {
        }

        override fun onConnectionStateChanged(p0: Int, p1: Int) {
            eventListener?.onConnectionStateChanged(p0, p1)
        }

        override fun onMessageReceived(p0: RtmMessage?, p1: String?) {
            eventListener?.onPeerMsgReceived(p0, p1)
        }

        override fun onMediaDownloadingProgress(p0: RtmMediaOperationProgress?, p1: Long) {
        }

        override fun onMediaUploadingProgress(p0: RtmMediaOperationProgress?, p1: Long) {
        }

        override fun onImageMessageReceivedFromPeer(p0: RtmImageMessage?, p1: String?) {
        }

        override fun onFileMessageReceivedFromPeer(p0: RtmFileMessage?, p1: String?) {
        }
    }
    private val rtcEngineEventListener = object : IRtcEngineEventHandler() {
    }

    override fun init(context: Context, appId: String, logFileDir: String, rtcRegion: String?,
                      rtmRegion: String?) {
        Log.i(tag, "init")
        var path = logFileDir.plus(File.separatorChar).plus("agorartm.log")
        val serviceContext = RtmServiceContext()
        serviceContext.areaCode = rtmRegion(rtmRegion)
        val code = RtmClient.setRtmServiceContext(serviceContext)
        rtmClient = RtmClient.createInstance(context, appId, rtmClientListener)
        rtmClient.setLogFile(path)

        path = logFileDir.plus(File.separatorChar).plus("agorasdk.log")

        val config = RtcEngineConfig()
        config.mContext = context
        config.mAppId = appId
        config.mAreaCode = rtcRegion(rtcRegion)
        config.mEventHandler = rtcEngineEventListener
        rtcEngineEx = RtcEngineEx.create(config) as RtcEngineEx
        rtcEngineEx.setLogFile(path)
    }

    override fun setRtcParameters(parameters: String): Int {
        return rtcEngineEx.setParameters(parameters)
    }

    override fun loginRtm(rtmUid: String, rtmToken: String, callback: RteCallback<Unit>) {
        val reporter = ReportManager.getRteReporter()
        reporter.reportRtmLoginStart()
        rtmClient.login(rtmToken, rtmUid, object : ResultCallback<Void> {
            override fun onSuccess(p0: Void?) {
                setRtmLoginSuccess(true)
                callback.onSuccess(Unit)
                reporter.reportRtmLoginResult("1", null, null)
            }

            override fun onFailure(p0: ErrorInfo?) {
                setRtmLoginSuccess(false)
                if (p0?.errorCode == LOGIN_ERR_ALREADY_LOGIN) {
                    callback.onSuccess(Unit)
                } else {
                    // release,Otherwise, rtmClient.setrtmservicecontext will fail
                    rtmClient.release()
                    callback.onFailure(rtmError(p0 ?: ErrorInfo(-1)))
                    reporter.reportRtmLoginResult("0", p0?.errorCode?.toString(), null)
                }
            }
        })
    }

    override fun logoutRtm() {
        rtmClient.logout(object : ResultCallback<Void> {
            override fun onSuccess(p0: Void?) {
                setRtmLoginSuccess(false)
                Log.i(tag, "rtm logout success")
            }

            override fun onFailure(p0: ErrorInfo?) {
                Log.i(tag, "rmt logout fail:${p0?.errorDescription}")
                if (p0?.errorCode == RtmStatusCode.LeaveChannelError.LEAVE_CHANNEL_ERR_USER_NOT_LOGGED_IN) {
                    setRtmLoginSuccess(false)
                }
            }
        })
    }

    override fun createChannel(channelId: String, eventListener: RteChannelEventListener,
                               mixingListener: RteAudioMixingListener): IRteChannel {
        val rteChannel = RteChannelImpl(rtcEngineEx, rtmClient, channelId, eventListener)
        rteChannel.audioMixingListener = mixingListener
        channelMap[channelId] = rteChannel
        return rteChannel
    }

    override fun getRtcCallId(id: String): String {
        return channelMap[id]?.getRtcCallId() ?: ""
    }

    override fun getRtmSessionId(): String {
        return RtmManager.getRtmSessionId(rtmClient)
    }

    override fun setLocalRenderMode(renderMode: Int, mirrorMode: Int): Int {
        return rtcEngineEx.setLocalRenderMode(renderMode, mirrorMode)
    }

    override fun setRemoteRenderMode(channelId: String, uid: String, renderMode: Int, mirrorMode: Int): Int {
        if (channelMap.isNotEmpty()) {
            return channelMap[channelId]?.setRemoteRenderMode(uid, renderMode, mirrorMode) ?: -1
        }
        return -1
    }

    operator fun get(channelId: String): IRteChannel? {
        return channelMap[channelId]
    }

    override fun setLatencyLevel(channelId: String, level: Int) {
        channelLatencyMap[channelId] = level
    }

    override fun setClientRole(channelId: String, uid: String, role: Int): Int {
        if (channelMap.isEmpty()) {
            AgoraLog.e("$tag:channelMap is Empty, failed to set client role->$role")
            return -1
        }
        val code = channelMap[channelId]?.setClientRole(uid, role) ?: -1
        if (code == 0) {
            AgoraLog.e("$tag:set client role success to:$role")
        } else {
            AgoraLog.e("$tag:failed to set client role->$role")
        }
        return code
    }

    override fun setClientRole2(channelId: String, uid: String, role: Int): Int {
        if (channelMap.isEmpty()) {
            AgoraLog.e("$tag:channelMap is Empty, failed to set2 client role->$role")
            return -1
        }
//        val options = ClientRoleOptions()
//        if (channelLatencyMap[channelId] == EduLatencyLevel.EduLatencyLevelLow.value &&
//                role == CLIENT_ROLE_AUDIENCE) {
//            options.audienceLatencyLevel = EduLatencyLevel.EduLatencyLevelLow.value
//        } else {
//            options.audienceLatencyLevel = EduLatencyLevel.EduLatencyLevelUltraLow.value
//        }
//        val code = (channelMap[channelId] as RteChannelImpl).rtcChannel.setClientRole(role,
//                options)
        val code = channelMap[channelId]?.setClientRole2(uid, role) ?: -1
        if (code == 0) {
            AgoraLog.e("$tag:set2 client role success to:$role")
        } else {
            AgoraLog.e("$tag:failed to set2 client role->$role")
        }
        return code
    }

    /** 1: at the moment, only used by preview.
     *  2: only works on the main channel */
    override fun setClientRole(role: Int): Int {
        val code = rtcEngineEx.setClientRole(role)
        if (code == 0) {
            Log.e(tag, "rtcEngine set client role success to:$role")
        }
        return code
    }

    override fun publish(channelId: String, uid: String): Int {
        if (channelMap.isNotEmpty()) {
            val code0 = channelMap[channelId]?.publish(uid) ?: -1
            if (code0 != ok()) {
                return code0
            }
            val code1 = startPreview()
            if (code1 != ok()) {
                return code1
            }
            return ok()
        }
        return -1
    }

    override fun unpublish(channelId: String, uid: String): Int {
        if (channelMap.isNotEmpty()) {
            val code0 = channelMap[channelId]?.unPublish(uid) ?: -1
            if (code0 != ok()) {
                return code0
            }
            val code1 = stopPreview()
            if (code1 != ok()) {
                return code1
            }
            return ok()
        }
        return -1
    }

    override fun updateLocalStream(channelId: String, uid: String, hasAudio: Boolean, hasVideo: Boolean): Int {
        val a = enableLocalAudio(hasAudio)
        if (a != ok()) {
            return a
        }
        val b = enableLocalVideo(hasVideo)
        if (b != ok()) {
            return b
        }
        val c = channelMap[channelId]?.muteLocalAudioStream(uid, !hasAudio) ?: -1
        if (c != ok()) {
            return c
        }
        val d = channelMap[channelId]?.muteLocalVideoStream(uid, !hasVideo) ?: -1
        if (d != ok()) {
            return d
        }
        return ok()
    }

    override fun updateLocalAudioStream(channelId: String, uid: String, hasAudio: Boolean): Int {
        val a = enableLocalAudio(hasAudio)
        if (a != ok()) {
            return a
        }

        val c = channelMap[channelId]?.muteLocalAudioStream(uid, !hasAudio) ?: -1
        if (c != ok()) {
            return c
        }
        return ok()
    }

    override fun updateLocalVideoStream(channelId: String, uid: String, hasVideo: Boolean): Int {
        val b = enableLocalVideo(hasVideo)
        if (b != ok()) {
            return b
        }

        val d = channelMap[channelId]?.muteLocalVideoStream(uid, !hasVideo) ?: -1
        if (d != ok()) {
            return d
        }
        return ok()
    }

    override fun muteRemoteStream(channelId: String, uid: String, muteAudio: Boolean, muteVideo: Boolean): Int {
        if (channelMap.isNotEmpty()) {
            return channelMap[channelId]?.muteRemoteStream(uid, muteAudio, muteVideo) ?: -1
        }
        return -1
    }

    override fun muteLocalStream(channelId: String, uid: String, muteAudio: Boolean, muteVideo: Boolean): Int {
        return channelMap[channelId]?.muteLocalStream(uid, muteAudio, muteVideo) ?: -1
    }

    override fun muteLocalAudioStream(channelId: String, uid: String, muteAudio: Boolean): Int {
        return channelMap[channelId]?.muteLocalAudioStream(uid, muteAudio) ?: -1
    }

    override fun muteLocalVideoStream(channelId: String, uid: String, muteVideo: Boolean): Int {
        return channelMap[channelId]?.muteLocalVideoStream(uid, muteVideo) ?: -1
    }

    override fun startPreview(): Int {
        return rtcEngineEx.startPreview()
    }

    override fun stopPreview(): Int {
        return rtcEngineEx.stopPreview()
    }

    override fun enableDualStreamMode(enabled: Boolean): Int {
        return rtcEngineEx.enableDualStreamMode(enabled)
    }

    override fun setRemoteVideoStreamType(channelId: String, uid: String, type: RteVideoStreamType): Int {
        return channelMap[channelId]?.setRemoteVideoStreamType(uid, type) ?: -1
    }

    override fun setVideoEncoderConfiguration(config: RteVideoEncoderConfig): Int {
        return rtcEngineEx.setVideoEncoderConfiguration(config.convertVideoEncoderConfig())
    }

    override fun setVideoEncoderConfiguration(channelId: String, uid: String, config: RteVideoEncoderConfig): Int {
        return channelMap[channelId]?.setVideoEncoderConfig(uid, config) ?: -1
    }

    override fun enableLocalVideo(enabled: Boolean): Int {
        return rtcEngineEx.enableLocalVideo(enabled)
    }

    override fun enableLocalAudio(enabled: Boolean): Int {
        return rtcEngineEx.enableLocalAudio(enabled)
    }

    // TODO
    override fun switchCamera(): Int {
        return rtcEngineEx.switchCamera()
    }

    override fun setupLocalVideo(local: RteVideoCanvas): Int {
        return rtcEngineEx.setupLocalVideo(local)
    }

    override fun setupRemoteVideo(channelId: String, curChannelLocalUid: String, canvas: RteVideoCanvas): Int {
        if (channelMap.isNotEmpty()) {
            return channelMap[channelId]?.setupRemoteVideo(curChannelLocalUid, canvas) ?: -1
        }
        return -1
    }

    override fun setEnableSpeakerphone(enabled: Boolean): Int {
        return rtcEngineEx.setEnableSpeakerphone(enabled)
    }

    override fun isSpeakerphoneEnabled(): Boolean {
        return rtcEngineEx.isSpeakerphoneEnabled
    }

    override fun startAudioMixing(filePath: String, loopback: Boolean, replace: Boolean, cycle: Int): Int {
        return rtcEngineEx.startAudioMixing(filePath, loopback, replace, cycle)
    }

    override fun enableLocalTrapezoidCorrection(enabled: Boolean): Int {
        return rtcEngineEx.enableLocalTrapezoidCorrection(enabled)
    }

    override fun setLocalTrapezoidCorrectionOptions(options: RteTrapezoidCorrectionOptions): Int {
        return rtcEngineEx.setLocalTrapezoidCorrectionOptions(options.convert())
    }

    override fun getLocalTrapezoidCorrectionOptions(): RteTrapezoidCorrectionOptions {
        return convert(rtcEngineEx.localTrapezoidCorrectionOptions)
    }

    override fun enableRemoteTrapezoidCorrectionEx(channelId: String, curChannelLocalUid: String,
                                                   enabled: Boolean): Int {
        if (channelMap.isNotEmpty()) {
            return channelMap[channelId]?.enableRemoteTrapezoidCorrectionEx(curChannelLocalUid, enabled)
                    ?: -1
        }
        return -1
    }

    override fun setRemoteTrapezoidCorrectionOptionsEx(channelId: String, curChannelLocalUid: String,
                                                       options: RteTrapezoidCorrectionOptions): Int {
        if (channelMap.isNotEmpty()) {
            return channelMap[channelId]?.setRemoteTrapezoidCorrectionOptionsEx(curChannelLocalUid, options)
                    ?: -1
        }
        return -1
    }

    override fun getRemoteTrapezoidCorrectionOptionsEx(channelId: String,
                                                       curChannelLocalUid: String): RteTrapezoidCorrectionOptions? {
        if (channelMap.isNotEmpty()) {
            return channelMap[channelId]?.getRemoteTrapezoidCorrectionOptionsEx(curChannelLocalUid)
        }
        return null
    }

    override fun applyTrapezoidCorrectionToRemoteEx(channelId: String, curChannelLocalUid: String,
                                                    enabled: Boolean): Int {
        if (channelMap.isNotEmpty()) {
            return channelMap[channelId]?.applyTrapezoidCorrectionToRemoteEx(curChannelLocalUid, enabled)
                    ?: -1
        }
        return -1
    }

    override fun applyVideoEncoderMirrorToRemoteEx(channelId: String, curChannelLocalUid: String, mirrorModeValue: Int): Int {
        if (channelMap.isNotEmpty()) {
            return channelMap[channelId]?.applyVideoEncoderMirrorToRemoteEx(curChannelLocalUid, mirrorModeValue)
                    ?: -1
        }
        return -1
    }

    override fun enableBrightnessCorrection(enabled: Boolean, mode: RteBrightnessCorrectionMode): Int {
        return rtcEngineEx.enableBrightnessCorrection(enabled, mode.value)
    }

    override fun applyBrightnessCorrectionToRemoteEx(channelId: String, curChannelLocalUid: String,
                                                     enabled: Boolean, mode: RteBrightnessCorrectionMode): Int {
        if (channelMap.isNotEmpty()) {
            return channelMap[channelId]?.applyBrightnessCorrectionToRemoteEx(curChannelLocalUid, enabled, mode)
                    ?: -1
        }
        return -1
    }

    // TODO 1
    override fun setAudioMixingPosition(pos: Int): Int {
        return rtcEngineEx.setAudioMixingPosition(pos)
    }

    override fun pauseAudioMixing(): Int {
        return rtcEngineEx.pauseAudioMixing()
    }

    override fun resumeAudioMixing(): Int {
        return rtcEngineEx.resumeAudioMixing()
    }

    override fun stopAudioMixing(): Int {
        return rtcEngineEx.stopAudioMixing()
    }

    override fun getAudioMixingDuration(): Int {
        return rtcEngineEx.audioMixingDuration
    }

    override fun getAudioMixingCurrentPosition(): Int {
        return rtcEngineEx.audioMixingCurrentPosition
    }

    override fun setLocalVoiceChanger(voiceManager: RteAudioVoiceChanger): Int {
        return rtcEngineEx.setLocalVoiceChanger(voiceManager.value)
    }

    override fun setLocalVoiceReverbPreset(preset: RteAudioReverbPreset): Int {
        return rtcEngineEx.setLocalVoiceReverbPreset(preset.value)
    }

    override fun enableInEarMonitoring(enabled: Boolean): Int {
        return rtcEngineEx.enableInEarMonitoring(enabled)
    }

    override fun enableAudioVolumeIndication(interval: Int, smooth: Int, report_vad: Boolean) {
        if (report_vad) {
            AgoraLog.w("$tag:enableAudioVolumeIndication->Currently, report_VAD is not supported!")
        }
        rtcEngineEx.enableAudioVolumeIndication(interval, smooth)
    }
    // TODO 1

    override fun setMediaDeviceListener(channelId: String, listener: RteMediaDeviceListener): Int {
        if (channelMap.isNotEmpty()) {
            val channel = channelMap[channelId] as RteChannelImpl
            channel.mediaDeviceListener = listener
            return 0
        }
        return -1
    }

    override fun setAudioMixingListener(channelId: String, listener: RteAudioMixingListener): Int {
        if (channelMap.isNotEmpty()) {
            val channel = channelMap[channelId] as RteChannelImpl
            channel.audioMixingListener = listener
            return 0
        }
        return -1
    }

    override fun setSpeakerReportListener(channelId: String, listener: RteSpeakerReportListener): Int {
        if (channelMap.isNotEmpty()) {
            val channel = channelMap[channelId] as RteChannelImpl
            channel.speakerReportListener = listener
            return 0
        }
        return -1
    }

    override fun setChannelMode(channelId: String, curChannelLocalUid: String, mode: Int): Int {
        if (channelMap.isNotEmpty()) {
            return channelMap[channelId]?.setChannelMode(curChannelLocalUid, mode) ?: -1
        }
        return -1
    }

    override fun getError(code: Int): String {
        return RteEngine.getErrorDescription(code)
    }

    override fun version(): String {
        return RteEngine.version()
    }

    override fun dispose() {
        rtmClient.release()
        RteEngine.destroy()
        channelMap.clear()
    }

    override fun sendRtmServerRequest(text: String, peerId: String,
                                      callback: EduCallback<Void>?): RtmServerRequestResult {
        if (!rtmLoginSuccess()) {
            return RtmServerRequestResult.RtmNotLogin
        }

        val options = SendMessageOptions()
        options.enableOfflineMessaging = false
        options.enableHistoricalMessaging = false

        rtmClient.sendMessageToPeer(peerId, rtmClient.createMessage(text),
                options, object : ResultCallback<Void> {
            override fun onSuccess(p0: Void?) {
                callback?.onSuccess(p0)
            }

            override fun onFailure(p0: ErrorInfo?) {
                callback?.onFailure(
                        if (p0 != null) EduError(p0.errorCode, p0.errorDescription)
                        else EduError(-1, ""))
            }
        })

        return RtmServerRequestResult.Success
    }

    override fun rtmServerPeerOnlineStatus(serverIdList: List<String>, callback: EduCallback<Map<String, Boolean>>?) {
        if (rtmLoginSuccess()) {
            rtmClient.queryPeersOnlineStatus(serverIdList.toSet(), object : ResultCallback<Map<String, Boolean>> {
                override fun onSuccess(p0: Map<String, Boolean>?) {
                    p0?.let {
                        callback?.onSuccess(it)
                    }
                }

                override fun onFailure(p0: ErrorInfo?) {
                    callback?.onFailure(EduError(p0?.errorCode ?: -1, p0?.errorDescription ?: ""))
                }
            })
        }
    }


    private fun uidConvert(uid: String): Int {
        return (uid.toLong() and 0xffffffffL).toInt()
    }
}
