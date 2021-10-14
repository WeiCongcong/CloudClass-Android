package io.agora.edu.uikit.impl.room

import android.annotation.SuppressLint
import android.app.Activity
import android.graphics.Rect
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import io.agora.edu.R
import io.agora.edu.uikit.component.dialog.AgoraUIDialog
import io.agora.edu.uikit.impl.AbsComponent
import io.agora.edu.uikit.component.dialog.AgoraUIDialogBuilder
import io.agora.edu.uikit.handlers.RoomHandler
import io.agora.edu.uikit.impl.setting.AgoraUIDeviceSettingDialog

@SuppressLint("InflateParams")
class AgoraUIRoomStatusNormal(private val parent: ViewGroup,
                              private val eduContext: io.agora.edu.core.context.EduContextPool?,
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

    private val eventHandler = object : RoomHandler() {
        override fun onClassroomName(name: String) {
            super.onClassroomName(name)
            setClassroomName(name)
        }

        override fun onClassState(state: io.agora.edu.core.context.EduContextClassState) {
            super.onClassState(state)
            setClassState(state)
        }

        override fun onClassTime(time: String) {
            super.onClassTime(time)
            setClassTime(time)
        }

        override fun onNetworkStateChanged(state: io.agora.edu.core.context.EduContextNetworkState) {
            super.onNetworkStateChanged(state)
            setNetworkState(state)
        }

        override fun onLogUploaded(logData: String) {
            super.onLogUploaded(logData)
            //set log updated dialog
            Log.d("updated log", "log updated ->$logData")
            setUploadLogDialog(logData)
        }

        override fun onFlexRoomPropsInitialized(properties: MutableMap<String, Any>) {
            super.onFlexRoomPropsInitialized(properties)
            Log.i(tag, "onFlexRoomPropsInitialized->")
        }

        override fun onFlexRoomPropsChanged(changedProperties: MutableMap<String, Any>, properties: MutableMap<String, Any>, cause: MutableMap<String, Any>?, operator: io.agora.edu.core.context.EduContextUserInfo?) {
            super.onFlexRoomPropsChanged(changedProperties, properties, cause, operator)
            Log.i(tag, "onRoomPropertiesChanged->")
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
            if (eduContext != null) {
                eduContext.roomContext()?.uploadLog()
            }
        }

        setNetworkState(io.agora.edu.core.context.EduContextNetworkState.Unknown)
        eduContext?.roomContext()?.addHandler(eventHandler)
    }

    fun showLeaveDialog() {
        className.post {
            AgoraUIDialogBuilder(className.context)
                    .title(className.context.resources.getString(R.string.agora_dialog_end_class_confirm_title))
                    .message(className.context.resources.getString(R.string.agora_dialog_end_class_confirm_message))
                    .negativeText(className.context.resources.getString(R.string.cancel))
                    .positiveText(className.context.resources.getString(R.string.confirm))
                    .positiveClick(View.OnClickListener {
                        // APIs of leaving room and whiteboard are split
                        // into different processes. As the default behavior,
                        // here we leave room and whiteboard both at the same
                        // time.
                        eduContext?.roomContext()?.leave()
                        eduContext?.whiteboardContext()?.leave()
                    })
                    .build()
                    .show()
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
                    eduContext?.roomContext()?.leave(false)
                    eduContext?.whiteboardContext()?.leave()
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
                    eduContext?.roomContext()?.leave(false)
                    eduContext?.whiteboardContext()?.leave()
                }
            }
        }
    }

    fun setClassroomName(name: String) {
        className.post { className.text = name }
    }

    fun setClassState(state: io.agora.edu.core.context.EduContextClassState) {
        classStateText.post {
            if (state == io.agora.edu.core.context.EduContextClassState.Destroyed) {
                destroyClassDialog()
                return@post
            }
            classStateText.setText(
                    when (state) {
                        io.agora.edu.core.context.EduContextClassState.Init -> R.string.agora_room_state_not_started
                        io.agora.edu.core.context.EduContextClassState.Start -> R.string.agora_room_state_started
                        io.agora.edu.core.context.EduContextClassState.End -> R.string.agora_room_state_end
                        else -> return@post
                    })
            if (state == io.agora.edu.core.context.EduContextClassState.End) {
                classStateText.setTextColor(classStateText.context.resources.getColor(R.color.agora_setting_leave_text_color))
                classTimeText.setTextColor(classStateText.context.resources.getColor(R.color.agora_setting_leave_text_color))
            }
        }
    }

    fun setClassTime(time: String) {
        classTimeText.post { classTimeText.text = time }
    }

    fun setNetworkState(state: io.agora.edu.core.context.EduContextNetworkState) {
        networkImage.post {
            networkImage.setImageResource(getNetworkStateIcon(state))
        }
    }

    fun setUploadLogDialog(logData: String) {
        uploadLogBtn.post {
            AgoraUIDialogBuilder(uploadLogBtn.context)
                    .title(uploadLogBtn.context.resources.getString(R.string.agora_dialog_sent_log_success))
                    .message(logData)
                    .positiveText(uploadLogBtn.context.resources.getString(R.string.confirm))
                    .build()
                    .show()
        }
    }

    private fun getNetworkStateIcon(state: io.agora.edu.core.context.EduContextNetworkState): Int {
        return when (state) {
            io.agora.edu.core.context.EduContextNetworkState.Good -> R.drawable.agora_tool_icon_signal_good
            io.agora.edu.core.context.EduContextNetworkState.Medium -> R.drawable.agora_tool_icon_signal_medium
            io.agora.edu.core.context.EduContextNetworkState.Bad -> R.drawable.agora_tool_icon_signal_bad
            io.agora.edu.core.context.EduContextNetworkState.Unknown -> R.drawable.agora_tool_icon_signal_unknown
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