package io.straas.android.sdk.circall.demo;

import android.app.Activity;
import android.content.SharedPreferences;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;

import io.straas.android.sdk.circall.CircallConfig;
import io.straas.android.sdk.circall.CircallManager;
import io.straas.android.sdk.circall.CircallPlayConfig;
import io.straas.android.sdk.circall.CircallStatsReport;
import io.straas.android.sdk.circall.CircallStream;
import io.straas.android.sdk.circall.CircallToken;
import io.straas.android.sdk.circall.CircallPublishConfig;
import io.straas.android.sdk.circall.interfaces.EventListener;

import io.straas.android.sdk.demo.R;
import io.straas.android.sdk.demo.databinding.ActivitySingleVideoCallBinding;

public class SingleVideoCallActivity extends AppCompatActivity implements EventListener {
    private static final String TAG = SingleVideoCallActivity.class.getSimpleName();

    public static final String TARGET_ROOM_NAME = "android-test";

    private ActivitySingleVideoCallBinding mBinding;
    private SharedPreferences mSharedPref;
    private CircallManager mCircallManager;
    private CircallStream mLocalCircallStream;
    private boolean mIsConnected = false;
    private boolean mIsRecording = false;

    private CircallStream mRemoteStream;
    private Handler mHandler = new Handler();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        Utils.requestFullscreenMode(this);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_single_video_call);

        mSharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        CircallManager.initialize().continueWithTask(task -> {
            if (!task.isSuccessful()) {
                Log.e(TAG, "init fail: " + task.getException());
            }

            mCircallManager = task.getResult();
            mCircallManager.addEventListener(SingleVideoCallActivity.this);
            return prepare();
        });

        mBinding.toolbar.inflateMenu(R.menu.menu_action);
        mBinding.toolbar.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case R.id.action_switch_camera:
                    if (mLocalCircallStream != null) {
                        mLocalCircallStream.switchCamera();
                    }
                    break;
                case R.id.action_toggle_camera:
                    if (mLocalCircallStream != null) {
                        boolean isCameraOn = mLocalCircallStream.toggleCamera();
                        item.setIcon(isCameraOn ? R.drawable.ic_videocam_off_24dp : R.drawable.ic_videocam_on_24dp);
                    }
                    break;
                case R.id.action_toggle_mic:
                    if (mLocalCircallStream != null) {
                        boolean isMicOn = mLocalCircallStream.toggleMic();
                        item.setIcon(isMicOn ? R.drawable.ic_mic_off_24dp : R.drawable.ic_mic_on_24dp);
                    }
                    break;
                case R.id.action_publish:
                    if (mCircallManager == null || mCircallManager.getCircallState() != CircallManager.STATE_CONNECTED) {
                        break;
                    }

                    final Activity activity = SingleVideoCallActivity.this;
                    if (mCircallManager.isStreamPublished()) {
                        mCircallManager.unpublish().addOnSuccessListener(activity, aVoid ->
                                item.setIcon(R.drawable.ic_cloud_upload_24dp));

                    } else {
                        mCircallManager.publish(getPublishConfig()).addOnSuccessListener(
                                activity, s -> item.setIcon(R.drawable.ic_cloud_off_24dp));
                    }
                    break;
                case R.id.action_record:
                    if (mCircallManager == null || mLocalCircallStream == null ||
                            mCircallManager.getCircallState() != CircallManager.STATE_CONNECTED) {
                        break;
                    }

                    if (mIsRecording) {
                        mCircallManager.stopRecording(mLocalCircallStream).addOnCompleteListener(this, task -> {
                            if (task.isSuccessful()) {
                                mIsRecording = false;
                                item.setIcon(R.drawable.ic_video_call_24dp);
                            }
                        });
                    } else {
                        mCircallManager.startRecording(mLocalCircallStream).addOnCompleteListener(this, task -> {
                            if (task.isSuccessful()) {
                                mIsRecording = true;
                                item.setIcon(R.drawable.ic_slow_motion_video_24dp);
                            }
                        });
                    }
                    break;
                case R.id.action_screenshot:
                    if (mLocalCircallStream != null) {
                        mLocalCircallStream.getVideoFrame().addOnSuccessListener(
                                SingleVideoCallActivity.this,
                                bitmap -> {
                                    Log.d(TAG, "onSuccess bitmap:" + bitmap);
                                    mBinding.screenshot.setImageBitmap(bitmap);
                                    mHandler.postDelayed(() -> mBinding.screenshot.setImageBitmap(null), 3000);
                                });
                    }
                    break;
                case R.id.action_close:
                    destroy();
                    break;
            }
            return true;
        });
        mBinding.fab.setOnClickListener(view -> {
            if (mIsConnected) {
                mBinding.fab.setSelected(false);
                mIsConnected = false;
                leave();
            } else {
                join();
            }
        });

        mBinding.unsubscribe.setOnClickListener(view -> {
            if (mCircallManager != null) {
                mCircallManager.unsubscribe(mRemoteStream);
            }
        });
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
            return mCircallManager.prepare(getConfig(), mBinding.pipVideoView)
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

    private void join() {
//        Utils.getTokenFromChoosenMethod(mSharedPref, this).addOnCompleteListener(task -> {
//            if (!task.isSuccessful()) {
//                Log.e(TAG, "failed to fetch licode manager token:" + task.getException());
//                return;
//            }
//
//            String s = task.getResult();
//            if (TextUtils.isEmpty(s)) {
//                return;
//            }
//
//            join(new CircallToken(s));
//        });
    }

    private void join(CircallToken token) {
        mCircallManager.connect(token).continueWithTask(task -> {
            if (!task.isSuccessful()) {
                Log.e(TAG, "join fails: " + task.getException());
                return Tasks.forException(task.getException());
            }

            mBinding.fab.setSelected(true);
            mIsConnected = true;
            return publish();
        });
    }

    private Task<String> publish() {
        if (mCircallManager != null && mCircallManager.getCircallState() == CircallManager.STATE_CONNECTED) {
            mCircallManager.publish(getPublishConfig()).addOnSuccessListener(
                    this,
                    s -> mBinding.toolbar.getMenu().findItem(R.id.action_publish).setIcon(R.drawable.ic_cloud_off_24dp));
        }

        return Tasks.forException(new IllegalStateException());
    }

    private void leave() {
        if (mCircallManager != null) {
            mCircallManager.disconnect();
        }
    }

    private void destroy() {
        if (mCircallManager != null) {
            mCircallManager.destroy();
            mCircallManager = null;
        }

        finish();
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

    @Override
    public void onStreamAdded(CircallStream stream) {
        Log.d(TAG, "onStreamAdded: " + stream);

        if (mCircallManager != null && stream != null) {
            mCircallManager.subscribe(stream);
        }
    }

    @Override
    public void onStreamSubscribed(CircallStream stream) {
        if (stream == null) {
            return;
        }

        Log.d(TAG, "onStreamSubscribed setRenderer to fullscreenVideoView: ");

        mBinding.fullscreenVideoView.setVisibility(View.VISIBLE);
        stream.setRenderer(mBinding.fullscreenVideoView, CircallPlayConfig.ASPECT_FIT);
        mRemoteStream = stream;
    }

    @Override
    public void onStreamRemoved(CircallStream stream) {
        mBinding.fullscreenVideoView.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onCircallStatsReportUpdate(CircallStatsReport CircallStatsReport) {
//        Log.d(TAG, "onCircallStatsReportUpdate: " + Utils.toDisplayText(SingleVideoCallActivity.this,CircallStatsReport));
    }

    @Override
    public void onError(Exception error) {
    }
}
