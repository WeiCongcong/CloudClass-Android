package io.agora.edu.uikit.impl.container

import android.content.res.Resources
import android.graphics.Rect
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import io.agora.edu.R
import io.agora.edu.core.context.EduContextPool
import io.agora.edu.core.context.EduContextUserDetailInfo
import io.agora.edu.core.context.EduContextUserRole
import io.agora.edu.uikit.component.dialog.AgoraUICustomDialogBuilder
import io.agora.edu.uikit.component.dialog.AgoraUIDialogBuilder
import io.agora.edu.uikit.handlers.RoomHandler
import io.agora.edu.uikit.handlers.UserHandler
import io.agora.edu.uikit.handlers.VideoHandler
import io.agora.edu.uikit.impl.AgoraUIVideoListArt
import io.agora.edu.uikit.impl.loading.AgoraUILoading
import io.agora.edu.uikit.impl.options.*
import io.agora.edu.uikit.impl.room.AgoraUIRoomStatusArt
import io.agora.edu.uikit.impl.screenshare.AgoraUIScreenShare
import io.agora.edu.uikit.impl.whiteboard.AgoraUIWhiteBoardBuilder

class AgoraUISmallClassArtContainer(
        eduContext: EduContextPool?,
        configs: AgoraContainerConfig) : AbsUIContainer(eduContext, configs) {
    private val tag = "SmallClassArtContainer"

    private var statusBarHeight = 0

    private var whiteboardHeight = 0

    // margin for tool and page control
    private var componentMargin = 0

    private var margin = 0
    private var shadow = 0
    private var border = 0

    private var width = 0
    private var height = 0
    private var top = 0
    private var left = 0

    private var videoListWindow: AgoraUIVideoListArt? = null
    private var optionLayout: OptionsLayout? = null

    private var optionRight = 0
    private var optionBottom = 0
    private var optionIconSize = 0

    private var optionPopupRight = 0

    private var chatWidth = 0
    private var chatHeight = 0

    private val whiteboardRect = Rect()

    private var teacherDetailInfo: EduContextUserDetailInfo? = null
    private var coHostList: MutableList<EduContextUserDetailInfo> = mutableListOf()

    private var whiteboardToolItem: OptionsLayoutWhiteboardItem? = null

    private val smallContainerTeacherVideoHandler = object : VideoHandler() {
        override fun onUserDetailInfoUpdated(info: EduContextUserDetailInfo) {
            super.onUserDetailInfoUpdated(info)
            teacherDetailInfo = info
            notifyVideos()
        }
    }
    private val smallContainerUserHandler = object : UserHandler() {
        override fun onCoHostListUpdated(list: MutableList<EduContextUserDetailInfo>) {
            super.onCoHostListUpdated(list)
            coHostList = list
            notifyVideos()
        }

        override fun onKickOut() {
            super.onKickOut()
            kickOut()
        }
    }

    private fun notifyVideos() {
        val hasTeacher = teacherDetailInfo?.onLine == true
        videoListWindow?.showTeacher(hasTeacher)
        val hasCoHost = hasTeacher || coHostList.size > 0
        videoListWindow?.showStudents(hasCoHost)

        if (hasCoHost) {
            agoraUILoading?.setRect(whiteboardRect)
        } else {

        }
    }

    private val roomHandler = object : RoomHandler() {
        override fun onLogUploaded(logData: String) {
            setUploadLogDialog(logData)
        }
    }

    fun setUploadLogDialog(logData: String) {
        getActivity()?.let {
            it.runOnUiThread {
                AgoraUIDialogBuilder(it)
                        .title(it.resources.getString(R.string.agora_dialog_sent_log_success))
                        .message(logData)
                        .positiveText(it.resources.getString(R.string.confirm))
                        .build()
                        .show()
            }
        }
    }

    override fun init(layout: ViewGroup, left: Int, top: Int, width: Int, height: Int) {
        super.init(layout, left, top, width, height)

        this.width = width
        this.height = height
        this.left = left
        this.top = top

        initValues(layout.context.resources, width, height)

        roomStatusArt = AgoraUIRoomStatusArt(layout, getEduContext(), width, statusBarHeight, left, top)
        roomStatusArt!!.setContainer(this)

        calculateVideoSize()
        if (getContext() == null) {
            return
        }

        val whiteboardW = width
        whiteboardRect.top = height - whiteboardHeight
        whiteboardRect.bottom = height
        whiteboardRect.left = 0
        whiteboardRect.right = whiteboardW
        agoraUILoading = AgoraUILoading(layout, whiteboardRect)

        val whiteboardContainer = RelativeLayout(getContext())
        layout.addView(whiteboardContainer)
        val params = whiteboardContainer.layoutParams as ViewGroup.MarginLayoutParams
        params.width = whiteboardW
        params.height = whiteboardHeight
        params.topMargin = whiteboardRect.top
        whiteboardContainer.layoutParams = params

        whiteboardWindow = AgoraUIWhiteBoardBuilder(layout.context, getEduContext(), whiteboardContainer)
                .width(whiteboardW)
                .height(whiteboardHeight)
                .left(0f)
                .top(0f)
                .shadowWidth(0f).build()
        whiteboardWindow!!.setContainer(this)

        videoListWindow = AgoraUIVideoListArt(layout.context, getEduContext(),
                layout, 0, statusBarHeight, width,
                height - whiteboardRect.height() - statusBarHeight, 0, 0)

        screenShareWindow = AgoraUIScreenShare(layout.context,
                getEduContext(), layout,
                whiteboardRect.width(), whiteboardRect.height(),
                whiteboardRect.left, whiteboardRect.top, 0f)
        screenShareWindow!!.setContainer(this)

        initOptionLayout(whiteboardContainer)

        getEduContext()?.videoContext()?.addHandler(smallContainerTeacherVideoHandler)
        getEduContext()?.userContext()?.addHandler(smallContainerUserHandler)
        getEduContext()?.roomContext()?.addHandler(roomHandler)
    }

    private fun initOptionLayout(layout: ViewGroup) {
        val role = getEduContext()?.userContext()?.localUserInfo()?.role
                ?: EduContextUserRole.Student
        val mode = if (AgoraUIConfig.isLargeScreen) OptionLayoutMode.Separate else OptionLayoutMode.Joint
        val container = if (layout is RelativeLayout) {
            layout
        } else {
            val container = RelativeLayout(getContext())
            val params = ViewGroup.MarginLayoutParams(
                    ViewGroup.MarginLayoutParams.MATCH_PARENT,
                    ViewGroup.MarginLayoutParams.MATCH_PARENT)
            layout.addView(container, params)
            container
        }

        OptionsLayout(getContext()).let {
            optionLayout = it

            it.init(getEduContext(), container, role, optionIconSize, optionRight, mode,
                    this, widgetManager)
            it.popUpListener = object : OptionPopupListener {
                override fun onPopupShow(item: OptionItemType) {

                }

                override fun onPopupDismiss(item: OptionItemType) {

                }
            }
        }

        OptionsLayout.listener = object : OptionsLayoutListener {
            override fun onLeave() {
                showLeaveDialog()
            }

            override fun onUploadLog() {
                getEduContext()?.roomContext()?.uploadLog(false)
            }

            override fun onKickout(userId: String, userName: String) {
                showKickDialog(userId)
            }
        }

        // On large screens(tablets), whiteboard tool button is placed
        // at the right-bottom of classroom UI container, separated
        // from other option items
        if (mode == OptionLayoutMode.Separate) {
            OptionsLayoutWhiteboardItem(container, optionIconSize,
                    optionRight, optionRight, optionRight, getEduContext()).let {
                whiteboardToolItem = it

                val param = RelativeLayout.LayoutParams(optionIconSize, optionIconSize)
                param.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE)
                param.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE)
                param.rightMargin = optionRight
                param.bottomMargin = optionRight
                container.addView(it, param)

                it.initIcon()
                it.setOnClickListener {
                    optionLayout?.dismissCurrentPopup()
                    whiteboardToolItem?.toggleDialog()
                }
                if (role == EduContextUserRole.Student) {
                    it.visibility = View.GONE
                }
            }
        }
    }

    private fun showLeaveDialog() {
        layout()?.let {
            it.post {
                AgoraUIDialogBuilder(it.context)
                        .title(it.context.resources.getString(R.string.agora_dialog_end_class_confirm_title))
                        .message(it.context.resources.getString(R.string.agora_dialog_end_class_confirm_message))
                        .negativeText(it.context.resources.getString(R.string.cancel))
                        .positiveText(it.context.resources.getString(R.string.confirm))
                        .positiveClick { getEduContext()?.roomContext()?.leave() }
                        .build()
                        .show()
            }
        }
    }

    private fun showKickDialog(userId: String) {
        layout()?.let {
            it.post {
                val customView = LayoutInflater.from(it.context).inflate(
                        R.layout.agora_kick_dialog_radio_layout, it, false)
                val optionOnce = customView.findViewById<RelativeLayout>(R.id.agora_kick_dialog_once_layout)
                val optionForever = customView.findViewById<RelativeLayout>(R.id.agora_kick_dialog_forever_layout)

                optionOnce.isActivated = true
                optionForever.isActivated = false

                optionOnce.setOnClickListener {
                    optionOnce.isActivated = true
                    optionForever.isActivated = false
                }

                optionForever.setOnClickListener {
                    optionOnce.isActivated = false
                    optionForever.isActivated = true
                }

                AgoraUICustomDialogBuilder(it.context)
                        .title(it.context.resources.getString(R.string.agora_dialog_kick_student_title))
                        .negativeText(it.context.resources.getString(R.string.cancel))
                        .positiveText(it.context.resources.getString(R.string.confirm))
                        .positiveClick {
                            val forever = !optionOnce.isActivated && optionForever.isActivated
                            getEduContext()?.userContext()?.kick(userId, forever)
                        }
                        .setCustomView(customView)
                        .build()
                        .show()
            }
        }
    }

    private fun initValues(resources: Resources, width: Int, height: Int) {
        // 375 is the base height of container height of phones
        // 574 is the base height of tablets
        val basePhone = 375f
        val baseTablet = 574f
        statusBarHeight = if (AgoraUIConfig.isLargeScreen) {
            (height * 14 / basePhone).toInt()
        } else {
            (height * 20 / baseTablet).toInt()
        }

        whiteboardHeight = (height * 0.82).toInt()

        optionRight = (height * 4 / basePhone).toInt()
        optionBottom = (height * 7 / basePhone).toInt()

        optionIconSize = if (AgoraUIConfig.isLargeScreen)
            (height * 46 / baseTablet).toInt() else (height * 46 / basePhone).toInt()

        optionPopupRight = if (AgoraUIConfig.isLargeScreen)
            (height * 60 / baseTablet).toInt() else (height * 50 / basePhone).toInt()

        chatWidth = if (AgoraUIConfig.isLargeScreen)
            (height * 340 / baseTablet).toInt() else (height * 200 / basePhone).toInt()

        chatHeight = if (AgoraUIConfig.isLargeScreen)
            (height * 400 / baseTablet).toInt() else (height * 268 / basePhone).toInt()

        componentMargin = resources.getDimensionPixelSize(R.dimen.margin_medium)
        margin = resources.getDimensionPixelSize(R.dimen.margin_smaller)
        shadow = resources.getDimensionPixelSize(R.dimen.shadow_width)
        border = resources.getDimensionPixelSize(R.dimen.stroke_small)
    }

    override fun calculateVideoSize() {
        val videosLayoutMaxW = this.width - margin * (AgoraUIConfig.carouselMaxItem - 1)
        val videoMaxW = videosLayoutMaxW / AgoraUIConfig.carouselMaxItem
        AgoraUIConfig.SmallClass.teacherVideoWidth = videoMaxW
        AgoraUIConfig.SmallClass.teacherVideoHeight = (videoMaxW * AgoraUIConfig.videoRatio1).toInt()
    }

    override fun release() {
        widgetManager.release()
        optionLayout?.release()
    }

    override fun willLaunchExtApp(appIdentifier: String): Int {
        return 0
    }

    override fun getWhiteboardContainer(): ViewGroup? {
        return whiteboardWindow?.getWhiteboardContainer()
    }

    override fun setFullScreen(fullScreen: Boolean) {

    }
}