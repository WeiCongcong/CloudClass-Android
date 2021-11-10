package io.agora.edu.common.api;

import io.agora.education.api.EduCallback;

public interface HandsUp {

    void applyHandsUp(EduCallback<Boolean> callback, int timeout);

    void applyHandsWave(EduCallback<Boolean> callback, int timeout);

    // cancel before teacher handle
    void cancelApplyHandsUp(EduCallback<Boolean> callback);

    void exitHandsUp(EduCallback<Boolean> callback);
}
