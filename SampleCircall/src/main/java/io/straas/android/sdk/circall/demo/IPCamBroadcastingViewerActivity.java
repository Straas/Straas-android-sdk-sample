package io.straas.android.sdk.circall.demo;

import android.databinding.ViewDataBinding;
import android.graphics.Bitmap;
import android.support.v7.widget.ActionMenuView;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;

import java.util.List;

import io.straas.android.sdk.circall.CircallManager;
import io.straas.android.sdk.circall.CircallPlayerView;
import io.straas.android.sdk.circall.demo.databinding.ActivityIpcamBroadcastingViewerBinding;

public class IPCamBroadcastingViewerActivity extends CircallDemoBaseActivity {

    private static final String TAG = IPCamBroadcastingViewerActivity.class.getSimpleName();

    private ActivityIpcamBroadcastingViewerBinding mBinding;

    //=====================================================================
    // Abstract methods
    //=====================================================================
    @Override
    protected String getTag() {
        return TAG;
    }

    @Override
    protected int getContentViewLayoutId() {
        return R.layout.activity_ipcam_broadcasting_viewer;
    }

    @Override
    protected void setBinding(ViewDataBinding binding) {
        mBinding = (ActivityIpcamBroadcastingViewerBinding) binding;
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
        return list;
    }
}
