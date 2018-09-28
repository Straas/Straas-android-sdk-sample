package io.straas.android.sdk.circall.demo;

import android.Manifest;
import android.databinding.BindingMethod;
import android.databinding.BindingMethods;
import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.LayoutRes;
import android.support.annotation.MenuRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.ActionMenuView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.facebook.rebound.SimpleSpringListener;
import com.facebook.rebound.Spring;
import com.facebook.rebound.SpringSystem;
import com.facebook.rebound.SpringUtil;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import io.straas.android.sdk.circall.CircallManager;
import io.straas.android.sdk.circall.CircallPlayConfig;
import io.straas.android.sdk.circall.CircallPlayerView;
import io.straas.android.sdk.circall.CircallStream;
import io.straas.android.sdk.circall.CircallToken;
import io.straas.android.sdk.circall.interfaces.EventListener;
import io.straas.android.sdk.demo.R;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

@BindingMethods({
        @BindingMethod(type = android.widget.ImageView.class,
                attribute = "app:srcCompat",
                method = "setImageDrawable")
})
public abstract class CircallDemoBaseActivity extends AppCompatActivity implements ActionMenuView.OnMenuItemClickListener, EventListener {

    public static final String INTENT_CIRCALL_TOKEN = "circall_token";

    private static final String ALBUM_FOLDER = "StraaS";

    private static final String[] STORAGE_PERMISSION = {
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    private static final int STORAGE_REQUEST = 1;

    public static final int STATE_IDLE = 0;
    public static final int STATE_CONNECTING = 1;
    public static final int STATE_CONNECTED = 2;
    public static final int STATE_PUBLISHED = 3;
    public static final int STATE_SUBSCRIBED = 4;

    protected Bitmap mCapturedPicture;
    protected Handler mHandler = new Handler();

    protected CircallManager mCircallManager;
    protected CircallStream mRemoteCircallStream;
    protected int mState;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Utils.requestFullscreenMode(this);
        ViewDataBinding binding = DataBindingUtil.setContentView(this, getContentViewLayoutId());
        setBinding(binding);

        CircallManager.initialize().continueWithTask(task -> {
            if (!task.isSuccessful()) {
                Log.e(getTag(), "init fail: " + task.getException());
                finish();
                return Tasks.forException(new RuntimeException());
            }

            mCircallManager = task.getResult();
            mCircallManager.addEventListener(CircallDemoBaseActivity.this);
            return prepare();
        }).addOnSuccessListener(object -> {
            String token = getIntent().getStringExtra(INTENT_CIRCALL_TOKEN);
            if (!TextUtils.isEmpty(token)) {
                connect(new CircallToken(token));
            } else {
                Toast.makeText(getApplicationContext(), "Start CirCall fails due to empty token",
                        Toast.LENGTH_SHORT).show();
                finish();
            }
        }).addOnFailureListener(e -> {
            Log.e(getTag(), "Prepare fails " + e);
            finish();
        });

        setSupportActionBar(getToolbar());
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getActionMenuView().setOnMenuItemClickListener(this);

        setState(STATE_IDLE);
        setShowActionButtons(false);
    }

    //=====================================================================
    // Abstract methods
    //=====================================================================
    protected abstract String getTag();

    @LayoutRes
    protected abstract int getContentViewLayoutId();

    protected abstract void setBinding(ViewDataBinding binding);

    protected abstract void scaleScreenShotView(float scale);

    protected abstract void setScreenShotView(Bitmap bitmap);

    protected abstract ActionMenuView getActionMenuView();

    protected abstract Toolbar getToolbar();

    protected abstract CircallPlayerView getRemoteStreamView();

    protected abstract void setShowActionButtons(boolean show);

    public abstract void onShowActionButtonsToggled(View view);

    protected abstract Task<?> prepare();

    //=====================================================================
    // Optional implementation
    //=====================================================================
    protected void onConnected() {

    }

    @MenuRes
    protected int getMenuRes() {
        return R.menu.ipcam_broadcasting_menu_action;
    }

    protected void setState(int state) {
        mState = state;
    }

    protected void showFailedDialog(int titleResId, int messageResId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.CircallDialogTheme);
        builder.setTitle(titleResId);
        builder.setMessage(messageResId);
        builder.setPositiveButton(android.R.string.ok, null);
        final AlertDialog dialog = builder.create();
        dialog.show();
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_screenshot:
                if (mRemoteCircallStream == null) {
                    showScreenshotFailedDialog(R.string.screenshot_failed_message_two_way_not_ready);
                    break;
                }

                item.setIcon(R.drawable.ic_screenshot_focus);
                mRemoteCircallStream.getVideoFrame().addOnSuccessListener(
                        CircallDemoBaseActivity.this,
                        bitmap -> {
                            mCapturedPicture = bitmap;
                            storePicture();
                            item.setIcon(R.drawable.ic_screenshot);
                        });
                return true;
            default:
                break;
        }
        return false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(getMenuRes() , getActionMenuView().getMenu());
        return super.onCreateOptionsMenu(menu);
    }

    protected CircallPlayConfig getPlayConfig() {
        return new CircallPlayConfig.Builder()
                .scalingMode(CircallPlayConfig.ASPECT_FILL)
                .build();
    }

    protected List<Task<Void>> tasksBeforeDestroy() {
        return new ArrayList<>();
    }

    @Override
    public void onDestroy() {
        destroyCircallManager();
        super.onDestroy();
    }

    protected void destroyCircallManager() {
        if (mCircallManager == null) {
            return;
        }

        Tasks.whenAll(tasksBeforeDestroy()).addOnCompleteListener(task -> {
            mCircallManager.destroy();
            mCircallManager = null;
        });
    }

    protected Task<Void> unsubscribe() {
        return (mState == STATE_SUBSCRIBED && mRemoteCircallStream != null)
                ? mCircallManager.unsubscribe(mRemoteCircallStream)
                : Tasks.forException(new IllegalStateException());
    }

    protected Task<Void> unpublish() {
        return (mState >= STATE_PUBLISHED)
                ? mCircallManager.unpublish()
                : Tasks.forException(new IllegalStateException());
    }

    @Override
    public void onBackPressed() {
        if (mState >= STATE_CONNECTED) {
            showEndCircallConfirmationDialog();
        } else {
            super.onBackPressed();
        }
    }

    protected String getEndTitle() {
        return getResources().getString(R.string.end_circall_confirmation_title);
    }

    protected String getEndMessage() {
        return getResources().getString(R.string.end_circall_confirmation_message);
    }

    //=====================================================================
    // EventListener
    //=====================================================================
    @Override
    public void onStreamAdded(CircallStream stream) {
        if (mCircallManager != null && stream != null) {
            mCircallManager.subscribe(stream);
        }
    }

    @Override
    public void onStreamPublished(CircallStream stream) {
        setState(STATE_PUBLISHED);
    }

    @Override
    public void onStreamSubscribed(CircallStream stream) {
        if (stream == null) {
            return;
        }

        getRemoteStreamView().setVisibility(View.VISIBLE);
        // TODO: 2018/9/14
        stream.setRenderer(getRemoteStreamView(), getPlayConfig());
        mRemoteCircallStream = stream;
        setState(STATE_SUBSCRIBED);
    }

    @Override
    public void onStreamRemoved(CircallStream stream) {
        getRemoteStreamView().setVisibility(View.INVISIBLE);
        setState(STATE_CONNECTED);
    }

    @Override
    public void onStreamUpdated(CircallStream stream) {
    }

    @Override
    public void onError(Exception error) {
        Log.e(getTag(), "onError error:" + error);

        // In our demo, this page is only invoked from another activity,
        // so just abort for this onError event to avoid showing freeze screen
        Toast.makeText(getApplicationContext(), "onError",
                Toast.LENGTH_SHORT).show();
        finish();
    }

    //=====================================================================
    // Internal methods
    //=====================================================================
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    private void showScreenshotFailedDialog(int messageResId) {
        showFailedDialog(R.string.screenshot_failed_title, messageResId);
    }

    @AfterPermissionGranted(STORAGE_REQUEST)
    private void storePicture() {
        if (EasyPermissions.hasPermissions(this, STORAGE_PERMISSION)) {
            if (mCapturedPicture != null) {
                if (storePicture(mCapturedPicture)) {
                    applySpringAnimation(mCapturedPicture);
                }
                mCapturedPicture = null;
            }
        } else {
            EasyPermissions.requestPermissions(this,
                    getResources().getString(R.string.picture_store_need_permission),
                    STORAGE_REQUEST, STORAGE_PERMISSION);
        }
    }

    private File getPicturesFolder() throws IOException {
        File picturesFolder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        if(picturesFolder.exists() || picturesFolder.mkdir()) {
            File albumFolder = new File(picturesFolder, ALBUM_FOLDER);
            if (albumFolder.exists() || albumFolder.mkdir()) {
                return albumFolder;
            }
            return picturesFolder;
        }
        throw new IOException();
    }

    private boolean storePicture(Bitmap bitmap) {
        File dir;
        try {
            dir = getPicturesFolder();
        } catch (IOException e) {
            Log.w(getTag(), "Getting folder for storing pictures failed.");
            return false;
        }
        String prefix = new SimpleDateFormat("yyyyMMdd-", Locale.US).format(new Date());
        int index = 1;
        for (File file : dir.listFiles()) {
            String fileName = file.getName();
            if (!fileName.startsWith(prefix)) {
                continue;
            }
            index = Math.max(Integer.parseInt(
                    fileName.substring(fileName.indexOf("-") + 1, fileName.lastIndexOf("."))) + 1,
                    index);
        }
        File file = new File(dir, prefix + Integer.toString(index) + ".png");
        try {
            FileOutputStream out = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
            Toast.makeText(this, R.string.screenshot_success_message, Toast.LENGTH_SHORT).show();
            return true;
        } catch (IOException ignored) {
            Log.w(getTag(), "Writing the picture to file failed.");
            return false;
        }
    }

    private void applySpringAnimation(Bitmap bitmap) {
        SpringSystem springSystem = SpringSystem.create();
        Spring spring = springSystem.createSpring();
        spring.addListener(new SimpleSpringListener() {
            @Override
            public void onSpringUpdate(Spring spring) {
                float scale = (float) SpringUtil.mapValueFromRangeToRange(spring.getCurrentValue(), 0, 1, 1, 0.5);
                scaleScreenShotView(scale);
            }
        });
        spring.setEndValue(1);
        // TODO: 2018/9/14 Handle memory leak
        mHandler.postDelayed(() -> spring.setEndValue(0), 1400);
        setScreenShotView(bitmap);
        // TODO: 2018/9/14 Handle memory leak
        mHandler.postDelayed(() -> setScreenShotView(null), 3000);
    }

    private void connect(CircallToken token) {
        setState(STATE_CONNECTING);
        mCircallManager.connect(token).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                onConnected();
            } else {
                Log.e(getTag(), "connect fails: " + task.getException());
                Toast.makeText(getApplicationContext(), "connect fails",
                        Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void showEndCircallConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.CircallDialogTheme);
        builder.setTitle(getEndTitle());
        builder.setMessage(getEndMessage());
        builder.setPositiveButton(android.R.string.ok, (dialog, which) -> finish());
        builder.setNegativeButton(android.R.string.cancel, null);
        final AlertDialog dialog = builder.create();
        dialog.show();
    }
}
