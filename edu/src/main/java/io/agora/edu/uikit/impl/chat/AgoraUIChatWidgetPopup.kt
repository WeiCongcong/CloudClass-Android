package io.agora.edu.uikit.impl.chat

import android.graphics.Rect
import android.view.ViewGroup

class AgoraUIChatWidgetPopup : ChatPopupWidget() {
    override fun init(parent: ViewGroup, width: Int, height: Int, top: Int, left: Int) {

    }

    override fun getLayout(): ViewGroup? {
        return null
    }

    override fun receive(fromCompId: String, cmd: String, vararg: Any?) {

    }

    override fun release() {

    }

    override fun setFullscreenRect(fullScreen: Boolean, rect: Rect) {
        // Not used in this implementation
    }

    override fun setFullDisplayRect(rect: Rect) {
        // Not used in this implementation
    }

    override fun show(show: Boolean) {
        // Not used in this implementation
    }

    override fun isShowing(): Boolean {
        // Not used in this implementation
        return false
    }

    override fun setClosable(closable: Boolean) {
        // Not used in this implementation
    }

    override fun setRect(rect: Rect) {
        // Not used in this implementation
    }
}