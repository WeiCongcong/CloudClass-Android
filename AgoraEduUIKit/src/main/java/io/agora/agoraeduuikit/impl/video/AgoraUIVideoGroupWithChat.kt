package io.agora.agoraeduuikit.impl.video

import android.content.Context
import android.graphics.Rect
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.RelativeLayout
import androidx.core.view.isVisible
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener
import io.agora.agoraeduuikit.R
import io.agora.agoraeducore.core.context.EduContextPool
import io.agora.agoraeducore.core.context.EduContextRoomInfo
import io.agora.agoraeducore.core.context.EduContextVideoMode
import io.agora.agoraeducore.core.internal.education.impl.Constants.Companion.AgoraLog
import io.agora.agoraeducore.core.internal.framework.impl.handler.RoomHandler
import io.agora.agoraeducore.extensions.widgets.AgoraWidgetDefaultId
import io.agora.agoraeduuikit.provider.UIDataProvider
import io.agora.agoraeduuikit.impl.AbsComponent
import io.agora.agoraeduuikit.impl.chat.ChatPopupWidget
import io.agora.agoraeduuikit.impl.chat.EaseChatWidgetPopup


class AgoraUIVideoGroupWithChat(
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

    private val tabLayout = TabLayout(context)
    private val tabs = arrayOf("视频", "聊天")
    private val contentView = RelativeLayout(context)
    val view = View(context)
    private val videoLayout = LinearLayout(context)
    private val chatLayout = LinearLayout(context)
    private var teacherVideoWindow: AgoraUIVideoGroup? = null
    protected var chat: ChatPopupWidget? = null
    private val chatRect = Rect()
    private val roomHandler = object : RoomHandler() {
        override fun onRoomJoinSuccess(roomInfo: EduContextRoomInfo) {
            super.onRoomJoinSuccess(roomInfo)
            chatLayout.let {
                val config1 = eduContext?.widgetContext()?.getWidgetConfig(AgoraWidgetDefaultId.Chat.id)
                config1?.apply {
                    chat = eduContext?.widgetContext()?.create(config1) as? ChatPopupWidget
                }
                it.post {
                    // make sure the view is added to the top-level viewGroup
                    (contentView.parent as? ViewGroup)?.let { vg ->
                        (chat as? EaseChatWidgetPopup)?.setInputViewParent(vg)
                    }
                    chat?.init(it, width, height, 0, 0)
                    chat?.setRect(chatRect)
                    chat?.setClosable(false)
                }
            }
        }
    }

    init {
        teacherVideoWindow = AgoraUIVideoGroup(parent.context, eduContext,
            videoLayout, 0, 0, width,
            height - 150, margin, EduContextVideoMode.Pair)
        for (i in tabs.indices) {
            tabLayout.addTab(tabLayout.newTab().setText(tabs[i]))
        }
        tabLayout.setSelectedTabIndicatorHeight(0)
        tabLayout.addOnTabSelectedListener(object : OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                when (tab.position) {
                    0 -> {
                        AgoraLog.d("TAG", "0")
                        teacherVideoWindow?.show(true)
                        chatLayout.isVisible = false
                    }
                    1 -> {
                        AgoraLog.d("TAG", "0")
                        teacherVideoWindow?.show(false)
                        chatLayout.isVisible = true
                    }
                    else -> {
                    }
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {
            }

            override fun onTabReselected(tab: TabLayout.Tab) {
            }
        })
        parent.addView(contentView, width, height)
        contentView.addView(videoLayout, width, height)
        contentView.addView(chatLayout, width, height)
        contentView.addView(tabLayout)
        var params = tabLayout.layoutParams
        params.height = 150
        tabLayout.layoutParams = params
        chatRect.set(0, tabLayout.layoutParams.height, width, height)
        contentView.setBackgroundColor(context.resources.getColor(R.color.agora_board_preload_progress_view_progressbar_bg))
        val param = RelativeLayout.LayoutParams(
            RelativeLayout.LayoutParams.MATCH_PARENT,
            RelativeLayout.LayoutParams.WRAP_CONTENT)
        param.height = 5
        param.addRule(RelativeLayout.ABOVE, videoLayout.id)
        param.setMargins(0, tabLayout.layoutParams.height - margin, 0, 0)
        view.setBackgroundColor(context.resources.getColor(R.color.gray_F9F9FC))
        contentView.addView(view, param)
        val videoParams0 = contentView.layoutParams as ViewGroup.MarginLayoutParams
        videoParams0.leftMargin = left
        videoParams0.topMargin = top + margin
        contentView.layoutParams = videoParams0
        val videoParams = videoLayout.layoutParams as ViewGroup.MarginLayoutParams
        videoParams.leftMargin = 0
        videoParams.topMargin = tabLayout.layoutParams.height
        videoLayout.layoutParams = videoParams
        videoLayout.orientation = LinearLayout.VERTICAL
        val videoParams2 = chatLayout.layoutParams as ViewGroup.MarginLayoutParams
        videoParams2.leftMargin = 0
        videoParams2.topMargin = 0
        chatLayout.layoutParams = videoParams2
        chatLayout.orientation = LinearLayout.VERTICAL
        teacherVideoWindow?.let {
            uiDataProvider?.addListener(it.uiDataProviderListener)
        }
        eduContext?.roomContext()?.addHandler(roomHandler)
        videoLayout.post {
            val params = videoLayout.layoutParams as ViewGroup.MarginLayoutParams
            params.topMargin = tabLayout.layoutParams.height
            params.leftMargin = 0
            params.width = width
            params.height = height
            videoLayout.layoutParams = params
        }
        chatLayout.post {
            val params = chatLayout.layoutParams as ViewGroup.MarginLayoutParams
            params.topMargin = 0
            params.leftMargin = 0
            params.width = width
            params.height = height
            chatLayout.layoutParams = params
        }
        chatLayout.isVisible = false

    }


    fun show(show: Boolean) {
        contentView.post {
            contentView.visibility = if (show) View.VISIBLE else View.GONE
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