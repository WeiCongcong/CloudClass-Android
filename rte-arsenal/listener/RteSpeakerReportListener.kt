package io.agora.edu.core.internal.rte.listener

import io.agora.edu.core.internal.rte.data.RteAudioVolumeInfo
import io.agora.rtc2.IRtcEngineEventHandler

interface RteSpeakerReportListener {
    fun onAudioVolumeIndicationOfLocalSpeaker(speakers: Array<out RteAudioVolumeInfo>?, totalVolume: Int)

    fun onAudioVolumeIndicationOfRemoteSpeaker(speakers: Array<out RteAudioVolumeInfo>?, totalVolume: Int)
}