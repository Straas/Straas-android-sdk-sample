package io.straas.android.sdk.circall.demo;

import android.databinding.ViewDataBinding;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;

import io.straas.android.sdk.circall.CircallManager;
import io.straas.android.sdk.circall.CircallPlayConfig;
import io.straas.android.sdk.circall.CircallStream;
import io.straas.android.sdk.circall.CircallToken;
import io.straas.android.sdk.circall.interfaces.EventListener;
import io.straas.android.sdk.demo.R;
import io.straas.android.sdk.demo.databinding.ActivityIpcamBroadcastingBinding;

public class IPCamBroadcastingViewerActivity extends CircallDemoBaseActivity implements EventListener {

    private static final String TAG = IPCamBroadcastingViewerActivity.class.getSimpleName();

    private ActivityIpcamBroadcastingBinding mBinding;

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
            mCircallManager.addEventListener(this);
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
                case R.id.action_screenshot:
                    if (mRemoteCircallStream == null) {
                        showScreenshotFailedDialog(R.string.screenshot_failed_message);
                        break;
                    }

                    item.setIcon(R.drawable.ic_screenshot_focus);
                    mRemoteCircallStream.getVideoFrame().addOnSuccessListener(
                            IPCamBroadcastingViewerActivity.this,
                            bitmap -> {
                                mCapturedPicture = bitmap;
                                storePicture();
                                item.setIcon(R.drawable.ic_screenshot);
                            });
                    break;
                default:
                    break;
            }
            return true;
        });

        setState(STATE_IDLE);
        mBinding.setShowActionButtons(false);
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

    //=====================================================================
    // Optional implementation
    //=====================================================================
    @Override
    protected void setState(int state) {
        super.setState(state);
        mBinding.setState(state);
    }

    private Task<Void> prepare() {
        if (mCircallManager != null && mCircallManager.getCircallState() == CircallManager.STATE_IDLE) {
            return mCircallManager.prepareForUrl(getApplicationContext())
                    .addOnFailureListener(this, e -> Log.e(getTag(), "Prepare fails " + e));
        }
        return Tasks.forException(new IllegalStateException());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.ipcam_broadcasting_menu_action, mBinding.actionMenuView.getMenu());
        return super.onCreateOptionsMenu(menu);
    }

    public void onShowActionButtonsToggled(View view) {
        boolean isShowing = mBinding.getShowActionButtons();
        mBinding.setShowActionButtons(!isShowing);
    }

    @Override
    public void onDestroy() {
        destroyCircallManager();

        super.onDestroy();
    }

    private void join(CircallToken token) {
        setState(STATE_CONNECTING);
        mCircallManager.connect(token).addOnSuccessListener(aVoid -> {
            setState(STATE_CONNECTED);
        }).addOnFailureListener(e -> {
            Log.e(getTag(), "join fails: " + e);
            Toast.makeText(getApplicationContext(), "join fails",
                    Toast.LENGTH_SHORT).show();
            finish();
        });
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
    public void onStreamPublished(CircallStream stream) {
    }

    @Override
    public void onStreamSubscribed(CircallStream stream) {
        if (stream == null) {
            return;
        }

        mBinding.fullscreenVideoView.setVisibility(View.VISIBLE);
        stream.setRenderer(mBinding.fullscreenVideoView, getPlayConfig());
        mRemoteCircallStream = stream;
        setState(STATE_SUBSCRIBED);
    }

    @Override
    public void onStreamRemoved(CircallStream stream) {
        mBinding.fullscreenVideoView.setVisibility(View.INVISIBLE);
        setState(STATE_CONNECTED);
    }

    @Override
    public void onStreamUpdated(CircallStream stream) {
        if (stream == null) {
            return;
        }
    }

    @Override
    public void onError(Exception error) {
        Log.e(getTag(), "onError error:" + error);

        Toast.makeText(getApplicationContext(), "onError",
                Toast.LENGTH_SHORT).show();
        finish();
    }

    @Override
    public void onBackPressed() {
        if (mBinding.getState() == STATE_CONNECTED || mBinding.getState() == STATE_SUBSCRIBED) {
            showEndCircallConfirmationDialog();
        } else {
            super.onBackPressed();
        }
    }

    private void showEndCircallConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.CircallDialogTheme);
        builder.setTitle(R.string.end_circall_confirmation_title);
        builder.setMessage(R.string.end_circall_confirmation_message);
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

        unsubscribe().addOnCompleteListener(task -> {
            mCircallManager.destroy();
            mCircallManager = null;
        });
    }

    private Task<Void> unsubscribe() {
        return (mBinding.getState() == STATE_SUBSCRIBED && mRemoteCircallStream != null)
                ? mCircallManager.unsubscribe(mRemoteCircallStream)
                : Tasks.forException(new IllegalStateException());
    }
}
