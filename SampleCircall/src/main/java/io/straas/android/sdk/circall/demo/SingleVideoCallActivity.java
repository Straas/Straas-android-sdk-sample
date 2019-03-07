package io.straas.android.sdk.circall.demo;

import android.databinding.ViewDataBinding;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.ActionMenuView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;

import java.util.List;

import io.straas.android.sdk.circall.CircallConfig;
import io.straas.android.sdk.circall.CircallManager;
import io.straas.android.sdk.circall.CircallPlayerView;
import io.straas.android.sdk.circall.CircallPublishConfig;
import io.straas.android.sdk.circall.CircallRecordingStreamMetadata;
import io.straas.android.sdk.circall.CircallStream;
import io.straas.android.sdk.demo.databinding.ActivitySingleVideoCallBinding;

import static io.straas.android.sdk.circall.CircallPlayerView.ASPECT_FILL;

public class SingleVideoCallActivity extends CircallDemoBaseActivity {

    private static final String TAG = SingleVideoCallActivity.class.getSimpleName();

    private ActivitySingleVideoCallBinding mBinding;
    private CircallStream mLocalCircallStream;
    private String mRecordingId = "";

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

    @Override
    protected Task<CircallStream> prepare() {
        if (mCircallManager != null && mCircallManager.getCircallState() == CircallManager.STATE_IDLE) {
            return mCircallManager.prepareForCameraCapture(this, getConfig())
                    .addOnSuccessListener(circallStream -> {
                        mBinding.pipVideoView.setCircallStream(circallStream);
                        mLocalCircallStream = circallStream;
                    });
        }
        return Tasks.forException(new IllegalStateException());
    }

    //=====================================================================
    // Optional implementation
    //=====================================================================
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getRemoteStreamView().setScalingMode(ASPECT_FILL);
    }

    @Override
    protected void onConnected() {
        publish();
    }

    @Override
    protected void setState(int state) {
        super.setState(state);
        mBinding.setState(state);
    }

    @Override
    protected void setIsSubscribing(boolean isSubscribing) {
        super.setIsSubscribing(isSubscribing);
        mBinding.setIsSubscribing(isSubscribing);
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
                    item.setIcon(isCameraOn ? R.drawable.ic_camera_on : R.drawable.ic_camera_off);
                    mBinding.setIsLocalVideoOff(!isCameraOn);
                }
                return true;
            case R.id.action_toggle_mic:
                if (mLocalCircallStream != null) {
                    boolean isMicOn = mLocalCircallStream.toggleMic();
                    item.setIcon(isMicOn ? R.drawable.ic_mic_on : R.drawable.ic_mic_off );
                }
                return true;
            default:
                break;
        }
        return false;
    }

    @Override
    protected List<Task<Void>> tasksBeforeDestroy() {
        List<Task<Void>> list = super.tasksBeforeDestroy();
        list.add(stopRecording());
        list.add(unsubscribe());
        list.add(unpublish());
        return list;
    }

    @Override
    protected String getEndTitle() {
        return getResources().getString(R.string.end_single_call_confirmation_title);
    }

    @Override
    protected String getEndMessage() {
        return getResources().getString(R.string.end_single_call_confirmation_message);
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
                        mBinding.setIsRecording(true);
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

    //================================================================
    // Internal methods
    //================================================================
    public void onActionRecord(View view) {
        if (mCircallManager == null || mCircallManager.getCircallState() != CircallManager.STATE_CONNECTED) {
            return;
        }
        if (mRemoteCircallStream == null) {
            showRecordingFailedDialog(R.string.recording_failed_message_no_remote_stream);
            return;
        }

        if (!TextUtils.isEmpty(mRecordingId)) {
            mCircallManager.stopRecording(mRemoteCircallStream, mRecordingId).addOnCompleteListener(this, task -> {
                if (task.isSuccessful()) {
                    mBinding.setIsRecording(false);
                    mRecordingId = "";
                }
            });
        } else {
            mCircallManager.startRecording(mRemoteCircallStream).addOnCompleteListener(this, task -> {
                if (task.isSuccessful()) {
                    mRecordingId = task.getResult();
                    mBinding.setIsRecording(true);
                } else {
                    showRecordingFailedDialog(R.string.recording_failed_message_not_authorized);
                }
            });
        }
    }

    private void publish() {
        if (mCircallManager != null && mCircallManager.getCircallState() == CircallManager.STATE_CONNECTED) {
            mCircallManager.publishWithCameraCapture(getPublishConfig()).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    setShowActionButtons(true);
                } else {
                    Log.w(getTag(), "Publish fails: " + task.getException());
                    finish();
                }
            });
        }
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

    private Task<Void> stopRecording() {
        if (TextUtils.isEmpty(mRecordingId)) {
            return Tasks.forException(new IllegalStateException());
        }
        return mCircallManager.stopRecording(mRemoteCircallStream, mRecordingId)
                .addOnCompleteListener(this, task -> mRecordingId = "");
    }
}
