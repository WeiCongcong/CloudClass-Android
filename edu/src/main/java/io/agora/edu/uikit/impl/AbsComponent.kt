package io.agora.edu.uikit.impl

import android.graphics.Rect
import io.agora.edu.uikit.impl.container.AbsUIContainer

abstract class AbsComponent {
    private var container: AbsUIContainer? = null

    fun setContainer(container: AbsUIContainer) {
        this.container = container
    }

    fun getContainer(): AbsUIContainer? {
        return this.container
    }

    abstract fun setRect(rect: Rect)
}