package io.agora.agoraeduuikit.impl.whiteboard.netless.manager;

import android.os.Handler;
import android.os.Looper;

import com.google.gson.Gson;
import com.herewhite.sdk.Room;
import com.herewhite.sdk.RoomListener;
import com.herewhite.sdk.RoomParams;
import com.herewhite.sdk.WhiteSdk;
import com.herewhite.sdk.domain.AnimationMode;
import com.herewhite.sdk.domain.BroadcastState;
import com.herewhite.sdk.domain.CameraConfig;
import com.herewhite.sdk.domain.CameraState;
import com.herewhite.sdk.domain.GlobalState;
import com.herewhite.sdk.domain.MemberState;
import com.herewhite.sdk.domain.Promise;
import com.herewhite.sdk.domain.RoomPhase;
import com.herewhite.sdk.domain.RoomState;
import com.herewhite.sdk.domain.SDKError;
import com.herewhite.sdk.domain.Scene;
import com.herewhite.sdk.domain.SceneState;
import com.herewhite.sdk.domain.ViewMode;
import com.herewhite.sdk.domain.WindowAppParam;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import io.agora.agoraeduuikit.impl.whiteboard.netless.annotation.Appliance;
import io.agora.agoraeduuikit.impl.whiteboard.netless.listener.BoardEventListener;

import static android.text.TextUtils.isEmpty;
import static io.agora.agoraeducore.core.internal.education.impl.Constants.AgoraLog;

public class BoardRoomImpl extends NetlessManager<Room> implements BoardRoom, RoomListener {
    public static final String TAG = "BoardRoom";

    private String appliance;
    private int[] strokeColor;
    private double strokeWidth = -100f, textSize = -100f;
    private MemberState memberState = new MemberState();
    private Boolean disableDeviceInputs;
    private Boolean disableCameraTransform;
    private Boolean writable;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private BoardEventListener listener;
    private boolean joinSuccess = false;

    private int joinFailedRetry = 2;
    private int connectFailedRetry = 2;
    private WhiteSdk whiteSdk;
    private RoomParams roomParams;

    public void setListener(BoardEventListener listener) {
        this.listener = listener;
    }

    public void init(WhiteSdk sdk, RoomParams params) {
        this.whiteSdk = sdk;
        this.roomParams = params;
        sdk.joinRoom(params, BoardRoomImpl.this, promise);
    }

    public void setAppliance(@Appliance String appliance) {
        if (t != null) {
            MemberState state = new MemberState();
            state.setCurrentApplianceName(appliance);
            t.setMemberState(state);
        }
        this.appliance = appliance;
    }

    public String getAppliance() {
        if (t != null) {
            return t.getMemberState().getCurrentApplianceName();
        }
        return null;
    }

    public void setStrokeColor(int[] color) {
        if (t != null) {
            MemberState state = new MemberState();
            state.setStrokeColor(color);
            t.setMemberState(state);
        }
        this.strokeColor = color;
    }

    public int[] getStrokeColor() {
        if (t != null) {
            return t.getMemberState().getStrokeColor();
        }
        return null;
    }

    public void setStrokeWidth(double width) {
        if (t != null) {
            MemberState state = new MemberState();
            state.setStrokeWidth(width);
            t.setMemberState(state);
        }
        this.strokeWidth = width;
    }

    public Double getStrokeWidth() {
        if (t != null) {
            return t.getMemberState().getStrokeWidth();
        }
        return null;
    }

    public void setTextSize(double size) {
        if (t != null) {
            MemberState state = new MemberState();
            state.setTextSize(size);
            t.setMemberState(state);
        }
        this.textSize = size;
    }

    public Double getTextSize() {
        if (t != null) {
            return t.getMemberState().getTextSize();
        }
        return null;
    }

    @Override
    public void setMemState(@NotNull MemberState state) {
        if (!isEmpty(state.getCurrentApplianceName())) {
            this.appliance = state.getCurrentApplianceName();
            this.memberState.setCurrentApplianceName(state.getCurrentApplianceName());
        }
        if (state.getStrokeColor() != null) {
            this.strokeColor = state.getStrokeColor();
            this.memberState.setStrokeColor(state.getStrokeColor());
        }
        if (state.getStrokeWidth() != 0.0) {
            this.strokeWidth = state.getStrokeWidth();
            this.memberState.setStrokeWidth(state.getStrokeWidth());
        }
        if (state.getTextSize() != 0.0) {
            this.textSize = state.getTextSize();
            this.memberState.setTextSize(state.getTextSize());
        }
        if (t != null) {
            t.setMemberState(memberState);
        }
    }

    @Override
    public @Nullable MemberState getMemberState() {
        if (t != null) {
            return t.getMemberState();
        }
        return null;
    }

    public void setSceneIndex(int index) {
        if (t != null && !isDisableDeviceInputs()) {
            t.setSceneIndex(index, new Promise<Boolean>() {
                @Override
                public void then(Boolean aBoolean) {
                }

                @Override
                public void catchEx(SDKError t) {
                }
            });
        }
    }

    public RoomState getBoardState() {
        if (t != null) {
            return t.getRoomState();
        }
        return null;
    }

    public int getSceneCount() {
        if (t != null) {
            return t.getScenes().length;
        }
        return 0;
    }

    public void zoom(double scale) {
        if (t != null && !isDisableCameraTransform()) {
            CameraConfig cameraConfig = new CameraConfig();
            cameraConfig.setScale(scale);
            t.moveCamera(cameraConfig);
        }
    }

    public void moveCamera(CameraConfig cameraConfig) {
        if (t != null) {
            t.moveCamera(cameraConfig);
        }
    }

    public void scalePptToFit() {
        if (t != null) {
            t.scalePptToFit();
            t.scaleIframeToFit();
        }
    }

    public void follow(boolean follow) {
        AgoraLog.e(TAG + "->follow: " + follow);
        if (t != null) {
            AgoraLog.e(TAG + "->setViewMode: " + (follow ? ViewMode.Follower : ViewMode.Freedom));
            t.setViewMode(follow ? ViewMode.Follower : ViewMode.Freedom);
        }
    }

    public void scalePptToFit(AnimationMode mode) {
        if (t != null) {
            t.scalePptToFit(mode);
            t.scaleIframeToFit();
        }
    }

    public void hasBroadcaster(Promise<Boolean> promise) {
        if (promise == null) {
            return;
        }
        if (t != null) {
            t.getRoomState(new Promise<RoomState>() {
                @Override
                public void then(RoomState roomState) {
                    BroadcastState state = roomState.getBroadcastState();
                    promise.then(state != null && state.getBroadcasterId() != null);
                }

                @Override
                public void catchEx(SDKError t) {
                    promise.catchEx(t);
                }
            });
        } else {
            promise.then(false);
        }
    }

    public boolean hasBroadcaster() {
        if (t != null && t.getRoomState() != null) {
            BroadcastState state = t.getRoomState().getBroadcastState();
            return state != null && state.getBroadcasterId() != null;
        }
        return false;
    }

    public double getZoomScale() {
        if (t != null) {
            return t.getZoomScale();
        }
        return 1.0;
    }

    public void pptPreviousStep() {
        if (t != null && !isDisableDeviceInputs()) {
            t.pptPreviousStep();
        }
    }

    public void pptNextStep() {
        if (t != null && !isDisableDeviceInputs()) {
            t.pptNextStep();
        }
    }

    public void getRoomPhase(Promise<RoomPhase> promise) {
        if (t != null) {
            t.getRoomPhase(promise);
        } else {
            if (promise != null) {
                promise.then(RoomPhase.disconnected);
            }
        }
    }

    public void refreshViewSize() {
        if (t != null) {
            t.refreshViewSize();
        }
    }

    public void removeScenes(String dirOrPath) {
        if (t != null) {
            t.removeScenes(dirOrPath);
        }
    }

    public void putScenes(String dir, Scene[] scenes, int index) {
        if (t != null) {
            t.putScenes(dir, scenes, index);
        }
    }

    public void setScenePath(String path, final Promise<Boolean> promise) {
        if (t != null) {
            t.setScenePath(path, promise);
        }
    }

    public void setScenePath(String path) {
        if (t != null) {
            t.setScenePath(path);
        }
    }

    public void getSceneState(Promise<SceneState> promise) {
        if (t != null) {
            t.getSceneState(promise);
        }
    }

    public void disableDeviceInputs(boolean disabled) {
        if (t != null) {
            t.disableDeviceInputs(disabled);
        }
        disableDeviceInputs = disabled;
    }

    public void disableDeviceInputsTemporary(boolean disabled) {
        if (t != null) {
            t.disableDeviceInputs(disabled);
        }
    }

    public boolean isDisableDeviceInputs() {
        return disableDeviceInputs == null ? false : disableDeviceInputs;
    }

    public void disableCameraTransform(boolean disabled) {
        if (t != null) {
            t.disableCameraTransform(disabled);
        }
        disableCameraTransform = disabled;
    }

    public boolean isDisableCameraTransform() {
        return disableCameraTransform == null ? false : disableCameraTransform;
    }

    public void setWritable(boolean writable) {
        if (t != null) {
            MemberState memberState = new MemberState();
            memberState.setCurrentApplianceName(appliance);
            memberState.setStrokeColor(strokeColor);
            memberState.setTextSize(textSize);
            memberState.setStrokeWidth(strokeWidth);
            t.setWritable(writable, new Promise<Boolean>() {
                @Override
                public void then(Boolean aBoolean) {
                    AgoraLog.i(TAG + "->setWritable-then:" + aBoolean);
                    if (aBoolean) {
                        // restore memberState
                        t.setMemberState(memberState);
                    }
                }

                @Override
                public void catchEx(SDKError t) {
                    AgoraLog.e(TAG + "->setWritable-catchEx:" + t.getJsStack());
                }
            });
        }
        this.writable = writable;
    }

    @Override
    public void setWritable(boolean writable, Promise<Boolean> promise) {
        if (t != null) {
            MemberState memberState = new MemberState();
            memberState.setCurrentApplianceName(appliance);
            memberState.setStrokeColor(strokeColor);
            memberState.setTextSize(textSize);
            memberState.setStrokeWidth(strokeWidth);
            t.setWritable(writable, new Promise<Boolean>() {
                @Override
                public void then(Boolean aBoolean) {
                    AgoraLog.i(TAG + "->setWritable-then:" + aBoolean);
                    if (aBoolean) {
                        // restore memberState
                        t.setMemberState(memberState);
                    }
                    BoardRoomImpl.this.writable = writable;
                    promise.then(aBoolean);
                }

                @Override
                public void catchEx(SDKError t) {
                    AgoraLog.e(TAG + "->setWritable-catchEx:" + t.getJsStack());
                    promise.catchEx(t);
                }
            });
        }
    }

    public void disconnect() {
        if (t != null) {
            t.disconnect();
        }
    }

    public void setGlobalState(GlobalState state) {
        if (t != null) {
            t.setGlobalState(state);
        }
    }

    @Override
    public void setWindowApp(@NotNull WindowAppParam param, @Nullable Promise<String> promise) {
        if (t != null) {
            t.addApp(param, promise);
        }
    }

    @Override
    public void changeMixingState(int state, int errorCode) {
        whiteSdk.getAudioMixerImplement().setMediaState(state, errorCode);
    }

    public void disconnect(Promise<Object> promise) {
        if (t != null) {
            t.disconnect(promise);
        }
    }

    @Override
    public void onPhaseChanged(RoomPhase phase) {
        AgoraLog.i(TAG + "->onPhaseChanged:" + phase.name());
        if (listener != null) {
            handler.post(() -> listener.onRoomPhaseChanged(phase));
        }
    }

    @Override
    public void onDisconnectWithError(Exception e) {
        AgoraLog.e(TAG + "->onDisconnectWithError:" + e.getMessage());
        if (listener != null) {
            handler.post(() -> listener.onDisconnectWithError(e));
        }
    }

    @Override
    public void onKickedWithReason(String reason) {
        AgoraLog.w(TAG + "->onKickedWithReason:" + reason);
    }

    @Override
    public void onRoomStateChanged(RoomState modifyState) {
        AgoraLog.d(TAG + "->onRoomStateChanged:" + new Gson().toJson(modifyState));
        if (modifyState.getBroadcastState() != null && modifyState.getBroadcastState()
            .getBroadcasterId() == null) {
            AgoraLog.i(TAG + "->onRoomStateChanged:teacher is not here, scalePptToFit");
            scalePptToFit(AnimationMode.Continuous);
        }
        if (listener != null) {
            GlobalState state = modifyState.getGlobalState();
            if (state != null) {
                handler.post(() -> listener.onGlobalStateChanged(state));
            }
            MemberState memberState = modifyState.getMemberState();
            if (memberState != null) {
                handler.post(() -> listener.onMemberStateChanged(memberState));
            }
            SceneState sceneState = modifyState.getSceneState();
            if (sceneState != null) {
                handler.post(() -> listener.onSceneStateChanged(sceneState));
            }

            CameraState cameraState = modifyState.getCameraState();
            if (cameraState != null) {
                handler.post(() -> listener.onCameraStateChanged(cameraState));
            }
        }
    }

    @Override
    public void onCanUndoStepsUpdate(long canUndoSteps) {
        AgoraLog.w(TAG + "->onCanUndoStepsUpdate:" + canUndoSteps);
    }

    @Override
    public void onCanRedoStepsUpdate(long canRedoSteps) {
        AgoraLog.w(TAG + "->onCanRedoStepsUpdate:" + canRedoSteps);
    }

    @Override
    public void onCatchErrorWhenAppendFrame(long userId, Exception error) {
        AgoraLog.e(TAG + "->onCatchErrorWhenAppendFrame->userId:" + userId + "error:" + error.getMessage());
    }

    @Override
    void onSuccess(Room room) {
        AgoraLog.d(TAG + "->onSuccess->room:" + roomParams.toString());
        if (appliance != null) {
            setAppliance(appliance);
        }
        if (strokeColor != null) {
            setStrokeColor(strokeColor);
        }
        if (strokeWidth != -100f) {
            setStrokeWidth(strokeWidth);
        }
        if (textSize != -100f) {
            setTextSize(textSize);
        }
        if (disableDeviceInputs != null) {
            disableDeviceInputs(disableDeviceInputs);
        }
        if (disableCameraTransform != null) {
            disableCameraTransform(disableCameraTransform);
        }
        if (writable != null) {
            setWritable(writable);
        }
        if (listener != null) {
            if (!joinSuccess) {
                joinSuccess = true;
                listener.onJoinSuccess(getBoardState().getGlobalState());
            }
            listener.onSceneStateChanged(room.getSceneState());
        }
    }

    @Override
    void onFail(SDKError error) {
        AgoraLog.e(TAG + "->onFail:" + error.toString());
        String sdkInitFailed = "sdk init failed jsStack: Unknow stack";
        String magixConnectFailed = "magix connect fail";
        if (error.toString().contains(sdkInitFailed) && joinFailedRetry > 0) {
            if (whiteSdk == null && roomParams == null) {
                return;
            }
            AgoraLog.i(TAG + "->joinRoom-retry-sdkInitFailed");
            this.whiteSdk.joinRoom(this.roomParams, this, promise);
            joinFailedRetry--;
            return;
        } else if (error.toString().contains(magixConnectFailed) && connectFailedRetry > 0) {
            if (whiteSdk == null && roomParams == null) {
                return;
            }
            AgoraLog.i(TAG + "->joinRoom-retry-magixConnectFailed");
            this.whiteSdk.joinRoom(this.roomParams, this, promise);
            connectFailedRetry--;
            return;
        }
        listener.onJoinFail(error);
    }
}
