package io.straas.android.sdk.streaming.demo.camera;

import android.Manifest;
import android.app.Activity;
import android.arch.lifecycle.Observer;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;

import java.util.ArrayList;

import io.straas.android.sdk.demo.common.MemberIdentity;
import io.straas.android.sdk.demo.qrcode.QrcodeActivity;
import io.straas.android.sdk.media.LiveEventListener;
import io.straas.android.sdk.streaming.CameraController;
import io.straas.android.sdk.streaming.LiveEventConfig;
import io.straas.android.sdk.streaming.StreamConfig;
import io.straas.android.sdk.streaming.StreamManager;
import io.straas.android.sdk.streaming.StreamStatsReport;
import io.straas.android.sdk.streaming.demo.R;
import io.straas.android.sdk.streaming.demo.Utils;
import io.straas.android.sdk.streaming.demo.filter.GPUImageSupportFilter;
import io.straas.android.sdk.streaming.demo.filter.GrayImageFilter;
import io.straas.android.sdk.streaming.error.StreamException.EventExpiredException;
import io.straas.android.sdk.streaming.error.StreamException.LiveCountLimitException;
import io.straas.android.sdk.streaming.interfaces.EventListener;
import jp.co.cyberagent.android.gpuimage.GPUImageColorInvertFilter;

import static io.straas.android.sdk.streaming.StreamManager.STATE_CONNECTING;
import static io.straas.android.sdk.streaming.StreamManager.STATE_STREAMING;
import static io.straas.android.sdk.streaming.demo.R.id.filter;
import static io.straas.android.sdk.streaming.demo.R.id.flash;
import static io.straas.android.sdk.streaming.demo.R.id.switch_camera;
import static io.straas.android.sdk.streaming.demo.R.id.trigger;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "Streaming";

    private static final int QRCODE_REQUEST_STREAM_KEY = 1;

    private StreamManager mStreamManager;
    private CameraController mCameraController;
    private TextureView mTextureView;
    private RelativeLayout layoutStreamInput;
    private RadioGroup mStreamWaySwitcher;
    private EditText mTitleEdit;
    private FrameLayout mStreamKeyPanel;
    private EditText mStreamKeyEdit;
    private TextView mStreamStats;
    private Button mTriggerButton, mSwitchCameraButton, mFlashButton, mFilterButton;
    private ToggleButton mMuteButton;
    private int mFilter = 0;
    private static final String[] STREAM_PERMISSIONS = {
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO
    };
    private static final int STREAM_PERMISSION_REQUEST = 1;
    // remove LiveEventListener if you don't need to receive live event, e.g. CCU
    private LiveEventListener mLiveEventListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_streaming);

        StreamManager.initialize(MemberIdentity.ME)
                .addOnCompleteListener(new OnCompleteListener<StreamManager>() {
                    @Override
                    public void onComplete(@NonNull Task<StreamManager> task) {
                        if (!task.isSuccessful()) {
                            Log.e(TAG, "init fail " + task.getException());
                            return;
                        }
                        mStreamManager = task.getResult();
                        mStreamManager.addEventListener(mEventListener);
                        preview();
                    }
                });
        mTextureView = findViewById(R.id.preview);
        mTextureView.setKeepScreenOn(true);

        mTriggerButton = findViewById(trigger);
        mSwitchCameraButton = findViewById(switch_camera);
        mFlashButton = findViewById(flash);
        mFilterButton = findViewById(filter);
        mMuteButton = findViewById(R.id.mute);
        mTitleEdit = findViewById(R.id.edit_title);
        mStreamStats = findViewById(R.id.stream_stats);
        mStreamWaySwitcher = findViewById(R.id.stream_way);
        mStreamKeyEdit = findViewById(R.id.edit_stream_key);
        mStreamKeyPanel = findViewById(R.id.stream_key_panel);
        layoutStreamInput = findViewById(R.id.layout_stream_input);
        initEditStreamKey();
        initStreamWaySwitcher();

        checkPermissions();
    }

    private void initEditStreamKey() {
        final ImageView clearButton = findViewById(R.id.clear_stream_key);
        final ImageView scanButton = findViewById(R.id.scan_stream_key);
        mStreamKeyEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                boolean showScanButton = TextUtils.isEmpty(s);
                scanButton.setVisibility(showScanButton ? View.VISIBLE : View.GONE);
                clearButton.setVisibility(showScanButton ? View.GONE : View.VISIBLE);
            }
        });
    }

    private void initStreamWaySwitcher() {
        switchInputView(mStreamWaySwitcher.getCheckedRadioButtonId());
        mStreamWaySwitcher.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switchInputView(checkedId);
            }
        });
    }

    private void setStreamWidigetVisibility(int visibility) {
        if (layoutStreamInput != null && visibility != layoutStreamInput.getVisibility()) {
            layoutStreamInput.setVisibility(visibility);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (checkPermissions() == 0) {
            preview();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        destroy();
    }

    private int checkPermissions() {
        String[] requestPermissions = getPermissionsRequestArray(STREAM_PERMISSIONS);
        if (requestPermissions.length != 0) {
            ActivityCompat.requestPermissions(MainActivity.this, requestPermissions,
                    STREAM_PERMISSION_REQUEST);
        }
        return requestPermissions.length;
    }

    private String[] getPermissionsRequestArray(String[] permissions) {
        ArrayList<String> requestArray = new ArrayList<>();
        for (String permission : permissions) {
            if (ActivityCompat.checkSelfPermission(MainActivity.this, permission) !=
                    PackageManager.PERMISSION_GRANTED) {
                requestArray.add(permission);
            }
        }
        return requestArray.toArray(new String[0]);
    }

    private StreamConfig getConfig() {
        return new StreamConfig.Builder()
                .camera(StreamConfig.CAMERA_FRONT)
                .fitAllCamera(true)
                .build();
    }

    private Task<CameraController> preview() {
        if (mStreamManager != null && mStreamManager.getStreamState() == StreamManager.STATE_IDLE) {
            return mStreamManager.prepare(getConfig(), mTextureView)
                    .addOnCompleteListener(this, new OnCompleteListener<CameraController>() {
                        @Override
                        public void onComplete(@NonNull Task<CameraController> task) {
                            if (task.isSuccessful()) {
                                Log.d(TAG, "Prepare succeeds");
                                mCameraController = task.getResult();
                                enableAllButtons();
                            } else {
                                Log.e(TAG, "Prepare fails " + task.getException());
                            }
                        }
                    });
        }
        return Tasks.forException(new IllegalStateException());
    }

    private void enableAllButtons() {
        mTriggerButton.setEnabled(true);
        mSwitchCameraButton.setEnabled(true);
        mFlashButton.setEnabled(true);
        mFilterButton.setEnabled(true);
        mMuteButton.setEnabled(true);
    }

    private void createLiveEventAndStartStreaming(String title) {
        mStreamManager.createLiveEvent(new LiveEventConfig.Builder()
                .title(title)
                .build())
                .addOnSuccessListener(new OnSuccessListener<String>() {
                    @Override
                    public void onSuccess(String liveId) {
                        Log.d(TAG, "Create live event succeeds: " + liveId);
                        startStreamingWithLiveId(liveId);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception error) {
                        if (error instanceof LiveCountLimitException) {
                            String liveId = ((LiveCountLimitException) error).getLiveId();
                            Log.d(TAG, "Existing live event: " + liveId);
                            startStreamingWithLiveId(liveId);
                        } else {
                            Log.e(TAG, "Create live event fails: " + error);
                            showError(error);
                            mTriggerButton.setText(getResources().getString(R.string.start));
                            mStreamStats.setText("");
                        }
                    }
                });
    }

    private void startStreamingWithLiveId(final String liveId) {
        mStreamManager.startStreamingWithLiveId(liveId).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Log.d(TAG, "Start streaming succeeds");
                    // remove LiveEventListener if you don't need to receive live event, e.g. CCU
                    if (mLiveEventListener == null) {
                        LiveEventListener.initWithLiveId(MemberIdentity.ME, null)
                                .addOnSuccessListener(new OnSuccessListener<LiveEventListener>() {
                                    @Override
                                    public void onSuccess(LiveEventListener liveEventListener) {
                                        mLiveEventListener = liveEventListener;
                                        mLiveEventListener.getCCU().observe(MainActivity.this,
                                                new Observer<Integer>() {
                                                    @Override
                                                    public void onChanged(@Nullable Integer integer) {
                                                        if (integer != null) {
                                                            Log.d(TAG, "ccu: " + Integer.toString(integer));
                                                        }
                                                    }
                                                });
                                        mLiveEventListener.getHitCount().observe(MainActivity.this,
                                                new Observer<Integer>() {
                                                    @Override
                                                    public void onChanged(@Nullable Integer integer) {
                                                        if (integer != null) {
                                                            Log.d(TAG, "hit count: " + Integer.toString(integer));
                                                        }
                                                    }
                                                });
                                        mLiveEventListener.start(liveId);
                                    }
                                });
                    } else {
                        mLiveEventListener.start(liveId);
                    }
                } else {
                    Exception error = task.getException();
                    if (error instanceof EventExpiredException) {
                        Log.w(TAG, "Live event expires, set this event to ended and create " +
                                "a new one.");
                        mStreamManager.endLiveEvent(liveId).addOnSuccessListener(
                                new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Log.d(TAG, "End live event succeeds: " + liveId);
                                        createLiveEventAndStartStreaming(mTitleEdit.getText().toString());
                                    }
                                });
                    } else {
                        Log.e(TAG, "Start streaming fails " + error);
                        showError(error);
                        mTriggerButton.setText(getResources().getString(R.string.start));
                        mStreamStats.setText("");
                    }
                }
            }
        });
    }

    private void startStreamingWithStreamKey(String streamKey) {
        mStreamManager.startStreamingWithStreamKey(streamKey).addOnCompleteListener(
                new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "Start streaming succeeds");
                        } else {
                            Log.e(TAG, "Start streaming fails " + task.getException());
                            showError(task.getException());
                            mTriggerButton.setText(getResources().getString(R.string.start));
                            mStreamStats.setText("");
                        }
                    }
                });
    }

    private void stopStreaming() {
        // remove LiveEventListener if you don't need to receive live event, e.g. CCU
        if (mLiveEventListener != null) {
            mLiveEventListener.stop();
        }
        mStreamManager.stopStreaming().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Log.d(TAG, "Stop succeeds");
                    resetViews();
                } else {
                    Log.e(TAG, "Stop fails: " + task.getException());
                }
            }
        });
    }

    private void destroy() {
        if (mStreamManager != null) {
            mStreamManager.destroy().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    resetViews();
                }
            });
        }
    }

    public void trigger(View view) {
        if (mStreamManager == null) {
            return;
        }
        if (mTriggerButton.getText().equals(getResources().getString(R.string.start))) {
            mTriggerButton.setText(getResources().getString(R.string.stop));
            int radioButtonId = mStreamWaySwitcher.getCheckedRadioButtonId();
            if (radioButtonId == R.id.stream_way_live_event) {
                createLiveEventAndStartStreaming(mTitleEdit.getText().toString());
            } else if (radioButtonId == R.id.stream_way_stream_key) {
                startStreamingWithStreamKey(mStreamKeyEdit.getText().toString());
            }
        } else {
            if (mStreamManager.getStreamState() == STATE_CONNECTING ||
                    mStreamManager.getStreamState() == STATE_STREAMING) {
                mTriggerButton.setEnabled(false);
                stopStreaming();
            } else {
                Toast.makeText(this, "Trying to get the live event, please try later.",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void switchCamera(View view) {
        if (mCameraController != null) {
            mCameraController.switchCamera();
        }
    }

    public void flash(View view) {
        if (mCameraController != null) {
            mCameraController.toggleFlash();
        }
    }

    public void changeFilter(View view) {
        if (mStreamManager != null) {
            switch (mFilter) {
                case 0:
                    mFilter += mStreamManager.setFilter(new GrayImageFilter()) ? 1 : 0;
                    break;
                case 1:
                    mFilter += mStreamManager.setFilter(new GPUImageSupportFilter<>(new GPUImageColorInvertFilter())) ? 1 : 0;
                    break;
                case 2:
                    mFilter += mStreamManager.setFilter(null) ? 1 : 0;
                    break;
            }
            mFilter %= 3;
        }
    }

    public void toggleMute(View view) {
        if (mStreamManager != null) {
            mStreamManager.setAudioEnabled(!mMuteButton.isChecked());
            mMuteButton.setChecked(!mStreamManager.isAudioEnabled());
        }
    }

    public void scanQrcode(View view) {
        if (checkPermissions() != 0) {
            return;
        }
        destroy();
        Intent intent = new Intent(this, QrcodeActivity.class);
        int requestCode = QRCODE_REQUEST_STREAM_KEY;
        startActivityForResult(intent, requestCode);
    }

    public void clearStreamKey(View view) {
        mStreamKeyEdit.getText().clear();
    }

    private void switchInputView(int checkedId) {
        if (checkedId == R.id.stream_way_live_event) {
            mStreamKeyPanel.setVisibility(View.GONE);
            mTitleEdit.setVisibility(View.VISIBLE);
        } else if (checkedId == R.id.stream_way_stream_key) {
            mTitleEdit.setVisibility(View.GONE);
            mStreamKeyPanel.setVisibility(View.VISIBLE);
        }
    }

    private void resetViews() {
        mTriggerButton.setText(getResources().getString(R.string.start));
        mTriggerButton.setEnabled(true);
        mStreamStats.setText("");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            String value = data.getStringExtra(QrcodeActivity.KEY_QR_CODE_VALUE);
            if (requestCode == QRCODE_REQUEST_STREAM_KEY) {
                if (!isPureText(value)) {
                    Toast.makeText(this, R.string.error_wrong_format, Toast.LENGTH_LONG).show();
                    return;
                }
                mStreamKeyEdit.setText(value);
            }
        }
    }

    private static boolean isPureText(String string) {
        return string.matches("[A-Za-z0-9]+");
    }

    private void showError(Exception exception) {
        Toast.makeText(this, exception.toString(), Toast.LENGTH_LONG).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == STREAM_PERMISSION_REQUEST) {
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(MainActivity.this, getResources().getString(R.string.hint_need_permission), Toast.LENGTH_SHORT).show();
                    return;
                }
            }
            preview();
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private EventListener mEventListener = new EventListener() {
        @Override
        public void onStreamStatsReportUpdate(StreamStatsReport streamStatsReport) {
            if (mStreamManager.getStreamState() == STATE_CONNECTING ||
                    mStreamManager.getStreamState() == STATE_STREAMING) {
                mStreamStats.setText(Utils.toDisplayText(MainActivity.this, streamStatsReport));
                setStreamWidigetVisibility(View.GONE);
            } else {
                setStreamWidigetVisibility(View.VISIBLE);
            }
        }

        @Override
        public void onError(Exception error, @Nullable String liveId) {
            Log.e(TAG, "onError " + error);
            mTriggerButton.setText(getResources().getString(R.string.start));
            mStreamStats.setText("");
            setStreamWidigetVisibility(View.VISIBLE);
        }
    };
}
