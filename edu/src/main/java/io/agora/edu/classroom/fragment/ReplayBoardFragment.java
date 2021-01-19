package io.agora.edu.classroom.fragment;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ProgressBar;

import com.google.android.exoplayer2.ui.PlayerView;
import com.herewhite.sdk.WhiteSdk;
import com.herewhite.sdk.WhiteSdkConfiguration;
import com.herewhite.sdk.WhiteboardView;
import io.agora.edu.R2;
import com.herewhite.sdk.domain.CameraBound;
import com.herewhite.sdk.domain.PlayerConfiguration;
import com.herewhite.sdk.domain.PlayerPhase;

import butterknife.BindView;
import butterknife.OnTouch;
import io.agora.edu.R;
import io.agora.edu.base.BaseFragment;
import io.agora.edu.classroom.widget.player.ReplayControlView;
import io.agora.whiteboard.netless.listener.ReplayEventListener;
import io.agora.whiteboard.netless.manager.ReplayManager;

import static io.agora.edu.launch.AgoraEduSDK.WHITEBOARD_END_TIME;
import static io.agora.edu.launch.AgoraEduSDK.WHITEBOARD_START_TIME;

public class ReplayBoardFragment extends BaseFragment implements ReplayEventListener {

    @BindView(R2.id.white_board_view)
    protected WhiteboardView white_board_view;
    @BindView(R2.id.replay_control_view)
    protected ReplayControlView replay_control_view;
    @BindView(R2.id.pb_loading)
    protected ProgressBar pb_loading;

    private String whiteBoardAppId;
    private WhiteSdk whiteSdk;
    private ReplayManager replayManager;
    private long startTime, endTime;

    private final double miniScale = 0.1d;
    private final double maxScale = 10d;

    public ReplayBoardFragment(String whiteBoardAppId) {
        this.whiteBoardAppId = whiteBoardAppId;
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_replay_board;
    }

    @Override
    protected void initData() {
        Bundle bundle = getArguments();
        if (bundle != null) {
            startTime = bundle.getLong(WHITEBOARD_START_TIME, 0);
            endTime = bundle.getLong(WHITEBOARD_END_TIME, 0);
        }
    }

    @Override
    protected void initView() {
        WhiteSdkConfiguration configuration = new WhiteSdkConfiguration(whiteBoardAppId);
        whiteSdk = new WhiteSdk(white_board_view, context, configuration);
        replayManager = new ReplayManager();
        replayManager.setListener(this);
    }

    @OnTouch(R2.id.white_board_view)
    boolean onTouch(View view, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP) {
            replay_control_view.setVisibility(View.VISIBLE);
        }
        return false;
    }

    public void initReplayWithRoomToken(String uuid, String roomToken) {
        if (TextUtils.isEmpty(uuid)) return;
        pb_loading.setVisibility(View.VISIBLE);
        PlayerConfiguration configuration = new PlayerConfiguration(uuid, roomToken);
        configuration.setBeginTimestamp(startTime);
        configuration.setDuration(endTime - startTime);
        configuration.setCameraBound(new CameraBound(miniScale, maxScale));
        replayManager.init(whiteSdk, configuration);
    }

    public void setPlayer(PlayerView view, String url) {
        replay_control_view.init(view, url, startTime, endTime);
    }

    public void releaseReplay() {
        replay_control_view.release();
        whiteSdk.releasePlayer();
    }

    @Override
    public void onPlayerPrepared(ReplayManager replayBoard) {
        replay_control_view.onPlayerPrepared(replayBoard);
    }

    @Override
    public void onPhaseChanged(PlayerPhase playerPhase) {
        replay_control_view.onPhaseChanged(playerPhase);
    }

    @Override
    public void onLoadFirstFrame() {
        pb_loading.setVisibility(View.GONE);
        replay_control_view.onLoadFirstFrame();
    }

    @Override
    public void onScheduleTimeChanged(long l) {
        replay_control_view.onScheduleTimeChanged(l);
    }

}
