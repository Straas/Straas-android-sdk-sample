package io.straas.android.media.demo;

import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.widget.RelativeLayout.LayoutParams;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import io.straas.android.media.demo.widget.StraasPlayerView;
import io.straas.android.media.demo.widget.ui.AspectRatioFrameLayout;
import io.straas.android.sdk.demo.R;
import io.straas.android.sdk.media.StraasMediaCore;
import io.straas.android.sdk.messaging.ChatroomManager;
import io.straas.android.sdk.messaging.ui.ChatroomInputView;
import io.straas.android.sdk.messaging.ui.ChatroomOutputView;
import io.straas.android.sdk.demo.identity.MemberIdentity;
import master.flame.danmaku.ui.widget.DanmakuSurfaceView;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;

public class LiveDanmakuActivity extends AppCompatActivity {
    private static final String TAG = LiveDanmakuActivity.class.getSimpleName();
    private static final String CHATROOM_NAME = "test_chatroom";
    private static final String LIVE_ID = "";
    private StraasMediaCore mStraasMediaCore;
    private ChatroomOutputView mChatroomOutputView;
    private ChatroomInputView mChatroomInputView;
    private ChatroomManager mChatroomManager;
    private DanmakuManager mDanmakuManager;
    private AspectRatioFrameLayout mAspectRatioFrameLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_live_danmaku);
        mAspectRatioFrameLayout = findViewById(R.id.aspectRatioFrameLayout);

        StraasPlayerView playerView = findViewById(R.id.straas);
        playerView.initialize(this);

        mStraasMediaCore = new StraasMediaCore(playerView, MemberIdentity.ME,
                new MediaBrowserCompat.ConnectionCallback() {
                    @Override
                    public void onConnected() {
                        getMediaControllerCompat().getTransportControls().playFromMediaId(
                                StraasMediaCore.LIVE_ID_PREFIX + LIVE_ID, null);
                    }
                });
        getMediaBrowser().connect();

        mChatroomOutputView = findViewById(R.id.chat_room);
        mChatroomInputView = findViewById(android.R.id.inputArea);
        handleConfiguration(getResources().getConfiguration());
        mDanmakuManager = new DanmakuManager(new DanmakuSurfaceView(this));
        playerView.addView(mDanmakuManager.getDanmakuView(),
                playerView.indexOfChild(playerView.getVideoContainer()) + 1,
                new LayoutParams(MATCH_PARENT, MATCH_PARENT));

        ChatroomManager.initialize()
                .continueWithTask(new Continuation<ChatroomManager, Task<Void>>() {
                    @Override
                    public Task<Void> then(@NonNull Task<ChatroomManager> task) throws Exception {
                        if (task.isSuccessful()) {
                            mChatroomManager = task.getResult();
                            mChatroomManager.addEventListener(mDanmakuManager);
                            return mChatroomManager.connect(CHATROOM_NAME, MemberIdentity.ME);
                        }
                        throw task.getException();
                    }
                })
                .addOnSuccessListener(this, new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        mChatroomOutputView.setChatroomManager(mChatroomManager);
                        mChatroomInputView.setChatroomManager(mChatroomManager);
                    }
                });
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (getMediaControllerCompat() != null) {
            getMediaControllerCompat().getTransportControls().play();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (getMediaControllerCompat() != null) {
            if (isFinishing()) {
                getMediaControllerCompat().getTransportControls().stop();
            } else {
                getMediaControllerCompat().getTransportControls().pause();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        getMediaBrowser().disconnect();
        mChatroomOutputView.disconnect();
    }

    private MediaBrowserCompat getMediaBrowser() {
        return mStraasMediaCore.getMediaBrowser();
    }

    private MediaControllerCompat getMediaControllerCompat() {
        return MediaControllerCompat.getMediaController(this);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        handleConfiguration(newConfig);
    }

    private void handleConfiguration(Configuration config) {
        if (config.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            getSupportActionBar().hide();
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
            mChatroomOutputView.setVisibility(View.GONE);
            mChatroomInputView.setVisibility(View.GONE);
            mAspectRatioFrameLayout.setAspectRatio((float)getResources().getDisplayMetrics().widthPixels /
                    getResources().getDisplayMetrics().heightPixels);
        } else {
            mChatroomOutputView.setVisibility(View.VISIBLE);
            mChatroomInputView.setVisibility(View.VISIBLE);
            mAspectRatioFrameLayout.setAspectRatio(1.778f);
            getSupportActionBar().show();
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
    }

}
