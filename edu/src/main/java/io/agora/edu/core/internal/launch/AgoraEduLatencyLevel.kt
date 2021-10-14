package io.agora.edu.core.internal.launch

import android.os.Parcel
import android.os.Parcelable
import io.agora.edu.core.internal.rte.data.RteLatencyLevel

enum class AgoraEduLatencyLevel(val value: Int) : Parcelable {
    // 极速直播
    AgoraEduLatencyLevelLow(1),

    // 互动直播
    AgoraEduLatencyLevelUltraLow(2);

    fun convert(): RteLatencyLevel {
        return when (this.value) {
            AgoraEduLatencyLevelLow.value -> RteLatencyLevel.RteLatencyLevelLow
            AgoraEduLatencyLevelUltraLow.value -> RteLatencyLevel.RteLatencyLevelUltraLow
            else -> RteLatencyLevel.RteLatencyLevelUltraLow
        }
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeInt(ordinal)
    }

    companion object {
        @JvmField val CREATOR: Parcelable.Creator<AgoraEduLatencyLevel?> = object : Parcelable.Creator<AgoraEduLatencyLevel?> {
            override fun createFromParcel(`in`: Parcel): AgoraEduLatencyLevel? {
                return AgoraEduLatencyLevel.values()[`in`.readInt()]
            }

            override fun newArray(size: Int): Array<AgoraEduLatencyLevel?> {
                return arrayOfNulls(size)
            }
        }
    }
}