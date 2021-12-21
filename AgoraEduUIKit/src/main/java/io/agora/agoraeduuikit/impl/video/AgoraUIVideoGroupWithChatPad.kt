package io.agora.agoraeduuikit.impl.video

import android.content.Context
import android.graphics.Rect
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import io.agora.agoraeduuikit.R
import io.agora.agoraeducore.core.context.EduContextPool
import io.agora.agoraeducore.core.context.EduContextRoomInfo
import io.agora.agoraeducore.core.context.EduContextVideoMode
import io.agora.agoraeducore.core.internal.framework.impl.handler.RoomHandler
import io.agora.agoraeducore.extensions.widgets.AgoraWidgetDefaultId
import io.agora.agoraeduuikit.provider.UIDataProvider
import io.agora.agoraeduuikit.impl.AbsComponent
import io.agora.agoraeduuikit.impl.chat.ChatWidget
import io.agora.agoraeduuikit.impl.chat.EaseChatWidgetPopup


class AgoraUIVideoGroupWithChatPad(
    context: Context,
    private val eduContext: EduContextPool?,
    parent: ViewGroup,
    left: Int,
    top: Int,
    width: Int,
    height: Int,
    margin: Int,
    mode: EduContextVideoMode = EduContextVideoMode.Single,
    uiDataProvider: UIDataProvider?) : AbsComponent() {
    private val tag = "AgoraUIVideoGroup"
    private val contentView = LinearLayout(context)
    val view = View(context)
    private val videoLayout = LinearLayout(context)
    private val chatLayout = LinearLayout(context)
    private var teacherVideoWindow: AgoraUIVideoGroup? = null
    protected var chat: ChatWidget? = null
    private val chatRect = Rect()
    private val roomHandler = object : RoomHandler() {
        override fun onRoomJoinSuccess(roomInfo: EduContextRoomInfo) {
            super.onRoomJoinSuccess(roomInfo)
            chatLayout.let {
                val config1 = eduContext?.widgetContext()?.getWidgetConfig(AgoraWidgetDefaultId.Chat.id)
                config1?.apply {
                    chat = eduContext?.widgetContext()?.create(config1) as? ChatWidget
                }
                it.post {
                    // make sure the view is added to the top-level viewGroup
                    (contentView.parent as? ViewGroup)?.let { vg ->
                        (chat as? EaseChatWidgetPopup)?.setInputViewParent(vg)
                    }
                    chat?.init(it, width, height / 2, 0, 0)
                    chat?.setRect(chatRect)
                    chat?.setClosable(false)
                }
            }
        }
    }

    init {
        teacherVideoWindow = AgoraUIVideoGroup(parent.context, eduContext,
            videoLayout, 0, 0, width,
            height / 2, margin, EduContextVideoMode.Pair)
        parent.addView(contentView, width, height)
        contentView.addView(videoLayout, width, height / 2)
        contentView.addView(chatLayout, width, height / 2)
        chatRect.set(0, 0, width, height / 2)
        contentView.setBackgroundColor(context.resources.getColor(R.color.agora_board_preload_progress_view_progressbar_bg))
        val videoParams0 = contentView.layoutParams as ViewGroup.MarginLayoutParams
        videoParams0.leftMargin = left
        videoParams0.topMargin = top
        contentView.layoutParams = videoParams0
        contentView.orientation = LinearLayout.VERTICAL
        teacherVideoWindow?.let {
            uiDataProvider?.addListener(it.uiDataProviderListener)
        }
        eduContext?.roomContext()?.addHandler(roomHandler)
        videoLayout.post {
            val params = videoLayout.layoutParams as ViewGroup.MarginLayoutParams
            params.topMargin = 0
            params.leftMargin = 0
            params.width = width
            params.height = height / 2
            videoLayout.layoutParams = params
            videoLayout.orientation = LinearLayout.VERTICAL
        }
        chatLayout.post {
            val params = chatLayout.layoutParams as ViewGroup.MarginLayoutParams
            params.topMargin = 0
            params.leftMargin = 0
            params.width = width
            params.height = height / 2
            chatLayout.layoutParams = params
        }
    }

    fun show(show: Boolean) {
        videoLayout.post {
            videoLayout.visibility = if (show) View.VISIBLE else View.GONE
        }
    }

    override fun setRect(rect: Rect) {
        contentView.post {
            val params = contentView.layoutParams as ViewGroup.MarginLayoutParams
            params.topMargin = rect.top
            params.leftMargin = rect.left
            params.width = rect.right - rect.left
            params.height = rect.bottom - rect.top
            contentView.layoutParams = params
        }
    }
}