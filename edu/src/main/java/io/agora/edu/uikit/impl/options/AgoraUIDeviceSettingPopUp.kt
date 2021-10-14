package io.agora.edu.uikit.impl.options

import android.annotation.SuppressLint
import android.content.Context
import android.text.Spannable
import android.text.SpannableString
import android.text.style.UnderlineSpan
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.RelativeLayout
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.view.forEach
import io.agora.edu.R
import io.agora.edu.core.context.EduContextCameraFacing
import io.agora.edu.core.context.EduContextDeviceConfig
import io.agora.edu.core.context.EduContextPool
import io.agora.edu.uikit.handlers.DeViceHandler
import io.agora.edu.uikit.util.AppUtil

class AgoraUIDeviceSettingPopUp : RelativeLayout {
    constructor(context: Context) : super(context)
    constructor(context: Context, attr: AttributeSet) : super(context, attr)
    constructor(context: Context, attr: AttributeSet, defStyleAttr: Int) : super(context, attr, defStyleAttr)

    private val tag = "AgoraUIDeviceSettingDia"

    private val clickInterval = 500L

    private var parent: ViewGroup? = null

    @SuppressLint("InflateParams")
    private val layout = LayoutInflater.from(context).inflate(
        R.layout.agora_option_setting_popup_layout, null, false)

    private val content = layout.findViewById(R.id.agora_setting_dialog_layout) as ViewGroup

    private val cameraSwitch: AppCompatImageView = layout.findViewById(R.id.agora_setting_popup_camera_switch)
    private val micSwitch: AppCompatImageView = layout.findViewById(R.id.agora_setting_popup_mic_switch)
    private val speakerSwitch: AppCompatImageView = layout.findViewById(R.id.agora_setting_popup_volume_switch)
    private val facingFront: AppCompatTextView = layout.findViewById(R.id.agora_setting_popup_camera_front)
    private val facingBack: AppCompatTextView = layout.findViewById(R.id.agora_setting_popup_camera_back)
    private val uploadText: AppCompatTextView = layout.findViewById(R.id.agora_setting_popup_upload_log)

    private var eduContext: EduContextPool? = null

    var leaveRoomRunnable: Runnable? = null

    var uploadLogRunnable: Runnable? = null

    private val eventHandler = object : DeViceHandler() {
        override fun onCameraDeviceEnableChanged(enabled: Boolean) {
            super.onCameraDeviceEnableChanged(enabled)
            cameraSwitch.isActivated = enabled
        }

        override fun onCameraFacingChanged(facing: EduContextCameraFacing) {
            super.onCameraFacingChanged(facing)
            facingFront.isActivated = facing == EduContextCameraFacing.Front
            facingBack.isActivated = facing == EduContextCameraFacing.Back
        }

        override fun onMicDeviceEnabledChanged(enabled: Boolean) {
            super.onMicDeviceEnabledChanged(enabled)
            micSwitch.isActivated = enabled
        }

        override fun onSpeakerEnabledChanged(enabled: Boolean) {
            super.onSpeakerEnabledChanged(enabled)
            speakerSwitch.isActivated = enabled
        }
    }

    init {
        content.clipToOutline = true

        val config = eduContext?.deviceContext()?.getDeviceConfig() ?: EduContextDeviceConfig()
        layout.findViewById<AppCompatTextView>(R.id.agora_setting_exit_button).setOnClickListener {
            dismiss()
            leaveRoomRunnable?.run()
        }

        cameraSwitch.isActivated = config.cameraEnabled
        cameraSwitch.setOnClickListener {
            if (AppUtil.isFastClick(clickInterval)) {
                return@setOnClickListener
            }
            eduContext?.deviceContext()?.setCameraDeviceEnable(!cameraSwitch.isActivated)
        }

        facingFront.isActivated = config.cameraFacing == EduContextCameraFacing.Front
        facingBack.isActivated = config.cameraFacing == EduContextCameraFacing.Back

        facingFront.setOnClickListener {
            if (AppUtil.isFastClick(clickInterval)) {
                return@setOnClickListener
            }
            eduContext?.deviceContext()?.switchCameraFacing()
        }

        facingBack.setOnClickListener {
            if (AppUtil.isFastClick(clickInterval)) {
                return@setOnClickListener
            }
            eduContext?.deviceContext()?.switchCameraFacing()
        }

        micSwitch.isActivated = config.micEnabled
        micSwitch.setOnClickListener {
            if (AppUtil.isFastClick(clickInterval)) {
                return@setOnClickListener
            }
            eduContext?.deviceContext()?.setMicDeviceEnable(!micSwitch.isActivated)
        }

        speakerSwitch.isActivated = config.speakerEnabled
        speakerSwitch.setOnClickListener {
            if (AppUtil.isFastClick(clickInterval)) {
                return@setOnClickListener
            }
            eduContext?.deviceContext()?.setSpeakerEnable(!speakerSwitch.isActivated)
        }

        underlineUploadText()
        uploadText.setOnClickListener {
            dismiss()
            uploadLogRunnable?.run()
        }
    }

    private fun underlineUploadText() {
        val spannable = SpannableString(uploadText.text)
        spannable.setSpan(UnderlineSpan(), 0, spannable.length, Spannable.SPAN_INCLUSIVE_EXCLUSIVE)
        uploadText.text = spannable
    }

    fun initView(parent: ViewGroup, width: Int, height: Int, shadow: Int) {
        this.parent = parent
        addView(layout, width, height)

        var param = content.layoutParams as MarginLayoutParams
        param.topMargin = shadow
        param.bottomMargin = shadow
        param.leftMargin = shadow
        param.rightMargin = shadow
        param.width = width - shadow * 2
        param.height = height - shadow * 2
        content.layoutParams = param

        // shadow width may be slightly different at
        // different direction, so we reduce the
        // shadow a little bit to avoid sharp edges.
        content.elevation = shadow.toFloat() * 3 / 5
    }

    fun setEduContextPool(eduContext: EduContextPool?) {
        this.eduContext = eduContext
        resetDeviceStateButtons()
        eduContext?.deviceContext()?.addHandler(eventHandler)
    }

    private fun resetDeviceStateButtons() {
        val config = eduContext?.deviceContext()?.getDeviceConfig()
        config?.let { it ->
            cameraSwitch.isActivated = it.cameraEnabled
            facingFront.isActivated = it.cameraFacing == EduContextCameraFacing.Front
            facingBack.isActivated = it.cameraFacing == EduContextCameraFacing.Back
            micSwitch.isActivated = it.micEnabled
            speakerSwitch.isActivated = it.speakerEnabled
        }
    }

    fun dismiss() {
        this.parent?.let {
            var contains = false
            it.forEach { child ->
                if (child == this) contains = true
            }

            if (contains) {
                it.removeView(this)
            }
        }
    }
}