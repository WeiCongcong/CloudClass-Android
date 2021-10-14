package io.agora.edu.core.internal.rte.data

import io.agora.rtc.internal.EncryptionConfig


data class RteEncryptionConfig(
        var encryptionMode: RteEncryptionMode = RteEncryptionMode.AES_128_GCM2,
        var encryptionKey: String? = null
) {

    fun convert(): EncryptionConfig {
        val config = EncryptionConfig()
        config.encryptionMode = encryptionMode.convert()
        config.encryptionKey = encryptionKey
        return config
    }
}

enum class RteEncryptionMode(val value: Int) {
    AES_128_XTS(1),
    AES_128_ECB(2),
    AES_256_XTS(3),
    SM4_128_ECB(4),
    AES_128_GCM(5),
    AES_256_GCM(6),
    AES_128_GCM2(7),
    AES_256_GCM2(8),
    MODE_END(9);

    fun convert(): EncryptionConfig.EncryptionMode {
        return when (this.value) {
            AES_128_XTS.value -> EncryptionConfig.EncryptionMode.AES_128_XTS
            AES_128_ECB.value -> EncryptionConfig.EncryptionMode.AES_128_ECB
            AES_256_XTS.value -> EncryptionConfig.EncryptionMode.AES_256_XTS
            SM4_128_ECB.value -> EncryptionConfig.EncryptionMode.SM4_128_ECB
            AES_128_GCM.value -> EncryptionConfig.EncryptionMode.AES_128_GCM
            AES_256_GCM.value -> EncryptionConfig.EncryptionMode.AES_256_GCM
            AES_128_GCM2.value -> EncryptionConfig.EncryptionMode.AES_128_GCM2
            AES_256_GCM2.value -> EncryptionConfig.EncryptionMode.AES_256_GCM2
            else -> EncryptionConfig.EncryptionMode.MODE_END
        }
    }
}
