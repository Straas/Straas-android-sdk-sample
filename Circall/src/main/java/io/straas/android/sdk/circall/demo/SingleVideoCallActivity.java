package io.straas.android.sdk.circall.demo;

import android.databinding.ViewDataBinding;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.ActionMenuView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.android.gms.tasks.Tasks;

import io.straas.android.sdk.circall.CircallConfig;
import io.straas.android.sdk.circall.CircallManager;
import io.straas.android.sdk.circall.CircallPlayerView;
import io.straas.android.sdk.circall.CircallPublishConfig;
import io.straas.android.sdk.circall.CircallRecordingStreamMetadata;
import io.straas.android.sdk.circall.CircallStream;
import io.straas.android.sdk.circall.CircallToken;
import io.straas.android.sdk.demo.R;
import io.straas.android.sdk.demo.databinding.ActivitySingleVideoCallBinding;

public class SingleVideoCallActivity extends CircallDemoBaseActivity {

    private static final String TAG = SingleVideoCallActivity.class.getSimpleName();

    private static final int EVENT_UPDATE_RECORDING_TIME = 101;

    private ActivitySingleVideoCallBinding mBinding;
    private CircallStream mLocalCircallStream;
    private String mRecordingId = "";
    private long mRecordingStartTimeMillis;

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

        CircallManager.initialize().continueWithTask(task -> {
            if (!task.isSuccessful()) {
                Log.e(getTag(), "init fail: " + task.getException());
                finish();
                return Tasks.forException(new RuntimeException());
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
    }

    //=====================================================================
    // Abstract methods
    //=====================================================================
    @Override
    protected String getTag() {
        return TAG;
    }

    @Override
    protected int getContentViewLayoutId() {
        return R.layout.activity_single_video_call;
    }

    @Override
    protected void setBinding(ViewDataBinding binding) {
        mBinding = (ActivitySingleVideoCallBinding) binding;
    }

    @Override
    protected void scaleScreenShotView(float scale) {
        mBinding.screenshot.setScaleX(scale);
        mBinding.screenshot.setScaleY(scale);
    }

    @Override
    protected void setScreenShotView(Bitmap bitmap) {
        mBinding.screenshot.setImageBitmap(bitmap);
    }

    @Override
    protected ActionMenuView getActionMenuView() {
        return mBinding.actionMenuView;
    }

    @Override
    protected Toolbar getToolbar() {
        return mBinding.toolbar;
    }

    @Override
    protected CircallPlayerView getRemoteStreamView() {
        return mBinding.fullscreenVideoView;
    }

    @Override
    protected void setShowActionButtons(boolean show) {
        mBinding.setShowActionButtons(show);
    }

    @Override
    public void onShowActionButtonsToggled(View view) {
        mBinding.setShowActionButtons(!mBinding.getShowActionButtons());
    }

    //=====================================================================
    // Optional implementation
    //=====================================================================
    @Override
    protected void setState(int state) {
        super.setState(state);
        mBinding.setState(state);
    }

    @Override
    protected int getMenuRes() {
        return R.menu.single_video_call_menu_action;
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        if (super.onMenuItemClick(item)) {
            return true;
        }
        switch (item.getItemId()) {
            case R.id.action_switch_camera:
                if (mLocalCircallStream != null) {
                    item.setIcon(R.drawable.ic_switch_camera_focus);
                    mLocalCircallStream.switchCamera().addOnCompleteListener(success -> item.setIcon(R.drawable.ic_switch_camera));
                }
                return true;
            case R.id.action_toggle_camera:
                if (mLocalCircallStream != null) {
                    boolean isCameraOn = mLocalCircallStream.toggleCamera();
                    item.setIcon(isCameraOn ? R.drawable.ic_camera_off : R.drawable.ic_camera_on);
                    mBinding.setIsLocalVideoOff(!isCameraOn);
                }
                return true;
            case R.id.action_toggle_mic:
                if (mLocalCircallStream != null) {
                    boolean isMicOn = mLocalCircallStream.toggleMic();
                    item.setIcon(isMicOn ? R.drawable.ic_mic_off : R.drawable.ic_mic_on);
                }
                return true;
            default:
                break;
        }
        return false;
    }

    //================================================================
    // EventListener
    //================================================================
    @Override
    public void onStreamSubscribed(CircallStream stream) {
        if (stream == null) {
            return;
        }

        super.onStreamSubscribed(stream);
        mBinding.setIsRemoteVideoOff(!stream.isVideoEnabled());
        mCircallManager.getRecordingStreamMetadata().addOnCompleteListener(this, task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                for (CircallRecordingStreamMetadata recordingStream : task.getResult()) {
                    if (TextUtils.equals(recordingStream.getStreamId(), stream.getStreamId())) {
                        mRecordingId = recordingStream.getRecordingId();
                        showRecordingStartedFlashingUi();
                        break;
                    }
                }
            }
        });
    }

    @Override
    public void onStreamUpdated(CircallStream stream) {
        if (stream == null) {
            return;
        }

        mBinding.setIsRemoteVideoOff(!stream.isVideoEnabled());
    }

    public void onActionRecord(View view) {
        if (mCircallManager == null || mCircallManager.getCircallState() != CircallManager.STATE_CONNECTED) {
            return;
        }
        if (mRemoteCircallStream == null) {
            showRecordingFailedDialog(R.string.recording_failed_message_two_way_not_ready);
            return;
        }

        if (!TextUtils.isEmpty(mRecordingId)) {
            mCircallManager.stopRecording(mRemoteCircallStream, mRecordingId).addOnCompleteListener(this, task -> {
                if (task.isSuccessful()) {
                    mBinding.setIsRecording(false);
                    mBinding.actionRecord.setImageResource(R.drawable.ic_recording_off);
                    mRecordingId = "";
                }
            });
        } else {
            mCircallManager.startRecording(mRemoteCircallStream).addOnCompleteListener(this, task -> {
                if (task.isSuccessful()) {
                    mRecordingId = task.getResult();
                    showRecordingStartedFlashingUi();
                } else {
                    showRecordingFailedDialog(R.string.recording_failed_message_not_authorized);
                }
            });
        }
    }

    @Override
    public void onDestroy() {
        destroyCircallManager();

        super.onDestroy();
    }

    private Task<CircallStream> prepare() {
        if (mCircallManager != null && mCircallManager.getCircallState() == CircallManager.STATE_IDLE) {
            return mCircallManager.prepareForCameraCapture(getConfig(), mBinding.pipVideoView, getPlayConfig())
                    .addOnCompleteListener(this, task -> {
                        if (task.isSuccessful()) {
                            mLocalCircallStream = task.getResult();
                        } else {
                            Log.e(getTag(), "Prepare fails " + task.getException());
                        }
                    });
        }
        return Tasks.forException(new IllegalStateException());
    }

    private void join(CircallToken token) {
        setState(STATE_CONNECTING);
        mCircallManager.connect(token).continueWithTask(task -> {
            if (!task.isSuccessful()) {
                Log.e(getTag(), "join fails: " + task.getException());
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
            mCircallManager.publishWithCameraCapture(getPublishConfig()).addOnCompleteListener(task -> {
                setState(STATE_CONNECTED);
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

    private void showRecordingFailedDialog(int messageResId) {
        showFailedDialog(R.string.recording_failed_title, messageResId);
    }

    private void showRecordingStartedFlashingUi() {
        mBinding.setIsRecording(true);
        mBinding.actionRecord.setImageResource(R.drawable.ic_recording_on);

        mRecordingStartTimeMillis =SystemClock.elapsedRealtime();
        mMainThreadHandler.removeMessages(EVENT_UPDATE_RECORDING_TIME);
        mMainThreadHandler.sendEmptyMessage(EVENT_UPDATE_RECORDING_TIME);
    }

    @Override
    public void onBackPressed() {
        if (mState >= STATE_CONNECTED) {
            showEndCircallConfirmationDialog();
        } else {
            super.onBackPressed();
        }
    }

    private void showEndCircallConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.CircallDialogTheme);
        builder.setTitle(R.string.end_single_call_confirmation_title);
        builder.setMessage(R.string.end_single_call_confirmation_message);
        builder.setPositiveButton(android.R.string.ok, (dialog, which) -> {
            finish();
        });
        builder.setNegativeButton(android.R.string.cancel, null);
        final AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void destroyCircallManager() {
        if (mCircallManager == null) {
            return;
        }

        Tasks.whenAll(stopRecording(), unsubscribe(), unpublish()).addOnCompleteListener(task -> {
            mCircallManager.destroy();
            mCircallManager = null;
        });
    }

    private Task<Void> stopRecording() {
        if (TextUtils.isEmpty(mRecordingId)) {
            return Tasks.forException(new IllegalStateException());
        }
        return mCircallManager.stopRecording(mRemoteCircallStream, mRecordingId)
                .addOnCompleteListener(this, task -> mRecordingId = "");
    }

    private Task<Void> unsubscribe() {
        return (mBinding.getState() == STATE_SUBSCRIBED && mRemoteCircallStream != null)
                ? mCircallManager.unsubscribe(mRemoteCircallStream)
                : Tasks.forException(new IllegalStateException());
    }

    private Task<Void> unpublish() {
        return (mBinding.getState() >= STATE_PUBLISHED)
                ? mCircallManager.unpublish()
                : Tasks.forException(new IllegalStateException());
    }
}
