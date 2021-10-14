package io.agora.edu.core.internal.education.api.room.data

import io.agora.edu.core.internal.rte.data.RteLatencyLevel
import io.agora.edu.core.internal.rte.data.RteVideoEncoderConfig
import io.agora.edu.core.internal.framework.EduUserRole
import io.agora.edu.core.internal.launch.AgoraEduEncryptMode
import io.agora.edu.core.internal.launch.AgoraEduMediaEncryptionConfigs
import io.agora.edu.core.internal.rte.data.RteChannelMediaOptions
import io.agora.edu.core.internal.rte.data.RteEncryptionConfig
import io.agora.edu.core.internal.rte.data.RteEncryptionMode

data class RoomMediaOptions(
        var autoSubscribe: Boolean = true,
        var autoPublish: Boolean = true,
        val encryptionConfigs: AgoraEduMediaEncryptionConfigs? = null

) {
    /**用户传了primaryStreamId,那么就用他当做streamUuid;如果没传，就是默认值，后端会生成一个streamUuid*/
    var primaryStreamId: Int = DefaultStreamId

    companion object {
        const val DefaultStreamId = 0
    }

    constructor(primaryStreamId: Int) : this() {
        this.primaryStreamId = primaryStreamId
    }

    fun convert(): RteChannelMediaOptions {
        return RteChannelMediaOptions(autoSubscribeAudio = autoSubscribe, autoSubscribeVideo = autoSubscribe)
    }

    fun convertedEncryptionMode(mode: Int): RteEncryptionMode {
        when (mode) {
            AgoraEduEncryptMode.AES_128_XTS.value -> return RteEncryptionMode.AES_128_XTS
            AgoraEduEncryptMode.AES_128_ECB.value -> return RteEncryptionMode.AES_128_ECB
            AgoraEduEncryptMode.AES_256_XTS.value -> return RteEncryptionMode.AES_256_XTS
            AgoraEduEncryptMode.SM4_128_ECB.value -> return RteEncryptionMode.SM4_128_ECB
            AgoraEduEncryptMode.AES_128_GCM.value -> return RteEncryptionMode.AES_128_GCM
            AgoraEduEncryptMode.AES_256_GCM.value -> return RteEncryptionMode.AES_256_GCM
            AgoraEduEncryptMode.AES_128_GCM2.value -> return RteEncryptionMode.AES_128_GCM2
            AgoraEduEncryptMode.AES_256_GCM2.value -> return RteEncryptionMode.AES_256_GCM2
        }
        return RteEncryptionMode.MODE_END
    }

    fun rteEncryptionConfig(): RteEncryptionConfig {
        var rteConfig = RteEncryptionConfig()

        val config = encryptionConfigs
        config?.let {
            rteConfig.encryptionKey = config.encryptionKey
            rteConfig.encryptionMode = convertedEncryptionMode(config.encryptionMode)
        }

        return rteConfig
    }


    fun getPublishType(): AutoPublishItem {
        return if (autoPublish) {
            AutoPublishItem.AutoPublish
        } else {
            AutoPublishItem.NoOperation
        }
    }
}

data class RoomJoinOptions(
        val userUuid: String,
        /**用户可以传空,为空则使用roomImpl中默认的userName*/
        var userName: String?,
        val roleType: EduUserRole,
        val mediaOptions: RoomMediaOptions,
        /*用于RTC-SDK统计各个场景的使用情况*/
        var tag: Int? = null,
        var videoEncoderConfig: RteVideoEncoderConfig? = null,
        val latencyLevel: RteLatencyLevel
) {
    fun closeAutoPublish() {
        mediaOptions.autoPublish = false
    }
}

enum class AutoPublishItem(val value: Int) {
    NoOperation(0),
    AutoPublish(1),
    NoAutoPublish(2)
}
