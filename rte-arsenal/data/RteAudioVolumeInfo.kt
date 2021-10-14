package io.agora.edu.core.internal.rte.data

import io.agora.rtc2.IRtcEngineEventHandler

data class RteAudioVolumeInfo(
        val uid: Int,
        val userId: String?,
        val volume: Int,
        val vad: Int?,
        val channelId: String?) {

    companion object {
        fun convert(info: IRtcEngineEventHandler.AudioVolumeInfo): RteAudioVolumeInfo {
            return RteAudioVolumeInfo(info.uid, info.userId, info.volume, null, null)
        }

        fun convert(infos: Array<out IRtcEngineEventHandler.AudioVolumeInfo>):
                Array<RteAudioVolumeInfo> {
            val list = mutableListOf<RteAudioVolumeInfo>()
            infos.forEach {
                val element = RteAudioVolumeInfo(it.uid, it.userId, it.volume, null, null)
                list.add(element)
            }
            return list.toTypedArray()
        }
    }
}