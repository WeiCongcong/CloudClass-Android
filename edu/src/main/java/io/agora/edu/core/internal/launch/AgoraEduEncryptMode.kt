package io.agora.edu.core.internal.launch

enum class AgoraEduEncryptMode(val value: Int) {
    NONE(0),

    // only for rtc
    AES_128_XTS(1),

    // only for rtc
    AES_128_ECB(2),

    // only for rtc
    AES_256_XTS(3),

    // for rtc and rte
    SM4_128_ECB(4),

    // only for rtc
    AES_128_GCM(5),

    // only for rtc
    AES_256_GCM(6),

    // for rtc and rte
    AES_128_GCM2(7),

    // for rtc and rte
    AES_256_GCM2(8);
}