package io.agora.agoraeduuikit.impl.room

import android.annotation.SuppressLint
import android.graphics.Rect
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import io.agora.agoraeduuikit.R
import io.agora.agoraeducore.core.context.*
import io.agora.agoraeduuikit.component.dialog.AgoraUIDialog
import io.agora.agoraeduuikit.impl.AbsComponent
import io.agora.agoraeduuikit.component.dialog.AgoraUIDialogBuilder
import io.agora.agoraeducore.core.internal.framework.impl.handler.MonitorHandler
import io.agora.agoraeducore.core.internal.framework.impl.handler.RoomHandler
import io.agora.agoraeduuikit.impl.setting.AgoraUIDeviceSettingDialog

@SuppressLint("InflateParams")
class AgoraUIRoomStatusNormal(private val parent: ViewGroup,
                              private val eduContext: io.agora.agoraeducore.core.context.EduContextPool?,
                              width: Int,
                              height: Int,
                              left: Int,
                              top: Int) : AbsComponent() {
    private val tag = "AgoraUIRoomStatus"

    private val contentView: View = LayoutInflater.from(parent.context).inflate(
            R.layout.agora_status_bar_layout_normal, parent, false)
    private val networkImage: AppCompatImageView
    private val className: AppCompatTextView
    private val classStateText: AppCompatTextView
    private val classTimeText: AppCompatTextView
    private val settingBtn: AppCompatImageView
    private val uploadLogBtn: AppCompatImageView

    private var destroyClassDialog: AgoraUIDialog? = null

//    private val eventHandler = object : RoomHandler() {
//        override fun onClassTime(time: String) {
//            super.onClassTime(time)
//            setClassTime(time)
//        }
//    }

    private val roomHandler = object : RoomHandler() {
        override fun onRoomJoinSuccess(roomInfo: EduContextRoomInfo) {
            super.onRoomJoinSuccess(roomInfo)
            setClassroomName(roomInfo.roomName)
        }

        override fun onRoomClosed() {
            super.onRoomClosed()
            destroyClassDialog()
        }

        override fun onClassStateUpdated(state: AgoraEduContextClassState) {
            super.onClassStateUpdated(state)
            setClassState(state)
        }
    }

    private val monitorHandler = object : MonitorHandler() {
        override fun onLocalNetworkQualityUpdated(quality: EduContextNetworkQuality) {
            super.onLocalNetworkQualityUpdated(quality)
            setNetworkState(quality)
        }
    }

    init {
        parent.addView(contentView, width, height)
        val params = contentView.layoutParams as ViewGroup.MarginLayoutParams
        params.topMargin = top
        params.leftMargin = left
        contentView.layoutParams = params

        networkImage = contentView.findViewById(R.id.agora_status_bar_network_state_icon)
        className = contentView.findViewById(R.id.agora_status_bar_classroom_name)
        classStateText = contentView.findViewById(R.id.agora_status_bar_class_started_text)
        classTimeText = contentView.findViewById(R.id.agora_status_bar_class_time_text)
        settingBtn = contentView.findViewById(R.id.agora_status_bar_setting_icon)
        uploadLogBtn = contentView.findViewById(R.id.agora_status_bar_upload_log_icon)

        settingBtn.setOnClickListener {
//            showLeaveDialog()
            showDeviceSettingDialog()
        }

        uploadLogBtn.setOnClickListener {
            eduContext?.monitorContext()?.uploadLog(object : EduContextCallback<String> {
                override fun onSuccess(target: String?) {
                    target?.let {
                        setUploadLogDialog(target)
                    }
                }

                override fun onFailure(error: EduContextError?) {
                }
            })
        }

        setNetworkState(EduContextNetworkQuality.Unknown)
        eduContext?.roomContext()?.addHandler(roomHandler)
        eduContext?.monitorContext()?.addHandler(monitorHandler)
    }

    private fun setUploadLogDialog(logData: String) {
        getContainer()?.getActivity()?.let {
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

    fun showLeaveDialog() {
        ContextCompat.getMainExecutor(contentView.context).execute {
            getContainer()?.getActivity()?.let { activity ->
                if (!activity.isFinishing && !activity.isDestroyed) {
                    AgoraUIDialogBuilder(className.context)
                            .title(className.context.resources.getString(R.string.agora_dialog_end_class_confirm_title))
                            .message(className.context.resources.getString(R.string.agora_dialog_end_class_confirm_message))
                            .negativeText(className.context.resources.getString(R.string.cancel))
                            .positiveText(className.context.resources.getString(R.string.confirm))
                            .positiveClick {
                                eduContext?.roomContext()?.leaveRoom(object : EduContextCallback<Unit> {
                                    override fun onSuccess(target: Unit?) {
                                        activity.finish()
                                    }

                                    override fun onFailure(error: EduContextError?) {
                                        error?.let {
                                            getContainer()?.showError(it)
                                        }
                                    }
                                })
                            }
                            .build()
                            .show()
                }
            }
        }
    }

    private fun showDeviceSettingDialog() {
        settingBtn.isActivated = true
        val dialog = AgoraUIDeviceSettingDialog(settingBtn.context, settingBtn, Runnable { showLeaveDialog() },
                eduContext)
        dialog.setOnDismissListener {
            settingBtn.isActivated = false
        }
        dialog.show()
    }

    private fun destroyClassDialog() {
        if (destroyClassDialog != null && destroyClassDialog!!.isShowing) {
            return
        }
        ContextCompat.getMainExecutor(parent.context).execute {
            getContainer()?.getActivity()?.let { activity ->
                if (!activity.isFinishing && !activity.isDestroyed) {
                    destroyClassDialog = AgoraUIDialogBuilder(activity)
                            .title(className.context.resources.getString(R.string.agora_dialog_class_destroy_title))
                            .message(className.context.resources.getString(R.string.agora_dialog_class_destroy))
                            .positiveText(className.context.resources.getString(R.string.confirm))
                            .positiveClick {
                                activity.finish()
                            }
                            .build()
                    destroyClassDialog?.show()
                    eduContext?.roomContext()?.leaveRoom(object : EduContextCallback<Unit> {
                        override fun onSuccess(target: Unit?) {
                        }

                        override fun onFailure(error: EduContextError?) {
                            error?.let {
                                getContainer()?.showError(it)
                            }
                        }
                    })
                }
            }
        }
    }

    fun kickOut() {
        ContextCompat.getMainExecutor(parent.context).execute {
            getContainer()?.getActivity()?.let { activity ->
                if (!activity.isFinishing && !activity.isDestroyed) {
                    AgoraUIDialogBuilder(className.context)
                            .title(className.context.resources.getString(R.string.agora_dialog_kicked_title))
                            .message(className.context.resources.getString(R.string.agora_dialog_kicked_message))
                            .positiveText(className.context.resources.getString(R.string.confirm))
                            .positiveClick {
                                activity.finish()
                            }
                            .build()
                            .show()
                    eduContext?.roomContext()?.leaveRoom(object : EduContextCallback<Unit> {
                        override fun onSuccess(target: Unit?) {
                        }

                        override fun onFailure(error: EduContextError?) {
                            error?.let {
                                getContainer()?.showError(it)
                            }
                        }
                    })
                }
            }
        }
    }

    fun setClassroomName(name: String) {
        className.post { className.text = name }
    }

    fun setClassState(state: AgoraEduContextClassState) {
        classStateText.post {
//            if (state == AgoraEduContextClassState.Destroyed) {
//                destroyClassDialog()
//                return@post
//            }
            classStateText.setText(
                    when (state) {
                        AgoraEduContextClassState.Before -> R.string.agora_room_state_not_started
                        AgoraEduContextClassState.During -> R.string.agora_room_state_started
                        AgoraEduContextClassState.After -> R.string.agora_room_state_end
                        else -> return@post
                    })
            if (state == AgoraEduContextClassState.After) {
                classStateText.setTextColor(classStateText.context.resources.getColor(R.color.theme_text_color_orange_red))
                classTimeText.setTextColor(classStateText.context.resources.getColor(R.color.theme_text_color_orange_red))
            }
        }
    }

    fun setClassTime(time: String) {
        classTimeText.post { classTimeText.text = time }
    }

    fun setNetworkState(state: EduContextNetworkQuality) {
        networkImage.post {
            networkImage.setImageResource(getNetworkStateIcon(state))
        }
    }

    private fun getNetworkStateIcon(state: EduContextNetworkQuality): Int {
        return when (state) {
            EduContextNetworkQuality.Good -> R.drawable.agora_tool_icon_signal_good
            EduContextNetworkQuality.Medium -> R.drawable.agora_tool_icon_signal_medium
            EduContextNetworkQuality.Bad -> R.drawable.agora_tool_icon_signal_bad
            EduContextNetworkQuality.Unknown -> R.drawable.agora_tool_icon_signal_unknown
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