package io.agora.edu.core.internal.rte.listener

import io.agora.edu.core.internal.rte.data.RteAudioVolumeInfo
import io.agora.edu.core.internal.rte.data.RteLocalVideoStats
import io.agora.edu.core.internal.rte.data.RteRemoteVideoStats
import io.agora.rtm.RtmChannelMember
import io.agora.rtm.RtmMessage

interface RteChannelEventListener {

    fun onUserJoined(uid: Int)

    fun onUserOffline(uid: Int)

    fun onRemoteVideoStateChanged(channelId: String, uid: Int, state: Int, reason: Int, elapsed: Int)

    fun onRemoteAudioStateChanged(channelId: String, uid: Int, state: Int, reason: Int, elapsed: Int)

    fun onRemoteVideoStats(stats: RteRemoteVideoStats)

    fun onLocalVideoStateChanged(localVideoState: Int, error: Int)

    fun onLocalAudioStateChanged(localVideoState: Int, error: Int)

    fun onLocalVideoStats(stats: RteLocalVideoStats)

    /**本地用户语音声音提示*/
    fun onAudioVolumeIndicationOfLocalSpeaker(speakers: Array<out RteAudioVolumeInfo>?, totalVolume: Int)

    /**远端用户语音声音提示*/
    fun onAudioVolumeIndicationOfRemoteSpeaker(speakers: Array<out RteAudioVolumeInfo>?, totalVolume: Int)

    /**网络质量发生改变*/
    fun onNetworkQuality(uid: Int, txQuality: Int, rxQuality: Int)

    /**收到频道内消息(包括频道内的聊天消息和各种房间配置、人员信息、流信息等)*/
    fun onChannelMsgReceived(p0: RtmMessage?, p1: RtmChannelMember?)
}