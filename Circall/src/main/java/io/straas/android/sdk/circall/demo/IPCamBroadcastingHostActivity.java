package io.straas.android.sdk.circall.demo;

import android.databinding.BindingMethod;
import android.databinding.BindingMethods;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.os.Handler;
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

import io.straas.android.sdk.circall.CircallManager;
import io.straas.android.sdk.circall.CircallPlayConfig;
import io.straas.android.sdk.circall.CircallPublishWithUrlConfig;
import io.straas.android.sdk.circall.CircallStream;
import io.straas.android.sdk.circall.CircallToken;
import io.straas.android.sdk.circall.interfaces.EventListener;
import io.straas.android.sdk.demo.R;
import io.straas.android.sdk.demo.databinding.ActivityIpcamBroadcastingBinding;

@BindingMethods({
        @BindingMethod(type = android.widget.ImageView.class,
                attribute = "app:srcCompat",
                method = "setImageDrawable")
})

public class IPCamBroadcastingHostActivity extends AppCompatActivity implements EventListener {

    public static final String INTENT_CIRCALL_TOKEN = "circall_token";
    public static final String INTENT_PUBLISH_URL = "publish_url";

    private static final String TAG = IPCamBroadcastingViewerActivity.class.getSimpleName();

    public static final int STATE_IDLE = 0;
    public static final int STATE_CONNECTING = 1;
    public static final int STATE_CONNECTED = 2;
    public static final int STATE_PUBLISHED = 3;
    public static final int STATE_SUBSCRIBED = 4;

    private ActivityIpcamBroadcastingBinding mBinding;
    private CircallManager mCircallManager;
    private CircallStream mRemoteCircallStream;
    private Handler mHandler = new Handler();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Utils.requestFullscreenMode(this);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_ipcam_broadcasting);

        CircallManager.initialize().addOnSuccessListener(circallManager -> {
            mCircallManager = circallManager;
            mCircallManager.addEventListener(IPCamBroadcastingHostActivity.this);

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
                            IPCamBroadcastingHostActivity.this,
                            bitmap -> {
                                applySpringAnimation();
                                item.setIcon(R.drawable.ic_screenshot);
                                mBinding.screenshot.setImageBitmap(bitmap);
                                // TODO: 2018/9/14 Handle memory leak
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
        // TODO: 2018/9/14 Handle memory leak
        mHandler.postDelayed(() -> spring.setEndValue(0), 1400);
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
        mBinding.setState(STATE_CONNECTING);
        mCircallManager.connect(this, token).continueWithTask(task -> {
            if (!task.isSuccessful()) {
                Log.e(TAG, "join fails: " + task.getException());
                Toast.makeText(getApplicationContext(), "join fails",
                        Toast.LENGTH_SHORT).show();
                finish();
                return Tasks.forException(task.getException());
            }
            mBinding.setState(STATE_CONNECTED);
            Log.d(TAG, "connect success");
            return publish();
        });
    }

    private Task<Void> publish() {
        if (mCircallManager != null && mCircallManager.getCircallState() == CircallManager.STATE_CONNECTED) {
            final TaskCompletionSource<Void> source = new TaskCompletionSource<>();
            mCircallManager.publishWithUrl(getPublishConfig()).addOnSuccessListener(aVoid -> {
                mBinding.setState(STATE_PUBLISHED);
            }).addOnCompleteListener(task -> {
                mBinding.setShowActionButtons(true);
                source.setResult(null);
            });
            return source.getTask();
        }

        return Tasks.forException(new IllegalStateException());
    }

    private CircallPublishWithUrlConfig getPublishConfig() {
        return new CircallPublishWithUrlConfig.Builder()
                .url(getIntent().getStringExtra(INTENT_PUBLISH_URL)).build();
    }

    private CircallPlayConfig getPlayConfig() {
        return new CircallPlayConfig.Builder()
                .scalingMode(CircallPlayConfig.ASPECT_FILL)
                .build();
    }

    @Override
    public void onStreamAdded(CircallStream stream) {
    }

    @Override
    public void onStreamPublished(CircallStream stream) {
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
        mBinding.setState(STATE_SUBSCRIBED);
    }

    @Override
    public void onStreamRemoved(CircallStream stream) {
        mBinding.fullscreenVideoView.setVisibility(View.INVISIBLE);
        mBinding.setState(STATE_CONNECTED);
    }

    @Override
    public void onStreamUpdated(CircallStream stream) {
        if (stream == null) {
            return;
        }
    }

    @Override
    public void onError(Exception error) {
        Log.e(TAG, "onError error:" + error);

        Toast.makeText(getApplicationContext(), "onError",
                Toast.LENGTH_SHORT).show();
        finish();
    }

    private void showScreenshotFailedDialog(int messageResId) {
        showFailedDialog(R.string.screenshot_failed_title, messageResId);
    }

    private void showFailedDialog(int titleResId, int messageResId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.CircallDialogTheme);
        builder.setTitle(titleResId);
        builder.setMessage(messageResId);
        builder.setPositiveButton(android.R.string.ok, null);
        final AlertDialog dialog = builder.create();
        dialog.show();
    }

    @Override
    public void onBackPressed() {
        if (mBinding.getState() >= STATE_CONNECTED) {
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
            destroyCircallManager();

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

        mCircallManager.destroy();
        mCircallManager = null;
    }
}
