package io.straas.android.sdk.circall.demo;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;

import io.straas.android.sdk.circall.CircallConfig;
import io.straas.android.sdk.circall.CircallManager;
import io.straas.android.sdk.circall.CircallPlayConfig;
import io.straas.android.sdk.circall.CircallPublishConfig;
import io.straas.android.sdk.circall.CircallStatsReport;
import io.straas.android.sdk.circall.CircallStream;
import io.straas.android.sdk.circall.CircallToken;
import io.straas.android.sdk.circall.interfaces.EventListener;
import io.straas.android.sdk.demo.R;
import io.straas.android.sdk.demo.databinding.ActivitySingleVideoCallBinding;

public class SingleVideoCallActivity extends AppCompatActivity implements EventListener {

    public static final String INTENT_CIRCALL_TOKEN = "circall_token";

    private static final String TAG = SingleVideoCallActivity.class.getSimpleName();

    public static final int STATE_IDLE = 0;
    public static final int STATE_CONNECTING = 1;
    public static final int STATE_CONNECTED = 2;
    public static final int STATE_TWO_WAY_VIDEO = 3;

    private static final int EVENT_UPDATE_RECORDING_TIME = 101;

    private ActivitySingleVideoCallBinding mBinding;
    private CircallManager mCircallManager;
    private CircallStream mLocalCircallStream;
    private boolean mIsConnected = false;
    private long mRecordingStartTimeMillis;

    private CircallStream mRemoteStream;
    private Handler mHandler = new Handler();

    private final Handler mMainThreadHandler = new Handler(Looper.getMainLooper()) {
        public void handleMessage(Message msg) {
            switch(msg.what) {
                case EVENT_UPDATE_RECORDING_TIME:
                    mBinding.setSeconds((SystemClock.elapsedRealtime() - mRecordingStartTimeMillis) / 1000);
                    if (mBinding.getIsRecording()) {
                        mMainThreadHandler.removeMessages(EVENT_UPDATE_RECORDING_TIME);
                        mMainThreadHandler.sendEmptyMessageDelayed(EVENT_UPDATE_RECORDING_TIME, 1000);
                    }
                    break;
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Utils.requestFullscreenMode(this);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_single_video_call);

        CircallManager.initialize().continueWithTask(task -> {
            if (!task.isSuccessful()) {
                Log.e(TAG, "init fail: " + task.getException());
            }

            mCircallManager = task.getResult();
            mCircallManager.addEventListener(SingleVideoCallActivity.this);
            return prepare();
        }).addOnSuccessListener(circallStream -> {
            String token = getIntent().getStringExtra(INTENT_CIRCALL_TOKEN);
            android.util.Log.d("jason", "single token:" + token);

            if (!TextUtils.isEmpty(token)) {
                join(new CircallToken(token));
            } else {
                Toast.makeText(getApplicationContext(), "Start circall fails due to empty token",
                        Toast.LENGTH_SHORT).show();
                finish();
            }
        });

        setSupportActionBar(mBinding.toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        mBinding.actionMenuView.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case R.id.action_switch_camera:
                    if (mLocalCircallStream != null) {
                        item.setIcon(R.drawable.ic_switch_camera_focus_24dp);
                        mLocalCircallStream.switchCamera().addOnCompleteListener(success -> item.setIcon(R.drawable.ic_switch_camera_24dp));
                    }
                    break;
                case R.id.action_toggle_camera:
                    if (mLocalCircallStream != null) {
                        boolean isCameraOn = mLocalCircallStream.toggleCamera();
                        item.setIcon(isCameraOn ? R.drawable.ic_videocam_off_24dp : R.drawable.ic_videocam_on_24dp);
                        mBinding.setIsLocalVideoOff(!isCameraOn);
                    }
                    break;
                case R.id.action_toggle_mic:
                    if (mLocalCircallStream != null) {
                        boolean isMicOn = mLocalCircallStream.toggleMic();
                        item.setIcon(isMicOn ? R.drawable.ic_mic_off_24dp : R.drawable.ic_mic_on_24dp);
                    }
                    break;
                case R.id.action_screenshot:
                    if (mLocalCircallStream != null) {
                        item.setIcon(R.drawable.ic_screenshot_focus);
                        mLocalCircallStream.getVideoFrame().addOnSuccessListener(
                                SingleVideoCallActivity.this,
                                bitmap -> {
                                    Log.d(TAG, "onSuccess bitmap:" + bitmap);
                                    item.setIcon(R.drawable.ic_screenshot);
                                    mBinding.screenshot.setImageBitmap(bitmap);
                                    mHandler.postDelayed(() -> mBinding.screenshot.setImageBitmap(null), 3000);
                                });
                    }
                    break;
                default:
                    break;
            }
            return true;
        });

        mBinding.setState(STATE_IDLE);
        mBinding.setShowActionButtons(false);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_action , mBinding.actionMenuView.getMenu());
        return super.onCreateOptionsMenu(menu);
    }

    public void onActionRecord(View view) {
        if (mCircallManager == null || mLocalCircallStream == null ||
                mCircallManager.getCircallState() != CircallManager.STATE_CONNECTED) {
            return;
        }

        if (mBinding.getIsRecording()) {
            mCircallManager.stopRecording(mLocalCircallStream).addOnCompleteListener(this, task -> {
                if (task.isSuccessful()) {
                    mBinding.setIsRecording(false);
                    mBinding.actionRecord.setImageResource(R.drawable.ic_video_call_24dp);
                }
            });
        } else {
            mCircallManager.startRecording(mLocalCircallStream).addOnCompleteListener(this, task -> {
                if (task.isSuccessful()) {
                    mBinding.setIsRecording(true);
                    mBinding.actionRecord.setImageResource(R.drawable.ic_slow_motion_video_24dp);

                    mRecordingStartTimeMillis = SystemClock.elapsedRealtime();
                    mMainThreadHandler.removeMessages(EVENT_UPDATE_RECORDING_TIME);
                    mMainThreadHandler.sendEmptyMessage(EVENT_UPDATE_RECORDING_TIME);
                } else {
                    showRecordingFailedDialog();
                }
            });
        }
    }

    public void onShowActionButtonsToggled(View view) {
        boolean isShowing = mBinding.getShowActionButtons();
        mBinding.setShowActionButtons(!isShowing);
    }

    @Override
    public void onDestroy() {
        if (mCircallManager != null) {
            mCircallManager.destroy();
        }

        super.onDestroy();
    }

    private Task<CircallStream> prepare() {
        if (mCircallManager != null && mCircallManager.getCircallState() == CircallManager.STATE_IDLE) {
            return mCircallManager.prepare(getConfig(), mBinding.pipVideoView, CircallPlayConfig.ASPECT_FILL)
                    .addOnCompleteListener(this, task -> {
                        if (task.isSuccessful()) {
                            mLocalCircallStream = task.getResult();
                        } else {
                            Log.e(TAG, "Prepare fails " + task.getException());
                        }
                    });
        }
        return Tasks.forException(new IllegalStateException());
    }

    private void join(CircallToken token) {
        mBinding.setState(STATE_CONNECTING);
        mCircallManager.connect(token).continueWithTask(task -> {
            if (!task.isSuccessful()) {
                Log.e(TAG, "join fails: " + task.getException());
                return Tasks.forException(task.getException());
            }

            mIsConnected = true;
            return publish();
        });
    }

    private Task<String> publish() {
        if (mCircallManager != null && mCircallManager.getCircallState() == CircallManager.STATE_CONNECTED) {
            mCircallManager.publish(getPublishConfig()).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    mBinding.setState(STATE_CONNECTED);
                    mBinding.setShowActionButtons(true);
                }
            });
        }

        return Tasks.forException(new IllegalStateException());
    }

    private CircallConfig getConfig() {
        return new CircallConfig.Builder()
                .videoResolution(1280, 720)
                .build();
    }

    private CircallPublishConfig getPublishConfig() {
        return new CircallPublishConfig.Builder()
                .videoMaxBitrate(780 * 1024)
                .audioMaxBitrate(100 * 1024)
                .build();
    }

    @Override
    public void onStreamAdded(CircallStream stream) {
        Log.d(TAG, "onStreamAdded: " + stream);

        if (mCircallManager != null && stream != null) {
            mCircallManager.subscribe(stream);
        }
    }

    @Override
    public void onStreamSubscribed(CircallStream stream) {
        if (stream == null) {
            return;
        }

        Log.d(TAG, "onStreamSubscribed setRenderer to fullscreenVideoView: ");

        mBinding.fullscreenVideoView.setVisibility(View.VISIBLE);
        stream.setRenderer(mBinding.fullscreenVideoView, CircallPlayConfig.ASPECT_FIT);
        mRemoteStream = stream;
        mBinding.setState(STATE_TWO_WAY_VIDEO);
    }

    @Override
    public void onStreamRemoved(CircallStream stream) {
        mBinding.fullscreenVideoView.setVisibility(View.INVISIBLE);
        mBinding.setState(STATE_CONNECTED);
    }

    @Override
    public void onError(Exception error) {
    }

    private void showRecordingFailedDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.VideoCallDialogTheme);
        builder.setTitle(R.string.recording_failed_title);
        builder.setMessage(R.string.recording_failed_message);
        builder.setPositiveButton(android.R.string.ok, null);
        final AlertDialog dialog = builder.create();
        dialog.show();
    }

    @Override
    public void onBackPressed() {
        if (mBinding.getState() == STATE_CONNECTED || mBinding.getState() == STATE_TWO_WAY_VIDEO) {
            showEndCircallConfirmationDialog();
        } else {
            super.onBackPressed();
        }
    }

    private void showEndCircallConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.VideoCallDialogTheme);
        builder.setTitle(R.string.end_circall_confirmation_title);
        builder.setMessage(R.string.end_circall_confirmation_message);
        builder.setPositiveButton(android.R.string.ok, (dialog, which) -> {
            if (mCircallManager != null) {
                mCircallManager.destroy();
                mCircallManager = null;
            }

            finish();
        });
        builder.setNegativeButton(android.R.string.cancel, null);
        final AlertDialog dialog = builder.create();
        dialog.show();
    }
}
