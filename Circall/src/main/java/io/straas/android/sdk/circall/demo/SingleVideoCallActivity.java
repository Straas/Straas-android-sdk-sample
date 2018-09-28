package io.straas.android.sdk.circall.demo;

import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import io.straas.android.sdk.circall.CircallConfig;
import io.straas.android.sdk.circall.CircallManager;
import io.straas.android.sdk.circall.CircallPlayConfig;
import io.straas.android.sdk.circall.CircallPublishConfig;
import io.straas.android.sdk.circall.CircallRecordingStreamMetadata;
import io.straas.android.sdk.circall.CircallStream;
import io.straas.android.sdk.circall.CircallToken;
import io.straas.android.sdk.circall.interfaces.EventListener;
import io.straas.android.sdk.demo.R;
import io.straas.android.sdk.demo.databinding.ActivitySingleVideoCallBinding;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class SingleVideoCallActivity extends CircallDemoBaseActivity implements EventListener {

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
        Utils.requestFullscreenMode(this);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_single_video_call);

        CircallManager.initialize().continueWithTask(task -> {
            if (!task.isSuccessful()) {
                Log.e(TAG, "init fail: " + task.getException());
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

        mBinding.setState(STATE_IDLE);
        mBinding.setShowActionButtons(false);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
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
            Log.w(TAG, "Getting folder for storing pictures failed.");
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
            Log.w(TAG, "Writing the picture to file failed.");
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
                mBinding.screenshot.setScaleX(scale);
                mBinding.screenshot.setScaleY(scale);
            }
        });
        spring.setEndValue(1);
        // TODO: 2018/9/14 Handle memory leak
        mHandler.postDelayed(() -> spring.setEndValue(0), 1400);
        mBinding.screenshot.setImageBitmap(bitmap);
        // TODO: 2018/9/14 Handle memory leak
        mHandler.postDelayed(() -> mBinding.screenshot.setImageBitmap(null), 3000);
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

    public void onShowActionButtonsToggled(View view) {
        boolean isShowing = mBinding.getShowActionButtons();
        mBinding.setShowActionButtons(!isShowing);
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
            mCircallManager.publishWithCameraCapture(getPublishConfig()).addOnCompleteListener(task -> {
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
    public void onStreamPublished(CircallStream stream) {
        mBinding.setSeconds(STATE_PUBLISHED);
    }

    @Override
    public void onStreamSubscribed(CircallStream stream) {
        if (stream == null) {
            return;
        }

        mBinding.fullscreenVideoView.setVisibility(View.VISIBLE);
        // TODO: 2018/9/14
        stream.setRenderer(mBinding.fullscreenVideoView, getPlayConfig());
        mRemoteCircallStream = stream;
        mBinding.setState(STATE_SUBSCRIBED);
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
    public void onStreamRemoved(CircallStream stream) {
        mBinding.fullscreenVideoView.setVisibility(View.INVISIBLE);
        mBinding.setState(STATE_CONNECTED);
    }

    @Override
    public void onStreamUpdated(CircallStream stream) {
        if (stream == null) {
            return;
        }

        mBinding.setIsRemoteVideoOff(!stream.isVideoEnabled());
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
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.CircallDialogTheme);
        builder.setTitle(titleResId);
        builder.setMessage(messageResId);
        builder.setPositiveButton(android.R.string.ok, null);
        final AlertDialog dialog = builder.create();
        dialog.show();
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
        if (mBinding.getState() >= STATE_CONNECTED) {
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
