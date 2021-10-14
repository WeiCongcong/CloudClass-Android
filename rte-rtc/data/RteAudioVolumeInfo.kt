package io.agora.edu.core.internal.rte.data

import io.agora.rtc.IRtcEngineEventHandler

data class RteAudioVolumeInfo(
        val uid: Int,
        val userId: String?,
        val volume: Int,
        val vad: Int?,
        val channelId: String?) {

    companion object {
        fun convert(info: IRtcEngineEventHandler.AudioVolumeInfo): RteAudioVolumeInfo {
            return RteAudioVolumeInfo(info.uid, null, info.volume, info.vad, info.channelId)
        }

        fun convert(infos: Array<out IRtcEngineEventHandler.AudioVolumeInfo>):
                Array<RteAudioVolumeInfo> {
            val list = mutableListOf<RteAudioVolumeInfo>()
            infos.forEach {
                val element = RteAudioVolumeInfo(it.uid, null, it.volume, it.vad, it.channelId)
                list.add(element)
            }
            return list.toTypedArray()
        }
    }
}