package io.agora.edu.core.internal.rte.data

import io.agora.rtc2.IRtcEngineEventHandler


enum class RteRemoteAudioState(val value: Int) {
    REMOTE_AUDIO_STATE_STOPPED(0),
    REMOTE_AUDIO_STATE_STARTING(1),
    REMOTE_AUDIO_STATE_DECODING(2),
    REMOTE_AUDIO_STATE_FROZEN(3),
    REMOTE_AUDIO_STATE_FAILED(4);

    companion object {
        fun convert(value: IRtcEngineEventHandler.REMOTE_AUDIO_STATE?): Int {
            return when (value) {
                IRtcEngineEventHandler.REMOTE_AUDIO_STATE.REMOTE_AUDIO_STATE_STOPPED -> {
                    REMOTE_AUDIO_STATE_STOPPED.value
                }
                IRtcEngineEventHandler.REMOTE_AUDIO_STATE.REMOTE_AUDIO_STATE_STARTING -> {
                    REMOTE_AUDIO_STATE_STARTING.value
                }
                IRtcEngineEventHandler.REMOTE_AUDIO_STATE.REMOTE_AUDIO_STATE_DECODING -> {
                    REMOTE_AUDIO_STATE_DECODING.value
                }
                IRtcEngineEventHandler.REMOTE_AUDIO_STATE.REMOTE_AUDIO_STATE_FROZEN -> {
                    REMOTE_AUDIO_STATE_FROZEN.value
                }
                IRtcEngineEventHandler.REMOTE_AUDIO_STATE.REMOTE_AUDIO_STATE_FAILED -> {
                    REMOTE_AUDIO_STATE_FAILED.value
                }
                else -> {
                    REMOTE_AUDIO_STATE_STOPPED.value
                }
            }
        }
    }
}

enum class RteRemoteAudioStateChangeReason(val value: Int) {
    REMOTE_AUDIO_STATE_REASON_INTERNAL(0),
    REMOTE_AUDIO_STATE_REASON_NETWORK_CONGESTION(1),
    REMOTE_AUDIO_STATE_REASON_NETWORK_RECOVERY(2),
    REMOTE_AUDIO_STATE_REASON_LOCAL_MUTED(3),
    REMOTE_AUDIO_STATE_REASON_LOCAL_UNMUTED(4),
    REMOTE_AUDIO_STATE_REASON_REMOTE_MUTED(5),
    REMOTE_AUDIO_STATE_REASON_REMOTE_UNMUTED(6),
    REMOTE_AUDIO_STATE_REASON_REMOTE_OFFLINE(7);

    companion object {
        fun convert(value: IRtcEngineEventHandler.REMOTE_AUDIO_STATE_REASON?): Int {
            return when (value) {
                IRtcEngineEventHandler.REMOTE_AUDIO_STATE_REASON.REMOTE_AUDIO_REASON_INTERNAL -> {
                    REMOTE_AUDIO_STATE_REASON_INTERNAL.value
                }
                IRtcEngineEventHandler.REMOTE_AUDIO_STATE_REASON.REMOTE_AUDIO_REASON_NETWORK_CONGESTION -> {
                    REMOTE_AUDIO_STATE_REASON_NETWORK_CONGESTION.value
                }
                IRtcEngineEventHandler.REMOTE_AUDIO_STATE_REASON.REMOTE_AUDIO_REASON_NETWORK_RECOVERY -> {
                    REMOTE_AUDIO_STATE_REASON_NETWORK_RECOVERY.value
                }
                IRtcEngineEventHandler.REMOTE_AUDIO_STATE_REASON.REMOTE_AUDIO_REASON_LOCAL_MUTED -> {
                    REMOTE_AUDIO_STATE_REASON_LOCAL_MUTED.value
                }
                IRtcEngineEventHandler.REMOTE_AUDIO_STATE_REASON.REMOTE_AUDIO_REASON_LOCAL_UNMUTED -> {
                    REMOTE_AUDIO_STATE_REASON_LOCAL_UNMUTED.value
                }
                IRtcEngineEventHandler.REMOTE_AUDIO_STATE_REASON.REMOTE_AUDIO_REASON_REMOTE_MUTED -> {
                    REMOTE_AUDIO_STATE_REASON_REMOTE_MUTED.value
                }
                IRtcEngineEventHandler.REMOTE_AUDIO_STATE_REASON.REMOTE_AUDIO_REASON_REMOTE_UNMUTED -> {
                    REMOTE_AUDIO_STATE_REASON_REMOTE_UNMUTED.value
                }
                IRtcEngineEventHandler.REMOTE_AUDIO_STATE_REASON.REMOTE_AUDIO_REASON_REMOTE_OFFLINE -> {
                    REMOTE_AUDIO_STATE_REASON_REMOTE_OFFLINE.value
                }
                else -> {
                    REMOTE_AUDIO_STATE_REASON_INTERNAL.value
                }
            }
        }
    }
}