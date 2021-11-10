package io.agora.edu.launch;

import android.content.Context;

import androidx.annotation.NonNull;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Timer;
import java.util.TimerTask;

import io.agora.base.SharedPreferenceManager;
import io.agora.education.api.EduCallback;
import io.agora.education.api.base.EduError;
import io.agora.education.api.logger.DebugItem;
import io.agora.education.api.manager.EduManager;

public class AgoraEduLog {
    private static final String TAG = "AgoraEduLog";
    private final String uploadLog = "need-upload-log";

    private Timer timer;
    private TimerTask task;

    public void writeLogSign(@NonNull Context context, boolean upload) {
        SharedPreferenceManager.instance().put(context, uploadLog, upload);
    }

    private boolean needUploadLog(@NonNull Context context) {
        Boolean need = SharedPreferenceManager.instance().get(context, uploadLog, false);
        return need != null && need;
    }

    public void checkUploadLog(@NonNull Context context, EduManager manager, Object payload) {
        if (!needUploadLog(context)) {
            return;
        }

        timer = new Timer();
        task = new TimerTask() {
            @Override
            public void run() {
                manager.uploadDebugItem(DebugItem.LOG, payload, new EduCallback<String>() {
                    @Override
                    public void onSuccess(@Nullable String res) {
                        writeLogSign(context, false);
                        releaseTask();
                    }

                    @Override
                    public void onFailure(@NotNull EduError error) {
                        releaseTask();
                    }
                });
            }
        };
        timer.schedule(task, 30 * 1000);
    }

    private void releaseTask() {
        if (task != null) {
            task.cancel();
        }
        if (timer != null) {
            timer.purge();
            timer.cancel();
        }
        task = null;
        timer = null;
    }
}
