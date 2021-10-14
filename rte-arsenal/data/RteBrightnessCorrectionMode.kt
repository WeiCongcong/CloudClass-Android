package io.agora.edu.core.internal.rte.data

enum class RteBrightnessCorrectionMode(val value: Int) {
    // 为0时是自动模式，sdk内部暗光增强算法会自动检测亮度，如果检测当前亮度正常，
    // 可能不会启动亮度校正，如果是这个场景，调用后画面没有变化，没有效果
    Auto(0),

    // 为1时是手动模式，sdk内部暗光增强算法会启动亮度校正，画面会有亮度变化
    Manual(1);
}