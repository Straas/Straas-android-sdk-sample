package io.straas.android.sdk.streaming.demo;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.session.MediaControllerCompat.Callback;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;

import java.util.ArrayList;

import io.straas.android.sdk.demo.R;
import io.straas.android.sdk.media.StraasMediaCore;
import io.straas.android.sdk.streaming.CameraController;
import io.straas.android.sdk.streaming.LiveEventConfig;
import io.straas.android.sdk.streaming.StreamConfig;
import io.straas.android.sdk.streaming.StreamManager;
import io.straas.android.sdk.streaming.demo.filter.GPUImageSupportFilter;
import io.straas.android.sdk.streaming.demo.filter.GrayImageFilter;
import io.straas.android.sdk.streaming.error.StreamException.LiveCountLimitException;
import io.straas.android.sdk.streaming.interfaces.EventListener;
import io.straas.sdk.demo.MemberIdentity;
import jp.co.cyberagent.android.gpuimage.GPUImageColorInvertFilter;

import static io.straas.android.sdk.demo.R.id.filter;
import static io.straas.android.sdk.demo.R.id.flash;
import static io.straas.android.sdk.demo.R.id.switch_camera;
import static io.straas.android.sdk.demo.R.id.trigger;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "Streaming";
    private StreamManager mStreamManager;
    private CameraController mCameraController;
    private TextureView mTextureView;
    private EditText mEditTitle;
    private EditText mEditSynopsis;
    private Button btn_trigger, btn_switch, btn_flash, btn_filter;
    private int mFilter = 0;
    private String mLiveId;
    private static final String[] STREAM_PERMISSIONS = {
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO
    };
    private static final int STREAM_PERMISSION_REQUEST = 1;
    // remove StraasMediaCore if you don't need to receive live event, e.g. CCU
    private StraasMediaCore mStraasMediaCore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_streaming);

        StreamManager.initialize(MemberIdentity.ME)
                .continueWithTask(new Continuation<StreamManager, Task<CameraController>>() {
                    @Override
                    public Task<CameraController> then(@NonNull Task<StreamManager> task) throws Exception {
                        if (!task.isSuccessful()) {
                            Log.e(TAG, "init fail " + task.getException());
                            throw task.getException();
                        }
                        mStreamManager = task.getResult();
                        mStreamManager.addEventListener(mEventListener);
                        return preview();
                    }
                });
        mTextureView = (TextureView) findViewById(R.id.preview);
        mTextureView.setKeepScreenOn(true);

        btn_trigger = (Button) findViewById(trigger);
        btn_switch = (Button) findViewById(switch_camera);
        btn_flash = (Button) findViewById(flash);
        btn_filter = (Button) findViewById(filter);
        mEditTitle = (EditText) findViewById(R.id.edit_title);
        mEditSynopsis = (EditText) findViewById(R.id.edit_synopsis);

        checkPermissions();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mStreamManager != null) {
            mStreamManager.destroy();
        }
    }

    private void checkPermissions() {
        String[] requestPermissions = getPermissionsRequestArray(STREAM_PERMISSIONS);
        if (requestPermissions.length != 0) {
            ActivityCompat.requestPermissions(MainActivity.this, requestPermissions,
                    STREAM_PERMISSION_REQUEST);
        }
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
        btn_trigger.setEnabled(true);
        btn_switch.setEnabled(true);
        btn_flash.setEnabled(true);
        btn_filter.setEnabled(true);
    }

    private void startStreaming(String title, String synopsis) {
        mStreamManager.createLiveEvent(new LiveEventConfig.Builder()
                .title(title)
                .synopsis(synopsis)
                .build())
                .addOnSuccessListener(new OnSuccessListener<String>() {
                    @Override
                    public void onSuccess(String liveId) {
                        Log.d(TAG, "Create live event succeeds: " + liveId);
                        mLiveId = liveId;
                        startStreaming(mLiveId);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception error) {
                        if (error instanceof LiveCountLimitException){
                            mLiveId = ((LiveCountLimitException)error).getLiveId();
                            Log.d(TAG, "Existing live event: " + mLiveId);
                            startStreaming(mLiveId);
                        } else {
                            Log.e(TAG, "Create live event fails: " + error);
                            btn_trigger.setText(getResources().getString(R.string.start));
                        }
                    }
                });
    }

    private void startStreaming(final String liveId) {
        mStreamManager.startStreaming(liveId).addOnCompleteListener(new OnCompleteListener<String>() {
            @Override
            public void onComplete(@NonNull Task<String> task) {
                if (task.isSuccessful()) {
                    Log.d(TAG, "Start streaming succeeds");
                    // remove StraasMediaCore if you don't need to receive live event, e.g. CCU
                    mStraasMediaCore = new StraasMediaCore(MemberIdentity.ME, new MediaBrowserCompat.ConnectionCallback() {
                        @Override
                        public void onConnected() {
                            mStraasMediaCore.getMediaController().getTransportControls()
                                    .prepareFromMediaId(StraasMediaCore.LIVE_ID_PREFIX + liveId, null);
                            mStraasMediaCore.getMediaController().registerCallback(new Callback() {
                                @Override
                                public void onSessionEvent(String event, Bundle extras) {
                                    switch (event) {
                                        case StraasMediaCore.LIVE_EXTRA_STATISTICS_CCU:
                                            Log.d(TAG, "ccu: " + extras.getInt(event));
                                            break;
                                        case StraasMediaCore.LIVE_EXTRA_STATISTICS_HIT_COUNT:
                                            Log.d(TAG, "hit count: " + extras.getInt(event));
                                            break;
                                    }
                                }
                            });
                        }
                    });
                    mStraasMediaCore.getMediaBrowser().connect();
                } else {
                    Log.e(TAG, "Start streaming fails " + task.getException());
                    btn_trigger.setText(getResources().getString(R.string.start));
                }
            }
        });
    }

    private void stopStreaming() {
        // remove StraasMediaCore if you don't need to receive live event, e.g. CCU
        if (mStraasMediaCore != null && mStraasMediaCore.getMediaBrowser().isConnected()) {
            mStraasMediaCore.getMediaBrowser().disconnect();
        }
        mStreamManager.stopStreaming().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Log.d(TAG, "Stop succeeds");
                    btn_trigger.setText(getResources().getString(R.string.start));
                    btn_trigger.setEnabled(true);
                    endLiveEvent();
                } else {
                    Log.e(TAG, "Stop fails: " + task.getException());
                }
            }
        });
    }

    private void endLiveEvent() {
        if (mStreamManager != null || !TextUtils.isEmpty(mLiveId)) {
            mStreamManager.cleanLiveEvent(mLiveId).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Log.d(TAG, "End live event succeeds: " + mLiveId);
                    mLiveId = null;
                }
            });
        }
    }

    public void trigger(View view) {
        if (btn_trigger.getText().equals(getResources().getString(R.string.start))) {
            if (mStreamManager != null) {
                btn_trigger.setText(getResources().getString(R.string.stop));
                startStreaming(mEditTitle.getText().toString(), mEditSynopsis.getText().toString());
            }
        } else {
            btn_trigger.setEnabled(false);
            if (mStreamManager != null) {
                stopStreaming();
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
            switch(mFilter) {
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
        public void onError(Exception error, @Nullable String liveId) {
            Log.e(TAG, "onError " + error);
            btn_trigger.setText(getResources().getString(R.string.start));
        }
    };
}
