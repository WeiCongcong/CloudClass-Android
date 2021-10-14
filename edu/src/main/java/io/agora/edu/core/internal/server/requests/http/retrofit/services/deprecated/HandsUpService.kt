package io.agora.edu.core.internal.server.requests.http.retrofit.services.deprecated

import io.agora.edu.core.internal.edu.common.bean.ResponseBody
import io.agora.edu.core.internal.server.struct.request.CoHostRequest
import io.agora.edu.core.internal.server.struct.request.KickRequest
import io.agora.edu.core.internal.server.struct.request.RewardRequest
import io.agora.edu.core.internal.server.struct.response.BaseResponseBody
import retrofit2.Call
import retrofit2.http.*

interface HandsUpService {
    @POST("edu/apps/{appId}/v2/rooms/{roomUUid}/processes/handsUp/progress")
    fun applyHandsUp(
            @Path("appId") appId: String?,
            @Path("roomUUid") roomUuid: String?
    ): Call<ResponseBody<Int?>?>?

    @DELETE("edu/apps/{appId}/v2/rooms/{roomUUid}/processes/handsUp/progress")
    fun cancelApplyHandsUp(
            @Path("appId") appId: String?,
            @Path("roomUUid") roomUuid: String?
    ): Call<ResponseBody<Int?>?>?

    @DELETE("edu/apps/{appId}/v2/rooms/{roomUUid}/processes/handsUp/acceptance")
    fun exitHandsUp(
            @Path("appId") appId: String?,
            @Path("roomUUid") roomUuid: String?
    ): Call<ResponseBody<Int?>?>?

    @POST("edu/apps/{appId}/v2/rooms/{roomUuid}/processes/handsUp/acceptance")
    fun acceptCoHost(@Path("appId") appId: String,
                     @Path("roomUuid") roomUuid: String,
                     @Body body: CoHostRequest
    ): Call<BaseResponseBody>

    @DELETE("edu/apps/{appId}/v2/rooms/{roomUuid}/processes/handsUp/acceptance")
    fun endCoHost(@Path("appId") appId: String,
                  @Path("roomUuid") roomUuid: String
    ): Call<BaseResponseBody>

    @HTTP(method = "DELETE", hasBody = true,
        path = "edu/apps/{appId}/v2/rooms/{roomUuid}/processes/handsUp/acceptance")
    fun endCoHost(@Path("appId") appId: String,
                  @Path("roomUuid") roomUuid: String,
                  @Body body: CoHostRequest
    ): Call<BaseResponseBody>

    @POST("edu/apps/{appId}/v2/rooms/{roomUuid}/rewards")
    fun reward(@Path("appId") appId: String,
               @Path("roomUuid") roomUuid: String,
               @Body body: RewardRequest
    ): Call<BaseResponseBody>

    @POST("edu/apps/{appId}/v2/rooms/{roomUuid}/users/{userUuid}/exit")
    fun kick(@Path("appId") appId: String,
             @Path("roomUuid") roomUuid: String,
             @Path("userUuid") userUuid: String,
             @Body body: KickRequest? = null
    ): Call<BaseResponseBody>
}