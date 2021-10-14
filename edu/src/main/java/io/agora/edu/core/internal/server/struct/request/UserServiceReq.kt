package io.agora.edu.core.internal.server.struct.request

data class CoHostRequest(
    val toUserUuid: String
)

data class RewardRequest(
    val rewardDetails: List<RewardItem>
)

data class RewardItem(
    val userUuid: String,
    val changeReward: Int
)

data class KickRequest(
    val dirty: KickDirtyBody
)

data class KickDirtyBody(
    val state: Int,
    val duration: Long
)