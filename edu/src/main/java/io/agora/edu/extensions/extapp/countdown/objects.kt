package io.agora.edu.extensions.extapp.countdown

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

enum class CountdownLaunchStatus(val value: Int) {
    Init(0),
    Started(1),
    Paused(2)
}


data class CountdownStatus(
        val state: String,
        val startTime: String?,
        val pauseTime: String?,
        val duration: String?
) {
    fun convert(): Map<String, Any?>? {
        val json = Gson().toJson(this)
        val map: Map<String, Any?>? = Gson().fromJson(json, object : TypeToken<Map<String, Any?>?>() {}.type)
        return map
    }
}