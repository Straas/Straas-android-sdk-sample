package io.straas.android.sdk.circall.demo;

import android.databinding.ViewDataBinding;
import android.graphics.Bitmap;
import android.support.v7.widget.ActionMenuView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;

import java.util.List;

import io.straas.android.sdk.circall.CircallManager;
import io.straas.android.sdk.circall.CircallPlayerView;
import io.straas.android.sdk.circall.CircallPublishWithUrlConfig;
import io.straas.android.sdk.circall.CircallStream;
import io.straas.android.sdk.demo.R;
import io.straas.android.sdk.demo.databinding.ActivityIpcamBroadcastingBinding;

public class IPCamBroadcastingHostActivity extends CircallDemoBaseActivity {

    public static final String INTENT_PUBLISH_URL = "publish_url";

    private static final String TAG = IPCamBroadcastingHostActivity.class.getSimpleName();

    private ActivityIpcamBroadcastingBinding mBinding;

    //=====================================================================
    // Abstract methods
    //=====================================================================
    @Override
    protected String getTag() {
        return TAG;
    }

    @Override
    protected int getContentViewLayoutId() {
        return R.layout.activity_ipcam_broadcasting;
    }

    @Override
    protected void setBinding(ViewDataBinding binding) {
        mBinding = (ActivityIpcamBroadcastingBinding) binding;
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
}
