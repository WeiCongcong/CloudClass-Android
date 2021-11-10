package io.agora.uikit.impl.options

import android.annotation.SuppressLint
import android.os.CountDownTimer
import android.util.TypedValue.COMPLEX_UNIT_PX
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.view.isVisible
import io.agora.educontext.EduContextCallback
import io.agora.educontext.EduContextError
import io.agora.educontext.EduContextHandsUpState
import io.agora.educontext.EduContextPool
import io.agora.uikit.R
import io.agora.uikit.component.toast.AgoraUIToastManager
import io.agora.uikit.educontext.handlers.HandsUpHandler
import io.agora.uikit.impl.users.AgoraUIHandsUpToastPopUp

@SuppressLint("ClickableViewAccessibility")
class AgoraUIHandsUpWrapper(private val parent: ViewGroup,
                            private val eduContext: EduContextPool?,
                            private val anchor: AppCompatImageView,
                            private var width: Int,
                            private var height: Int,
                            private var right: Int,
                            private var bottom: Int,
                            private var handsUpPopup: AgoraUIHandsUpToastPopUp?) {

    private val tag = "AgoraUIHandsUpWrapper"

    private val timerLimit = 3200L
    private val timerInterval = 1000L
    private var mHandsUpPopup: AgoraUIHandsUpToastPopUp? = null

    private val timerView = LayoutInflater.from(anchor.context).inflate(
        R.layout.agora_handsup_countdown_timer_layout, parent, false)

    private val timerText = timerView.findViewById<AppCompatTextView>(R.id.count_down_text)
    private var HandsUpState: Int = HandsUpStateEnum.HandsUpBefore.value

    private val countDownTexts = parent.context.resources.
        getStringArray(R.array.agora_hands_up_count_down_texts)

    private val handsUpImages = arrayOf(
        R.drawable.agora_handsup_up_img,
        R.drawable.agora_handsup_down_img2,
        R.drawable.agora_handsup_cohost_img)

    private var state = EduContextHandsUpState.Init
    private var isCoHost = false

    @SuppressLint("ClickableViewAccessibility")
    private val anchorTouchListener = View.OnTouchListener { _, event ->
        when (event?.action) {

            MotionEvent.ACTION_DOWN -> {
                showTimerText()
                if (HandsUpState == HandsUpStateEnum.HandsUpBefore.value) {
                    HandsUpState = HandsUpStateEnum.HandsUping.value
                    timerText.isVisible = true
                    timerText.text = countDownTexts[1]
                    //挥手
                    eduContext?.handsUpContext()?.performHandsWave(EduContextHandsUpState.HandsUp, object : EduContextCallback<Boolean> {
                        override fun onSuccess(target: Boolean?) {
                            //处理down之后迅速up的情况， 确保收到-1回调的时候发一次3
                            //收到回调之后，up之前，下面的代码已经走过了 只是没有进if()里面
                            if (HandsUpState == HandsUpStateEnum.HandsUpAfter.value) {
                                eduContext?.handsUpContext()?.performHandsWave(EduContextHandsUpState.HandsUp, null, 3)
                            }
                        }

                        override fun onFailure(error: EduContextError?) {

                        }

                    }, -1)
                    showHandsUpToastPopup()
                }

            }
            MotionEvent.ACTION_UP -> {
                showTimerText()
                if (HandsUpState == HandsUpStateEnum.HandsUping.value) {
                    HandsUpState = HandsUpStateEnum.HandsUpAfter.value
                    cancelCountDownTimer.cancel()
                    handsUpStart()
                    eduContext?.handsUpContext()?.performHandsWave(EduContextHandsUpState.HandsUp, null, 3)
                }
            }
        }
        false
    }

    private fun showHandsUpToastPopup() {
        if (handsUpPopup?.isShowing == false){
            handsUpPopup?.show()
        }
    }
    private fun handsUpStart() {
        cancelCountDownTimer.start()
        timerText.isVisible = true
        timerText.text = countDownTexts[1]
    }

    private var cancelCountDownTimer: CountDownTimer = object : CountDownTimer(timerLimit, timerInterval) {
        override fun onFinish() {
            hideTimerText()
            timerText.text = ""
            timerText.isVisible = false

            HandsUpState = HandsUpStateEnum.HandsUpBefore.value//重新处理ACTION_DOWN事件
        }

        override fun onTick(millisUntilFinished: Long) {
            val index: Int = (3 - (millisUntilFinished / 1000)).toInt() + 1
            timerText.text = countDownTexts[index]
//            if (index==1){ //确保text到1后，更新timeout值
//                eduContext?.handsUpContext()?.performHandsWave(EduContextHandsUpState.HandsUp, null, 1)
//            }
        }
    }

    private val handsUpHandler = object : HandsUpHandler() {
        override fun onHandsUpEnabled(enabled: Boolean) {
            this@AgoraUIHandsUpWrapper.setHandsUpEnable(enabled)
        }

        override fun onHandsUpStateUpdated(state: EduContextHandsUpState, coHost: Boolean) {
            this@AgoraUIHandsUpWrapper.updateHandsUpState(state, coHost)
        }

        override fun onHandsUpStateResultUpdated(error: EduContextError?) {
            this@AgoraUIHandsUpWrapper.updateHandsUpStateResult(error)
        }
    }

    init {
        anchor.setOnTouchListener(anchorTouchListener)

        parent.addView(timerView)
        locate()
        hideTimerText()

        eduContext?.handsUpContext()?.addHandler(handsUpHandler)
        mHandsUpPopup = handsUpPopup
    }

    private fun locate() {
        val param = timerView.layoutParams as ViewGroup.MarginLayoutParams
        param.width = width
        param.height = height
        param.rightMargin = right
        param.bottomMargin = bottom
        param.leftMargin = parent.width - right - width
        param.topMargin = parent.height - bottom - height
        timerView.layoutParams = param

        timerText.setTextSize(COMPLEX_UNIT_PX, width / 2f)
    }

    private fun showTimerText() {
        timerView.visibility = View.VISIBLE
    }

    private fun hideTimerText() {
        timerView.visibility = View.GONE
    }

    fun setHandsUpEnable(enable: Boolean) {
        anchor.post {
            if (enable) {
                anchor.setImageResource(handsUpImages[0])
            } else {
                cancelCountDownTimer.cancel()
                anchor.setImageResource(handsUpImages[2])
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    fun updateHandsUpState(state: EduContextHandsUpState, coHost: Boolean) {
        this.state = state
        this.isCoHost = coHost

        anchor.post {
            if (coHost) {//如果已经上台了
                anchor.setOnTouchListener(anchorTouchListener)
                anchor.setImageResource(handsUpImages[1])
            } else {
                anchor.setOnTouchListener(anchorTouchListener)
                if (state == EduContextHandsUpState.HandsUp) {
                    anchor.setImageResource(handsUpImages[1])
                } else {
                    anchor.setImageResource(handsUpImages[1])
                }
            }
        }
    }

    fun updateHandsUpStateResult(error: EduContextError?) {
        error?.let {
            AgoraUIToastManager.showShort(it.msg)
        }
    }
}

enum class HandsUpStateEnum(val value: Int) {
    HandsUpBefore(0),
    HandsUping(1),
    HandsUpAfter(2)
}