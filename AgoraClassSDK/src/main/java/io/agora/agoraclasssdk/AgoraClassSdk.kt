package io.agora.agoraclasssdk

import android.content.Context
import io.agora.agoraclasssdk.app.activities.*
import io.agora.agoraeducore.core.AgoraEduCore
import io.agora.agoraeducore.core.ClassInfoCache
import io.agora.agoraeducore.core.internal.education.impl.Constants.Companion.AgoraLog
import io.agora.agoraeducore.core.internal.framework.impl.managers.AgoraWidgetManager
import io.agora.agoraeducore.core.internal.framework.impl.managers.AgoraWidgetManager.Companion.registerDefaultOnce
import io.agora.agoraeducore.core.internal.framework.proxy.RoomType
import io.agora.agoraeducore.core.internal.launch.*
import io.agora.agoraeducore.extensions.extapp.AgoraExtAppConfiguration
import io.agora.agoraeducore.extensions.extapp.AgoraExtAppEngine
import io.agora.agoraeducore.extensions.widgets.AgoraWidgetConfig
import io.agora.agoraeducore.extensions.widgets.AgoraWidgetDefaultId
import io.agora.agoraeduuikit.impl.chat.EaseChatWidget
import io.agora.agoraeduuikit.impl.chat.EaseChatWidgetPopup
import io.agora.agoraeduuikit.impl.whiteboard.AgoraWhiteBoardWidget

object AgoraClassSdk {
    private const val tag = "AgoraClassSdk"
    private lateinit var config: AgoraClassSdkConfig

    init {
        globalInit()
    }

    fun setConfig(config: AgoraClassSdkConfig) {
        AgoraClassSdk.config = config
    }

    private fun globalInit() {
        // Things that the only class sdk instance should do as global initialization
        // 1. register necessary widgets and extension apps
        registerWidgets()

        // 2. register activities for each room type
        addRoomClassTypes()
    }

    private fun registerWidgets() {
        // Register default widgets globally here because we must ensure
        // users call this register method just before they use our edu
        // library and will relief them registering default widgets in their code.
        // Then there will be a chance to replace the widgets of their own.
        // Widget registering will not depend on any other part of classroom
        // mechanism, so we handle it at the beginning of the classroom launch.
        val map = mutableMapOf<String, MutableList<AgoraWidgetConfig>>()
        val cnWidgetConfigs = mutableListOf<AgoraWidgetConfig>()
        cnWidgetConfigs.add(AgoraWidgetConfig(
            widgetClass = EaseChatWidgetPopup::class.java,
            widgetId = AgoraWidgetDefaultId.Chat.id))
        cnWidgetConfigs.add(AgoraWidgetConfig(
            widgetClass = AgoraWhiteBoardWidget::class.java,
            widgetId = AgoraWidgetDefaultId.WhiteBoard.id))
        map[AgoraEduRegion.cn] = cnWidgetConfigs
        val naWidgetConfigs = mutableListOf<AgoraWidgetConfig>()
        naWidgetConfigs.add(AgoraWidgetConfig(
            widgetClass = EaseChatWidgetPopup::class.java,
            widgetId = AgoraWidgetDefaultId.Chat.id))
        naWidgetConfigs.add(AgoraWidgetConfig(
            widgetClass = AgoraWhiteBoardWidget::class.java,
            widgetId = AgoraWidgetDefaultId.WhiteBoard.id))
        map[AgoraEduRegion.na] = naWidgetConfigs
        val apWidgetConfigs = mutableListOf<AgoraWidgetConfig>()
        apWidgetConfigs.add(AgoraWidgetConfig(
            widgetClass = EaseChatWidgetPopup::class.java,
            widgetId = AgoraWidgetDefaultId.Chat.id))
        apWidgetConfigs.add(AgoraWidgetConfig(
            widgetClass = AgoraWhiteBoardWidget::class.java,
            widgetId = AgoraWidgetDefaultId.WhiteBoard.id))
        map[AgoraEduRegion.ap] = apWidgetConfigs
        val euWidgetConfigs = mutableListOf<AgoraWidgetConfig>()
        euWidgetConfigs.add(AgoraWidgetConfig(
            widgetClass = EaseChatWidgetPopup::class.java,
            widgetId = AgoraWidgetDefaultId.Chat.id))
        euWidgetConfigs.add(AgoraWidgetConfig(
            widgetClass = AgoraWhiteBoardWidget::class.java,
            widgetId = AgoraWidgetDefaultId.WhiteBoard.id))
        map[AgoraEduRegion.eu] = euWidgetConfigs
        registerDefaultOnce(map)
    }

    // todo this is bug, this will replace launchConfig.widgetConfig while roomType is 1v1.
    private fun injectChatWidgetByRoomType(launchConfig: AgoraEduLaunchConfig) {
        if (launchConfig.roomType == AgoraEduRoomType.AgoraEduRoomTypeBig.value) {
            val chatWidgetConfig = AgoraWidgetConfig(widgetClass = EaseChatWidget::class.java,
                widgetId = AgoraWidgetDefaultId.Chat.id)
            if (launchConfig.widgetConfigs.isNullOrEmpty()) {
                launchConfig.widgetConfigs = mutableListOf(chatWidgetConfig)
            } else {
                launchConfig.widgetConfigs?.forEach {
                    if (it.widgetId == AgoraWidgetDefaultId.Chat.id) {
                        it.widgetClass = EaseChatWidget::class.java
                    }
                }
            }
        }
    }

    private fun addRoomClassTypes() {
        ClassInfoCache.addRoomActivityDefault(RoomType.ONE_ON_ONE.value, OneToOneClassActivity::class.java)
        ClassInfoCache.addRoomActivityDefault(RoomType.SMALL_CLASS.value, SmallClassActivity::class.java)
        ClassInfoCache.addRoomActivityDefault(RoomType.SMALL_CLASS_ART.value, SmallClassArtActivity::class.java)
        ClassInfoCache.addRoomActivityDefault(RoomType.LARGE_CLASS.value, LargeClassActivity::class.java)
    }

    /**
     * Replace default activity implementation for a room type if a
     * different activity and UI is used. The activity should be an
     * extension of BaseClassActivity, in order to have classroom
     * capabilities.
     * This replacement is global, and in most cases this should be
     * called only once. Make sure call this method before launch.
     */
    fun replaceClassActivity(classType: Int, activity: Class<out BaseClassActivity>) {
        ClassInfoCache.replaceRoomActivity(classType, activity)
    }

    fun launch(context: Context, config: AgoraEduLaunchConfig, callback: AgoraEduLaunchCallback) {
        // Currently art small class use different chat
        // implementation than other class types.
        // Register here and we may use the same chat
        // component in future versions
        injectChatWidgetByRoomType(config)

        if (!this::config.isInitialized) {
            AgoraLog.e("$tag->AgoraClassSdk has not initialized a configuration(not call " +
                "AgoraClassSdk.setConfig function)")
            return
        }

        AgoraEduCore.setAgoraEduSDKConfig(AgoraEduSDKConfig(AgoraClassSdk.config.appId, 0))
        AgoraEduCore.launch(context, config, callback)
    }

    fun configCourseWare(configs: MutableList<AgoraEduCourseware>) {
        AgoraEduSDK.configCourseWare(configs)
    }

    fun downloadCourseWare(context: Context, listener: AgoraEduCoursewarePreloadListener) {
        AgoraEduSDK.downloadCourseWare(context, listener)
    }

    /**
     * Register custom extension apps.
     * If an extension app's identifier has already registered, it
     * will be ignored.
     */
    fun registerExtensionApp(configs: MutableList<AgoraExtAppConfiguration>) {
        AgoraExtAppEngine.registerExtAppList(configs)
    }
}

class AgoraClassSdkConfig(var appId: String)
