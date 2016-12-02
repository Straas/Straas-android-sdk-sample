package io.straas.android.sdk.streaming.demo;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.ArrayList;

import io.straas.android.sdk.base.credential.CredentialFailReason;
import io.straas.android.sdk.base.interfaces.OnResultListener;
import io.straas.android.sdk.streaming.CameraController;
import io.straas.android.sdk.streaming.StreamConfig;
import io.straas.android.sdk.streaming.StreamManager;
import io.straas.android.sdk.streaming.demo.filter.GPUImageSupportFilter;
import io.straas.android.sdk.streaming.demo.filter.GrayImageFilter;
import io.straas.android.sdk.streaming.error.PrepareError;
import io.straas.android.sdk.streaming.error.StreamError;
import io.straas.android.sdk.streaming.interfaces.EventListener;
import io.straas.sdk.demo.MemberIdentity;
import jp.co.cyberagent.android.gpuimage.GPUImageColorInvertFilter;

import static io.straas.android.sdk.streaming.demo.R.id.filter;
import static io.straas.android.sdk.streaming.demo.R.id.flash;
import static io.straas.android.sdk.streaming.demo.R.id.switch_camera;
import static io.straas.android.sdk.streaming.demo.R.id.trigger;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "Streaming";
    private StreamManager mStreamManager;
    private CameraController mCameraController;
    private TextureView mTextureView;
    private EditText mEditTitle;
    private EditText mEditSynopsis;
    private Button btn_trigger, btn_switch, btn_flash, btn_filter;
    private int mFilter = 0;

    private static final String[] STREAM_PERMISSIONS = {
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO
    };
    private static final int STREAM_PERMISSION_REQUEST = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        StreamManager.validate(mInitListener);
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

    private OnResultListener<StreamManager, CredentialFailReason> mInitListener =
            new OnResultListener<StreamManager, CredentialFailReason>() {
        @Override
        public void onSuccess(StreamManager streamManager) {
            Log.d(TAG, "init success");
            mStreamManager = streamManager;
            preview();
        }

        @Override
        public void onFailure(CredentialFailReason error) {
            Log.d(TAG, "init fail");
        }
    };

    private StreamConfig getConfig() {
        StreamConfig config = new StreamConfig();
        config.setOutputSize(480, 480);
        config.setCamera(StreamConfig.CAMERA_FRONT);
        return config;
    }

    private void preview() {
        if (mStreamManager != null) {
            mStreamManager.prepare(getConfig(), mTextureView, mPrepareListener);
        }
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case trigger:
                if (btn_trigger.getText().equals(getResources().getString(R.string.start))) {
                    if (mStreamManager != null) {
                        btn_trigger.setText(getResources().getString(R.string.stop));
                        String title = mEditTitle.getText().toString();
                        String synopsis = mEditSynopsis.getText().toString();
                        mStreamManager.startStreaming(MemberIdentity.ME, title, synopsis, true, true,
                                mEventListener);
                    }
                } else {
                    btn_trigger.setText(getResources().getString(R.string.start));
                    if (mStreamManager != null) {
                        mStreamManager.stopStreaming();
                    }
                }
                break;
            case switch_camera:
                if (mCameraController != null) {
                    mCameraController.switchCamera();
                }
                break;
            case flash:
                if (mCameraController != null) {
                    mCameraController.toggleFlash();
                }
                break;
            case filter:
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
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mStreamManager != null) {
            mStreamManager.destroy();
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

    private void enableAllButtons() {
        btn_trigger.setEnabled(true);
        btn_switch.setEnabled(true);
        btn_flash.setEnabled(true);
        btn_filter.setEnabled(true);
    }

    private OnResultListener<CameraController, PrepareError> mPrepareListener =
            new OnResultListener<CameraController, PrepareError>() {
                @Override
                public void onSuccess(CameraController cameraController) {
                    Log.d(TAG, "Prepare success");
                    mCameraController = cameraController;
                    enableAllButtons();
                }

                @Override
                public void onFailure(PrepareError error) {
                    Log.d(TAG, "Prepare failure " + error);
                }
            };

    private EventListener mEventListener = new EventListener() {
        @Override
        public void onStreaming(String liveId) {
            Log.d(TAG, "onStreaming " + liveId);
        }

        @Override
        public void onError(StreamError error, String liveId) {
            Log.d(TAG, "onError " + error);
            btn_trigger.setText(getResources().getString(R.string.start));
        }

        @Override
        public void onFinished(boolean complete) {
            Log.d(TAG, "onFinished " + complete);
            btn_trigger.setText(getResources().getString(R.string.start));
        }
    };
}
