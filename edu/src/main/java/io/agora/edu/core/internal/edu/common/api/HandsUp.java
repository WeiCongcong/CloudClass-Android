package io.agora.edu.core.internal.edu.common.api;

import io.agora.edu.core.internal.framework.data.EduCallback;

public interface HandsUp {

    void applyHandsUp(EduCallback<Boolean> callback);

    // cancel before teacher handle
    void cancelApplyHandsUp(EduCallback<Boolean> callback);

    void exitHandsUp(EduCallback<Boolean> callback);

    void acceptCoHost(String userUuid, EduCallback<Boolean> callback);

    void endCoHost(String userUuid, EduCallback<Boolean> callback);

    void endCoHost(EduCallback<Boolean> callback);

    void reward(String userUuid, int rewardCount, EduCallback<Boolean> callback);

    void kick(String userUuid, boolean forever, EduCallback<Boolean> callback);
}
