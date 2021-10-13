package io.agora.education.api.user.data

import io.agora.education.api.stream.data.EduStreamEvent

enum class EduUserRole(var value: Int) {
    INVISIBLE(0),
    TEACHER(1),
    STUDENT(2),
    ASSISTANT(3);

    companion object {
        fun fromValue(value: Int): EduUserRole {
            return when (value) {
                INVISIBLE.value -> {
                    INVISIBLE
                }
                TEACHER.value -> {
                    TEACHER
                }
                STUDENT.value -> {
                    STUDENT
                }
                ASSISTANT.value -> {
                    ASSISTANT
                }
                else -> INVISIBLE
            }
        }
    }
}

open class EduBaseUserInfo(
        val userUuid: String,
        val userName: String,
        val role: EduUserRole) {

    override fun equals(other: Any?): Boolean {
        if (other == null || other !is EduBaseUserInfo) {
            return false
        }
        return (other.userUuid == this.userUuid && other.userName == this.userName && other.role == this.role)
    }

    override fun hashCode(): Int {
        return super.hashCode()
    }

    fun copy(): EduBaseUserInfo {
        return EduBaseUserInfo(this.userUuid, this.userName, this.role)
    }
}

open class EduUserInfo(
        userUuid: String,
        userName: String,
        role: EduUserRole,
        var isChatAllowed: Boolean?
) : EduBaseUserInfo(userUuid, userName, role) {
    /**用户主流的uuid(无主流则为null)*/
    lateinit var streamUuid: String
    var userProperties: MutableMap<String, Any> = mutableMapOf()
}

open class EduLocalUserInfo(
        userUuid: String,
        userName: String,
        role: EduUserRole,
        isChatAllowed: Boolean?,
        var userToken: String?,
        // 防止套娃crash CMDDataMergeProcessor > updateStreamWithAction > Log.e(TAG, "本地流缓存:" + Gson().toJson(streamInfoList))
        @Transient
        var streams: MutableList<EduStreamEvent>
) : EduUserInfo(userUuid, userName, role, isChatAllowed)

enum class EduChatState(var value: Int) {
    NotAllow(1),
    Allow(0)
}

enum class EduUserStateChangeType {
    Chat,
}

enum class EduUserLeftType(var value: Int) {
    Normal(1),
    KickOff(2)
}
