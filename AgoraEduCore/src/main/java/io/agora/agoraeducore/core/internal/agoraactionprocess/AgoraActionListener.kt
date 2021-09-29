package io.agora.agoraeducore.core.internal.agoraactionprocess

interface AgoraActionListener {
    fun onApply(actionMsgRes: AgoraActionMsgRes)

    fun onInvite(actionMsgRes: AgoraActionMsgRes)

    fun onAccept(actionMsgRes: AgoraActionMsgRes)

    fun onReject(actionMsgRes: AgoraActionMsgRes)

    fun onCancel(actionMsgRes: AgoraActionMsgRes)
}