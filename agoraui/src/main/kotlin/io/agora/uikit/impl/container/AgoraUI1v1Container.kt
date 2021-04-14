package io.agora.uikit.impl.container

import android.content.res.Resources
import android.graphics.Rect
import android.view.ViewGroup
import io.agora.uikit.R
import io.agora.uikit.impl.chat.AgoraUIChatWindow
import io.agora.uikit.impl.room.AgoraUIRoomStatus
import io.agora.uikit.impl.screenshare.AgoraUIScreenShare
import io.agora.uikit.impl.tool.AgoraUIToolBarBuilder
import io.agora.uikit.impl.tool.AgoraUIToolType
import io.agora.uikit.impl.users.AgoraUIVideoMode
import io.agora.uikit.impl.video.AgoraUIVideoGroup
import io.agora.uikit.impl.whiteboard.AgoraUIWhiteBoardBuilder
import io.agora.uikit.impl.whiteboard.paging.AgoraUIPagingControlBuilder

class AgoraUI1v1Container : AbsUIContainer() {
    private val tag = "AgoraUI1v1Container"

    private var statusBarHeight = 0
    private var margin = 0
    private var componentMargin = 0
    private var shadow = 0
    private var border = 0

    private var width = 0
    private var height = 0
    private var top = 0
    private var left = 0

    private val chatRect = Rect()
    private val chatFullScreenRect = Rect()
    private val chatFullScreenHideRect = Rect()
    private val whiteboardRect = Rect()
    private val fullScreenRect = Rect()

    private var isFullScreen = false

    override fun init(layout: ViewGroup, left: Int, top: Int, width: Int, height: Int) {
        super.init(layout, left, top, width, height)

        this.width = width
        this.height = height
        this.left = left
        this.top = top

        initValues(layout.context.resources)
        layout.setBackgroundColor(layout.context.resources.getColor(R.color.theme_gray_lighter))

        roomStatus = AgoraUIRoomStatus(layout, width, statusBarHeight, left, top)
        roomStatus!!.setContainer(this)

        calculateVideoSize()
        val videoLayoutW = AgoraUIConfig.OneToOneClass.teacherVideoWidth
        val videoLayoutH = height - statusBarHeight - margin - border
        val leftMargin = width - videoLayoutW - border
        val topMargin = statusBarHeight + margin
        videoGroupWindow = AgoraUIVideoGroup(layout.context, layout, leftMargin, topMargin, videoLayoutW,
                videoLayoutH, margin, AgoraUIVideoMode.Pair)
        videoGroupWindow!!.setContainer(this)

        val whiteboardW = width - videoLayoutW - margin - border * 2
        val whiteboardH = height - statusBarHeight - margin - border
        whiteboardRect.set(border, statusBarHeight + margin, border + whiteboardW, height - border)
        whiteboardWindow = AgoraUIWhiteBoardBuilder(layout.context, layout)
                .width(whiteboardW)
                .height(whiteboardH)
                .top(statusBarHeight + margin.toFloat())
                .shadowWidth(0f).build()
        whiteboardWindow!!.setContainer(this)
        fullScreenRect.set(border, statusBarHeight + margin, width - border, height - border)

        screenShareWindow = AgoraUIScreenShare(layout.context, layout,
                whiteboardW, whiteboardH, border, statusBarHeight + margin, 0f)
        screenShareWindow!!.setContainer(this)

        val pagingControlHeight = layout.context.resources.getDimensionPixelSize(R.dimen.agora_paging_control_height)
        val pagingControlLeft = componentMargin
        val pagingControlTop = height - pagingControlHeight - border - componentMargin
        pageControlWindow = AgoraUIPagingControlBuilder(layout.context, layout)
                .height(pagingControlHeight)
                .left(pagingControlLeft.toFloat())
                .top(pagingControlTop.toFloat())
                .shadowWidth(shadow.toFloat())
                .build()
        pageControlWindow!!.setContainer(this)

        toolbar = AgoraUIToolBarBuilder(layout.context, layout)
                .foldTop(whiteboardRect.top + componentMargin)
                .unfoldTop(whiteboardRect.top + componentMargin)
                .unfoldLeft(componentMargin)
                .unfoldHeight(pagingControlTop - whiteboardRect.top - componentMargin)
                .shadowWidth(shadow)
                .build()
        toolbar!!.setToolbarType(AgoraUIToolType.Whiteboard)
        toolbar!!.setContainer(this)

        val messageLeft = width - videoLayoutW - videoLayoutW - componentMargin
        val messageTop: Int
        val messageHeight: Int
        // chat window height matches the height of whiteboard with content margins on phones
        // while it is 60% of layout height on tablets
        if (AgoraUIConfig.isLargeScreen) {
            messageHeight = (height * AgoraUIConfig.chatHeightLargeScreenRatio).toInt()
            messageTop = height - componentMargin - messageHeight
        } else {
            messageTop = whiteboardRect.top + componentMargin
            messageHeight = height - componentMargin - messageTop
        }
        chatRect.set(messageLeft, messageTop, messageLeft + videoLayoutW, messageTop + messageHeight)
        chatWindow = AgoraUIChatWindow(layout, videoLayoutW, messageHeight, messageLeft, messageTop, shadow)
        chatWindow!!.setContainer(this)
        chatWindow!!.show(false)

        val chatFullScreenRight = width - border - componentMargin
        val chatFullScreenBottom = height - componentMargin
        chatFullScreenRect.set(width - videoLayoutW - margin, messageTop, chatFullScreenRight, chatFullScreenBottom)
        val chatFullScreenHideTop = chatFullScreenBottom - chatWindow?.hideIconSize!!
        val chatFullScreenHideLeft = chatFullScreenRight - chatWindow?.hideIconSize!!
        chatFullScreenHideRect.set(chatFullScreenHideLeft, chatFullScreenHideTop, chatFullScreenRight, chatFullScreenBottom)
    }

    private fun initValues(resources: Resources) {
        statusBarHeight = resources.getDimensionPixelSize(R.dimen.agora_status_bar_height)
        margin = resources.getDimensionPixelSize(R.dimen.margin_smaller)
        shadow = resources.getDimensionPixelSize(R.dimen.shadow_width)
        componentMargin = resources.getDimensionPixelSize(R.dimen.margin_medium)
        border = resources.getDimensionPixelSize(R.dimen.stroke_small)
    }

    override fun setFullScreen(fullScreen: Boolean) {
        if (isFullScreen == fullScreen) {
            return
        }

        isFullScreen = fullScreen
        if (fullScreen) {
            whiteboardWindow?.setRect(fullScreenRect)
            screenShareWindow?.setRect(fullScreenRect)
            chatWindow?.setFullscreenRect(fullScreen, chatFullScreenHideRect)
            chatWindow?.setFullDisplayRect(chatFullScreenRect)
            chatWindow?.show(false)
        } else {
            whiteboardWindow?.setRect(whiteboardRect)
            screenShareWindow?.setRect(whiteboardRect)
            // chatWindow?.setFullscreenRect(fullScreen, chatRect)
            chatWindow?.setFullDisplayRect(chatRect)
            chatWindow?.show(false)
        }
    }

    override fun calculateVideoSize() {
        AgoraUIConfig.OneToOneClass.teacherVideoWidth =
                minOf((AgoraUIConfig.videoWidthMaxRatio * width).toInt(), AgoraUIConfig.OneToOneClass.teacherVideoWidth)
    }
}