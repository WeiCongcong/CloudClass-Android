package io.agora.agoraeducore.core.internal.edu.common.bean.roompre

import io.agora.agoraeducore.core.internal.base.bean.JsonBean

class DeviceStateBean(
        val camera: Int?,
        val facing: Int?,
        val mic: Int?,
        val speaker: Int?
) : JsonBean() {
    companion object {
        const val DEVICES = "device"
    }
}