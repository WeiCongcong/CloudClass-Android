package io.agora.edu.core.internal.edu.common.impl;

import androidx.annotation.Nullable;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

import io.agora.edu.core.internal.base.callback.ThrowableCallback;
import io.agora.edu.core.internal.base.network.BusinessException;
import io.agora.edu.core.internal.base.network.ResponseBody;
import io.agora.edu.core.internal.base.network.RetrofitManager;
import io.agora.edu.core.internal.edu.common.api.Base;
import io.agora.edu.core.internal.edu.common.api.HandsUp;
import io.agora.edu.core.internal.server.requests.http.retrofit.services.deprecated.HandsUpService;
import io.agora.edu.core.internal.launch.AgoraEduSDK;
import io.agora.edu.core.internal.framework.data.EduCallback;
import io.agora.edu.core.internal.framework.data.EduError;
import io.agora.edu.core.internal.server.struct.request.CoHostRequest;
import io.agora.edu.core.internal.server.struct.request.KickDirtyBody;
import io.agora.edu.core.internal.server.struct.request.KickRequest;
import io.agora.edu.core.internal.server.struct.request.RewardItem;
import io.agora.edu.core.internal.server.struct.request.RewardRequest;
import io.agora.edu.core.internal.server.struct.response.BaseResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HandsUpImpl extends Base implements HandsUp {
    private static final String TAG = "RaiseHandImpl";

    public HandsUpImpl(@NotNull String appId, @NotNull String roomUuid) {
        super(appId, roomUuid);
    }

    @Override
    public void applyHandsUp(@NotNull EduCallback<Boolean> callback) {
        RetrofitManager.instance().getService(AgoraEduSDK.baseUrl(), HandsUpService.class)
                .applyHandsUp(appId, roomUuid)
                .enqueue(new RetrofitManager.Callback(0, new ThrowableCallback<ResponseBody<String>>() {
                    @Override
                    public void onFailure(@Nullable Throwable throwable) {
                        if (throwable instanceof BusinessException) {
                            BusinessException e = (BusinessException) throwable;
                            callback.onFailure(new EduError(e.getCode(), e.getMessage()));
                        } else {
                            callback.onFailure(EduError.Companion.customMsgError(throwable.getMessage()));
                        }
                    }

                    @Override
                    public void onSuccess(@Nullable ResponseBody<String> res) {
                        if (res != null) {
                            callback.onSuccess(true);
                        } else {
                            callback.onFailure(EduError.Companion.customMsgError("response is null"));
                        }
                    }
                }));
    }

    @Override
    public void cancelApplyHandsUp(@NotNull EduCallback<Boolean> callback) {
        RetrofitManager.instance().getService(AgoraEduSDK.baseUrl(), HandsUpService.class)
                .cancelApplyHandsUp(appId, roomUuid)
                .enqueue(new RetrofitManager.Callback(0, new ThrowableCallback<ResponseBody<String>>() {
                    @Override
                    public void onFailure(@Nullable Throwable throwable) {
                        if (throwable instanceof BusinessException) {
                            BusinessException e = (BusinessException) throwable;
                            callback.onFailure(new EduError(e.getCode(), e.getMessage()));
                        } else {
                            callback.onFailure(EduError.Companion.customMsgError(throwable.getMessage()));
                        }
                    }

                    @Override
                    public void onSuccess(@Nullable ResponseBody<String> res) {
                        if (res != null) {
                            callback.onSuccess(true);
                        } else {
                            callback.onFailure(EduError.Companion.customMsgError("response is null"));
                        }
                    }
                }));
    }

    @Override
    public void exitHandsUp(EduCallback<Boolean> callback) {
        RetrofitManager.instance().getService(AgoraEduSDK.baseUrl(), HandsUpService.class)
                .exitHandsUp(appId, roomUuid)
                .enqueue(new RetrofitManager.Callback(0, new ThrowableCallback<ResponseBody<String>>() {
                    @Override
                    public void onFailure(@Nullable Throwable throwable) {
                        if (throwable instanceof BusinessException) {
                            BusinessException e = (BusinessException) throwable;
                            callback.onFailure(new EduError(e.getCode(), e.getMessage()));
                        } else {
                            callback.onFailure(EduError.Companion.customMsgError(throwable.getMessage()));
                        }
                    }

                    @Override
                    public void onSuccess(@Nullable ResponseBody<String> res) {
                        if (res != null) {
                            callback.onSuccess(true);
                        } else {
                            callback.onFailure(EduError.Companion.customMsgError("response is null"));
                        }
                    }
                }));
    }

    @Override
    public void acceptCoHost(String userUuid, EduCallback<Boolean> callback) {
        CoHostRequest request = new CoHostRequest(userUuid);
        RetrofitManager.instance().getService(AgoraEduSDK.baseUrl(), HandsUpService.class)
                .acceptCoHost(appId, roomUuid, request)
                .enqueue(new Callback<BaseResponseBody>() {
                    @Override
                    public void onResponse(Call<BaseResponseBody> call,
                                           Response<BaseResponseBody> response) {
                        BaseResponseBody body = response.body();
                        if (body != null) {
                            if (body.getCode() == 0) {
                                callback.onSuccess(true);
                            } else {
                                callback.onFailure(new EduError(body.getCode(), body.getMsg()));
                            }
                        } else {
                            callback.onFailure(EduError.Companion.customMsgError("response is null"));
                        }
                    }

                    @Override
                    public void onFailure(Call<BaseResponseBody> call, Throwable t) {
                        callback.onFailure(EduError.Companion.customMsgError(t.getMessage()));
                    }
                });
    }

    @Override
    public void endCoHost(String userUuid, EduCallback<Boolean> callback) {
        CoHostRequest request = new CoHostRequest(userUuid);
        RetrofitManager.instance().getService(AgoraEduSDK.baseUrl(), HandsUpService.class)
                .endCoHost(appId, roomUuid, request)
                .enqueue(new Callback<BaseResponseBody>() {
                    @Override
                    public void onResponse(Call<BaseResponseBody> call,
                                           Response<BaseResponseBody> response) {
                        BaseResponseBody body = response.body();
                        if (body != null) {
                            if (body.getCode() == 0) {
                                callback.onSuccess(true);
                            } else {
                                callback.onFailure(new EduError(body.getCode(), body.getMsg()));
                            }
                        } else {
                            callback.onFailure(EduError.Companion.customMsgError("response is null"));
                        }
                    }

                    @Override
                    public void onFailure(Call<BaseResponseBody> call, Throwable t) {
                        callback.onFailure(EduError.Companion.customMsgError(t.getMessage()));
                    }
                });
    }

    @Override
    public void endCoHost(EduCallback<Boolean> callback) {
        RetrofitManager.instance().getService(AgoraEduSDK.baseUrl(), HandsUpService.class)
                .endCoHost(appId, roomUuid)
                .enqueue(new Callback<BaseResponseBody>() {
                    @Override
                    public void onResponse(Call<BaseResponseBody> call,
                                           Response<BaseResponseBody> response) {
                        BaseResponseBody body = response.body();
                        if (body != null) {
                            if (body.getCode() == 0) {
                                callback.onSuccess(true);
                            } else {
                                callback.onFailure(new EduError(body.getCode(), body.getMsg()));
                            }
                        } else {
                            callback.onFailure(EduError.Companion.customMsgError("response is null"));
                        }
                    }

                    @Override
                    public void onFailure(Call<BaseResponseBody> call, Throwable t) {
                        callback.onFailure(EduError.Companion.customMsgError(t.getMessage()));
                    }
                });
    }

    @Override
    public void reward(String userUuid, int rewardCount, EduCallback<Boolean> callback) {
        ArrayList<RewardItem> list = new ArrayList<>();
        list.add(new RewardItem(userUuid, rewardCount));
        RewardRequest request = new RewardRequest(list);
        RetrofitManager.instance().getService(AgoraEduSDK.baseUrl(), HandsUpService.class)
                .reward(appId, roomUuid, request)
                .enqueue(new Callback<BaseResponseBody>() {
                    @Override
                    public void onResponse(Call<BaseResponseBody> call,
                                           Response<BaseResponseBody> response) {
                        BaseResponseBody body = response.body();
                        if (body != null) {
                            if (body.getCode() == 0) {
                                callback.onSuccess(true);
                            } else {
                                callback.onFailure(new EduError(body.getCode(), body.getMsg()));
                            }
                        } else {
                            callback.onFailure(EduError.Companion.customMsgError("response is null"));
                        }
                    }

                    @Override
                    public void onFailure(Call<BaseResponseBody> call, Throwable t) {
                        callback.onFailure(EduError.Companion.customMsgError(t.getMessage()));
                    }
                });
    }

    @Override
    public void kick(String userUuid, boolean forever, EduCallback<Boolean> callback) {
        KickRequest request = new KickRequest(new KickDirtyBody(forever ? 1 : 0, forever ? 86400L : 0));
        RetrofitManager.instance().getService(AgoraEduSDK.baseUrl(), HandsUpService.class)
                .kick(appId, roomUuid, userUuid, request)
                .enqueue(new Callback<BaseResponseBody>() {
                    @Override
                    public void onResponse(Call<BaseResponseBody> call,
                                           Response<BaseResponseBody> response) {
                        BaseResponseBody body = response.body();
                        if (body != null) {
                            if (body.getCode() == 0) {
                                callback.onSuccess(true);
                            } else {
                                callback.onFailure(new EduError(body.getCode(), body.getMsg()));
                            }
                        } else {
                            callback.onFailure(EduError.Companion.customMsgError("response is null"));
                        }
                    }

                    @Override
                    public void onFailure(Call<BaseResponseBody> call, Throwable t) {
                        callback.onFailure(EduError.Companion.customMsgError(t.getMessage()));
                    }
                });
    }
}
