package io.agora.edu.core.internal.launch

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class AgoraEduStreamState(
        var videoState: Int,
        var audioState: Int
) : Parcelable

enum class AgoraEduStreamStatus(val value: Int) {
    DisEnabled(0),
    Enabled(1);
}

