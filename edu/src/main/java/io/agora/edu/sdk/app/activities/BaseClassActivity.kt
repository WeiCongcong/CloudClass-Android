package io.agora.edu.sdk.app.activities

import io.agora.edu.core.internal.launch.AgoraEduLatencyLevel.AgoraEduLatencyLevelUltraLow
import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import android.view.ViewTreeObserver
import android.view.WindowManager
import android.widget.RelativeLayout
import androidx.core.view.ViewCompat
import io.agora.edu.R
import io.agora.edu.core.AgoraEduCore
import io.agora.edu.core.AgoraEduCoreConfig
import io.agora.edu.core.AgoraEduCoreStateListener
import io.agora.edu.core.AgoraEduCoreStatics.selectImageResultCode
import io.agora.edu.core.context.EduContextCallback
import io.agora.edu.core.context.EduContextError
import io.agora.edu.core.context.EduContextErrors.parameterErrCode
import io.agora.edu.core.internal.edu.classroom.OnNavigationStateListener
import io.agora.edu.core.internal.launch.AgoraEduLaunchConfig
import io.agora.edu.core.internal.util.AppUtil.getNavigationBarHeight
import io.agora.edu.core.internal.widget.EyeProtection
import io.agora.edu.core.internal.framework.data.EduError
import io.agora.edu.core.internal.education.api.room.data.EduRoomState
import io.agora.edu.core.internal.education.impl.Constants.Companion.AgoraLog
import io.agora.edu.core.internal.framework.EduUserRole
import io.agora.edu.core.internal.server.struct.response.RoomPreCheckRes
import io.agora.edu.uikit.interfaces.protocols.IAgoraUIContainer
import java.lang.ref.WeakReference

abstract class BaseClassActivity : BaseActivity() {
    private companion object {
        const val tag = "BaseClassActivity"
        const val launchConfig = "LAUNCHCONFIG"
        const val preCheckData = "PRECHECKDATA"
    }

    protected var launchConfig: AgoraEduLaunchConfig? = null
    private var preCheckData: RoomPreCheckRes? = null

    protected var activityLayout: RelativeLayout? = null
    protected var contentLayout: RelativeLayout? = null
    protected var container: IAgoraUIContainer? = null
    private var screenOffReceiver: ScreenOffBroadcastReceiver? = null

    private var eduCore: AgoraEduCore? = null

    protected fun eduCore(): AgoraEduCore? {
        return eduCore
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        window.setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        super.onCreate(savedInstanceState)
        val view = onContentViewLayout()
        view.fitsSystemWindows = true
        setContentView(view)
        initData()
    }

    protected abstract fun onContentViewLayout(): RelativeLayout

    protected open fun onCreateClassJoinMediaOption(): ClassJoinMediaOption {
        // By default, students do not publish media streams.
        // All roles will automatically subscribe any streams that
        // exist in the room
        return ClassJoinMediaOption(true,
                launchConfig?.roleType != EduUserRole.STUDENT.value)
    }

    private fun initData() {
        launchConfig = intent.getParcelableExtra(Companion.launchConfig)
        preCheckData = intent.getParcelableExtra(Companion.preCheckData)
        registerScreenOffReceiver()
    }

    override fun onStart() {
        super.onStart()
        launchConfig?.let { EyeProtection.setNeedShow(it.eyeCare == 1) }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == selectImageResultCode && resultCode == RESULT_OK) {
            // result of select image
            val intent = Intent()
            intent.action = packageName.plus(resources.getString(R.string.chat_window_select_image_action))
            intent.putExtra(resources.getString(R.string.chat_window_select_image_key), data?.data)
            sendBroadcast(intent)
        }
    }

    override fun onBackPressed() {
        container?.showLeave()
    }

    protected fun createEduCore(callback: EduContextCallback<Unit>) {
        if (launchConfig == null) {
            val msg = "init room fail, launch config is null"
            AgoraLog.e("$tag -> $msg")
            callback.onFailure(EduContextError(parameterErrCode, msg))
            return
        }

        if (preCheckData == null) {
            val msg = "init room fail, precheck data is null"
            AgoraLog.e("$tag -> $msg")
            callback.onFailure(EduContextError(parameterErrCode, msg))
            return
        }

        val mediaOption = onCreateClassJoinMediaOption()

        eduCore = AgoraEduCore(this@BaseClassActivity.applicationContext,
                AgoraEduCoreConfig(
                        appId = launchConfig!!.appId,
                        userName = launchConfig!!.userName,
                        userUuid = launchConfig!!.userUuid,
                        roomName = launchConfig!!.roomName,
                        roomUuid = launchConfig!!.roomUuid,
                        roleType = launchConfig!!.roleType,
                        roomType = launchConfig!!.roomType,
                        rtmToken = launchConfig!!.rtmToken,
                        startTime = launchConfig!!.startTime ?: 0,
                        duration = launchConfig!!.duration ?: 0,
                        rtcRegion = preCheckData!!.rtcRegion ?: "",
                        rtmRegion = preCheckData!!.rtmRegion ?: "",
                        mediaOptions = launchConfig!!.mediaOptions,
                        userProperties = launchConfig!!.userProperties,
                        widgetConfigs = launchConfig!!.widgetConfigs,
                        state = EduRoomState.INIT.value,
                        closeDelay = preCheckData!!.closeDelay,
                        lastMessageId = preCheckData!!.lastMessageId,
                        muteChat = preCheckData!!.muteChat,
                        boardAppId = preCheckData!!.board?.boardAppId ?: "",
                        boardId = preCheckData!!.board?.boardId ?: "",
                        boardToken = preCheckData!!.board?.boardToken ?: "",
                        boardRegion = preCheckData!!.board?.boardRegion ?: "",
                        videoEncoderConfig = launchConfig!!.videoEncoderConfig,
                        agoraEduStreamState = launchConfig!!.agoraEduStreamState,
                        boardFitMode = launchConfig!!.boardFitMode,
                        latencyLevel = launchConfig!!.latencyLevel ?: AgoraEduLatencyLevelUltraLow,
                        eyeCare = launchConfig!!.eyeCare,
                        vendorId = launchConfig!!.vendorId,
                        logDir = launchConfig!!.logDir,
                        autoSubscribe = mediaOption.autoSubscribe,
                        autoPublish = mediaOption.autoPublish,
                        needUserListener = true),
                object : AgoraEduCoreStateListener {
                    override fun onCreated() {
                        callback.onSuccess(Unit)
                    }

                    override fun onError(error: EduError) {
                        AgoraLog.e("$tag -> ${error.msg}")
                        callback.onFailure(EduContextError(error.type, error.msg))
                    }
                }
        )
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        isNavigationBarChanged(this, object : OnNavigationStateListener {
            override fun onNavigationState(isShowing: Boolean, b: Int) {
                Log.e(tag, "isNavigationBarExist->$isShowing")
                contentLayout?.viewTreeObserver?.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
                    override fun onGlobalLayout() {
                        contentLayout?.viewTreeObserver?.removeOnGlobalLayoutListener(this)
                        val w = contentLayout!!.width
                        val h = contentLayout!!.height
                        container?.resize(contentLayout!!, 0, 0, w, h)
                    }
                })
            }
        })
    }

    private fun isNavigationBarChanged(activity: Activity,
                                       onNavigationStateListener: OnNavigationStateListener) {
        val height: Int = getNavigationBarHeight(activity)
        ViewCompat.setOnApplyWindowInsetsListener(activity.window.decorView) { v, insets ->
            var isShowing = false
            var l = 0
            var r = 0
            var b = 0
            if (insets != null) {
                l = insets.systemWindowInsetLeft
                r = insets.systemWindowInsetRight
                b = insets.systemWindowInsetBottom
                isShowing = l == height || r == height || b == height
            }
            if (b <= height) {
                onNavigationStateListener.onNavigationState(isShowing, b)
            }
            ViewCompat.onApplyWindowInsets(v, insets)
        }
    }

    private fun registerScreenOffReceiver() {
        screenOffReceiver = ScreenOffBroadcastReceiver(this)
        val filter = IntentFilter(Intent.ACTION_SCREEN_OFF)
        registerReceiver(screenOffReceiver, filter)
    }

    private fun unregisterScreenOffReceiver() {
        screenOffReceiver?.let {
            unregisterReceiver(it)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        eduCore()?.release()
        container?.release()
        unregisterScreenOffReceiver()
    }
}

class ClassJoinStateCountDown {
    private val max = 2
    private var current = max

    @Synchronized
    fun countdownRoomInit() {
        current--
    }

    @Synchronized
    fun countdownUiReady() {
        current--
    }

    @Synchronized
    fun isDone(): Boolean {
        return current == 0
    }
}

data class ClassJoinMediaOption(
        val autoSubscribe: Boolean,
        val autoPublish: Boolean
)

/**
 * if screenOff long time, app will be recycled, when screenOn by user, app must crash because of rtmClient
 * is released(or some property has not been initialized), so quit activity when screen off to solve this bug.
 * */
class ScreenOffBroadcastReceiver(activity: Activity) : BroadcastReceiver() {
    private val tag = "ScreenOffBroadcastReceiver"
    private val weakContext = WeakReference(activity)

    companion object {
        var autoHandleScreenOffEvent = true
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == Intent.ACTION_SCREEN_OFF && autoHandleScreenOffEvent) {
            AgoraLog.d("$tag->onReceive:auto handle ACTION_SCREEN_OFF event")
            weakContext.get()?.let {
                if (!it.isFinishing && !it.isDestroyed) {
                    weakContext.clear()
                    it.finish()
                }
            }
        }
    }
}