package io.agora.agoraeducore.core.internal.rte.listener

interface RteAudioMixingListener {
    fun onAudioMixingFinished()

    fun onAudioMixingStateChanged(state: Int, errorCode: Int)
}