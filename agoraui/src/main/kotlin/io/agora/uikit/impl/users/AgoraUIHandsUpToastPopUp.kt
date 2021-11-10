package io.agora.uikit.impl.users

import android.content.Context
import android.os.CountDownTimer
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.RelativeLayout
import androidx.core.view.forEach
import io.agora.educontext.EduContextPool
import io.agora.uikit.R
import io.agora.uikit.impl.container.AgoraUIConfig

class AgoraUIHandsUpToastPopUp : RelativeLayout {
    constructor(context: Context) : super(context)
    constructor(context: Context, attr: AttributeSet) : super(context, attr)
    constructor(context: Context, attr: AttributeSet, defStyleAttr: Int) : super(context, attr, defStyleAttr)

    private val tag = "AgoraUIRosterPopUp"

    private var parent: ViewGroup? = null
    private var contentWidth = 0
    private var contentHeight = 0
    private var marginRight = 0
    private var marginBottom = 0

    var isShowing = false
    private val timerLimit = 3200L
    private val timerInterval = 1000L
    private var eduContext: EduContextPool? = null

    var closeRunnable: Runnable? = null


    fun setEduContext(eduContextPool: EduContextPool?) {
        this.eduContext = eduContextPool
    }

    fun initView(parent: ViewGroup, right: Int, bottom: Int) {// parent: contentLayout
        this.parent = parent
        marginRight = right
        marginBottom = bottom
    }

    private var cancelCountDownTimer: CountDownTimer = object : CountDownTimer(timerLimit, timerInterval) {
        override fun onFinish() {
            dismiss()
        }
        override fun onTick(millisUntilFinished: Long) {

        }
    }

    fun show() {
        isShowing = true
        this.parent?.let { parent ->//parent : activityFitLayout
            LayoutInflater.from(parent.context).inflate(R.layout.agora_handup_toast_dialog_layout, this)
            parent.addView(this)
            val param = this.layoutParams as MarginLayoutParams
            if (AgoraUIConfig.isLargeScreen) {
                contentWidth = 120
                contentHeight = 45
                param.width = contentWidth
                param.height = contentHeight
                param.rightMargin = marginRight
                param.bottomMargin = marginBottom - 10
                param.leftMargin = parent.width - marginRight - contentWidth
                param.topMargin = parent.height - marginBottom + 10 - contentHeight
                this.layoutParams = param
            } else {
                contentWidth = 250
                contentHeight = 88
                param.width = contentWidth
                param.height = contentHeight
                param.rightMargin = marginRight
                param.bottomMargin = marginBottom
                param.leftMargin = parent.width - marginRight - contentWidth
                param.topMargin = parent.height - marginBottom - contentHeight
                this.layoutParams = param
            }
        }
        cancelCountDownTimer.start()
    }


    fun dismiss() {
        parent?.let { parent ->
            var contains = false
            parent.forEach {
                if (it == this) contains = true
            }
            if (contains) parent.removeView(this)
            this.removeAllViews()
        }
    }


}