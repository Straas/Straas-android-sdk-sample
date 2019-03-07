package io.straas.android.sdk.circall.demo;

import android.databinding.ViewDataBinding;
import android.graphics.Bitmap;
import android.support.v7.widget.ActionMenuView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;

import java.util.List;

import io.straas.android.sdk.circall.CircallManager;
import io.straas.android.sdk.circall.CircallPlayerView;
import io.straas.android.sdk.circall.CircallPublishWithUrlConfig;
import io.straas.android.sdk.circall.CircallStream;
import io.straas.android.sdk.demo.databinding.ActivityIpcamBroadcastingHostBinding;

public class IPCamBroadcastingHostActivity extends CircallDemoBaseActivity {

    public static final String INTENT_PUBLISH_URL = "publish_url";

    private static final String TAG = IPCamBroadcastingHostActivity.class.getSimpleName();

    private ActivityIpcamBroadcastingHostBinding mBinding;
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
        return R.layout.activity_ipcam_broadcasting_host;
    }

    @Override
    protected void setBinding(ViewDataBinding binding) {
        mBinding = (ActivityIpcamBroadcastingHostBinding) binding;
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
    protected Task<?> prepare() {
        if (mCircallManager != null && mCircallManager.getCircallState() == CircallManager.STATE_IDLE) {
            return mCircallManager.prepareForUrl(getApplicationContext());
        }
        return Tasks.forException(new IllegalStateException());
    }

    //=====================================================================
    // Optional implementation
    //=====================================================================
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
    protected List<Task<Void>> tasksBeforeDestroy() {
        List<Task<Void>> list = super.tasksBeforeDestroy();
        list.add(unsubscribe());
        list.add(unpublish());
        return list;
    }

    //=====================================================================
    // EventListener
    //=====================================================================
    @Override
    public void onStreamAdded(CircallStream stream) {
        //Do nothing because a host only subscribes his own stream.
    }

    @Override
    public void onStreamPublished(CircallStream stream) {
        if (mCircallManager != null && stream != null) {
            mCircallManager.subscribe(stream);
        }
    }

    //=====================================================================
    // Internal methods
    //=====================================================================
    private void publish() {
        if (mCircallManager != null && mCircallManager.getCircallState() == CircallManager.STATE_CONNECTED) {
            mCircallManager.publishWithUrl(getPublishConfig()).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    setShowActionButtons(true);
                } else {
                    Log.w(getTag(), "Publish fails: " + task.getException());
                    finish();
                }
            });
        }
    }

    private CircallPublishWithUrlConfig getPublishConfig() {
        return new CircallPublishWithUrlConfig.Builder()
                .url(getIntent().getStringExtra(INTENT_PUBLISH_URL)).build();
    }

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

    private void showRecordingFailedDialog(int messageResId) {
        showFailedDialog(R.string.recording_failed_title, messageResId);
    }
}
