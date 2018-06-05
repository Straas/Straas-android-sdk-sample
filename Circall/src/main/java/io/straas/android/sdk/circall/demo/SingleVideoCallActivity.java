package io.straas.android.sdk.circall.demo;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Toast;

import com.facebook.rebound.SimpleSpringListener;
import com.facebook.rebound.Spring;
import com.facebook.rebound.SpringSystem;
import com.facebook.rebound.SpringUtil;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.android.gms.tasks.Tasks;

import io.straas.android.sdk.circall.CircallConfig;
import io.straas.android.sdk.circall.CircallManager;
import io.straas.android.sdk.circall.CircallPlayConfig;
import io.straas.android.sdk.circall.CircallPublishConfig;
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
    private CircallStream mRemoteCircallStream;
    private long mRecordingStartTimeMillis;

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
                        item.setIcon(R.drawable.ic_switch_camera_focus);
                        mLocalCircallStream.switchCamera().addOnCompleteListener(success -> item.setIcon(R.drawable.ic_switch_camera));
                    }
                    break;
                case R.id.action_toggle_camera:
                    if (mLocalCircallStream != null) {
                        boolean isCameraOn = mLocalCircallStream.toggleCamera();
                        item.setIcon(isCameraOn ? R.drawable.ic_camera_off : R.drawable.ic_camera_on);
                        mBinding.setIsLocalVideoOff(!isCameraOn);
                    }
                    break;
                case R.id.action_toggle_mic:
                    if (mLocalCircallStream != null) {
                        boolean isMicOn = mLocalCircallStream.toggleMic();
                        item.setIcon(isMicOn ? R.drawable.ic_mic_off : R.drawable.ic_mic_on);
                    }
                    break;
                case R.id.action_screenshot:
                    if (mRemoteCircallStream == null) {
                        showScreenshotFailedDialog(R.string.screenshot_failed_message_two_way_not_ready);
                        break;
                    }

                    item.setIcon(R.drawable.ic_screenshot_focus);
                    mRemoteCircallStream.getVideoFrame().addOnSuccessListener(
                            SingleVideoCallActivity.this,
                            bitmap -> {
                                applySpringAnimation();
                                item.setIcon(R.drawable.ic_screenshot);
                                mBinding.screenshot.setImageBitmap(bitmap);
                                mHandler.postDelayed(() -> mBinding.screenshot.setImageBitmap(null), 3000);
                            });
                    break;
                default:
                    break;
            }
            return true;
        });

        mBinding.setState(STATE_IDLE);
        mBinding.setShowActionButtons(false);
    }

    private void applySpringAnimation() {
        SpringSystem springSystem = SpringSystem.create();
        Spring spring = springSystem.createSpring();
        spring.addListener(new SimpleSpringListener() {
            @Override
            public void onSpringUpdate(Spring spring) {
                float scale = (float) SpringUtil.mapValueFromRangeToRange(spring.getCurrentValue(), 0, 1, 1, 0.5);
                mBinding.screenshot.setScaleX(scale);
                mBinding.screenshot.setScaleY(scale);
            }
        });
        spring.setEndValue(1);
        mHandler.postDelayed(() -> spring.setEndValue(0), 1400);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_action , mBinding.actionMenuView.getMenu());
        return super.onCreateOptionsMenu(menu);
    }

    public void onActionRecord(View view) {
        if (mCircallManager == null || mCircallManager.getCircallState() != CircallManager.STATE_CONNECTED) {
            return;
        }
        if (mRemoteCircallStream == null) {
            showRecordingFailedDialog(R.string.recording_failed_message_two_way_not_ready);
            return;
        }

        if (mBinding.getIsRecording()) {
            mCircallManager.stopRecording(mRemoteCircallStream).addOnCompleteListener(this, task -> {
                if (task.isSuccessful()) {
                    mBinding.setIsRecording(false);
                    mBinding.actionRecord.setImageResource(R.drawable.ic_recording_off);
                }
            });
        } else {
            mCircallManager.startRecording(mRemoteCircallStream).addOnCompleteListener(this, task -> {
                if (task.isSuccessful()) {
                    mBinding.setIsRecording(true);
                    mBinding.actionRecord.setImageResource(R.drawable.ic_recording_on);

                    mRecordingStartTimeMillis = SystemClock.elapsedRealtime();
                    mMainThreadHandler.removeMessages(EVENT_UPDATE_RECORDING_TIME);
                    mMainThreadHandler.sendEmptyMessage(EVENT_UPDATE_RECORDING_TIME);
                } else {
                    showRecordingFailedDialog(R.string.recording_failed_message_not_authorized);
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
            return mCircallManager.prepare(getConfig(), mBinding.pipVideoView, getPlayConfig())
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
                Toast.makeText(getApplicationContext(), "join fails",
                        Toast.LENGTH_SHORT).show();
                finish();
                return Tasks.forException(task.getException());
            }

            return publish();
        });
    }

    private Task<Void> publish() {
        if (mCircallManager != null && mCircallManager.getCircallState() == CircallManager.STATE_CONNECTED) {
            final TaskCompletionSource<Void> source = new TaskCompletionSource<>();
            mCircallManager.publish(getPublishConfig()).addOnCompleteListener(task -> {
                mBinding.setState(STATE_CONNECTED);
                mBinding.setShowActionButtons(true);
                source.setResult(null);
            });
            return source.getTask();
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

    private CircallPlayConfig getPlayConfig() {
        return new CircallPlayConfig.Builder()
                .scalingMode(CircallPlayConfig.ASPECT_FILL)
                .build();
    }

    @Override
    public void onStreamAdded(CircallStream stream) {
        if (mCircallManager != null && stream != null) {
            mCircallManager.subscribe(stream);
        }
    }

    @Override
    public void onStreamSubscribed(CircallStream stream) {
        if (stream == null) {
            return;
        }

        mBinding.fullscreenVideoView.setVisibility(View.VISIBLE);
        stream.setRenderer(mBinding.fullscreenVideoView, getPlayConfig());
        mRemoteCircallStream = stream;
        mBinding.setState(STATE_TWO_WAY_VIDEO);
    }

    @Override
    public void onStreamRemoved(CircallStream stream) {
        mBinding.fullscreenVideoView.setVisibility(View.INVISIBLE);
        mBinding.setState(STATE_CONNECTED);
    }

    @Override
    public void onError(Exception error) {
        Log.e(TAG, "onError error:" + error);

        // For our 1:1 demo, video calling only invoking from outside page,
        // so just abort for this onError event to avoid showing freeze screen
        Toast.makeText(getApplicationContext(), "onError",
                Toast.LENGTH_SHORT).show();
        finish();
    }

    private void showScreenshotFailedDialog(int messageResId) {
        showFailedDialog(R.string.screenshot_failed_title, messageResId);
    }

    private void showRecordingFailedDialog(int messageResId) {
        showFailedDialog(R.string.recording_failed_title, messageResId);
    }

    private void showFailedDialog(int titleResId, int messageResId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.VideoCallDialogTheme);
        builder.setTitle(titleResId);
        builder.setMessage(messageResId);
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
