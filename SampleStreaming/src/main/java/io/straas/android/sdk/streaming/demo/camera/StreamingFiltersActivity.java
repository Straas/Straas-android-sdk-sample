package io.straas.android.sdk.streaming.demo.camera;

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
import android.widget.SeekBar;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;

import java.util.ArrayList;

import io.straas.android.sdk.demo.R;
import io.straas.android.sdk.streaming.CameraController;
import io.straas.android.sdk.streaming.StreamConfig;
import io.straas.android.sdk.streaming.StreamManager;
import io.straas.android.sdk.streaming.error.StreamException;
import io.straas.android.sdk.streaming.filter.SkinBeautifyFilter;
import io.straas.sdk.demo.MemberIdentity;

public class StreamingFiltersActivity extends AppCompatActivity {

    private static final String TAG = StreamingFiltersActivity.class.getSimpleName();

    private static final String[] STREAM_PERMISSIONS = {
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO
    };
    private static final int STREAM_PERMISSION_REQUEST = 1;

    private static final float MAX_BRIGHTNESS = 1.0f;
    private static final float MIN_BRIGHTNESS = 0.0f;
    private static final float MAX_SKIN_SMOOTHNESS = 2.0f;
    private static final float MIN_SKIN_SMOOTHNESS = 0.0f;

    private StreamManager mStreamManager;
    private CameraController mCameraController;
    private TextureView mTextureView;
    private Button mSwitchCameraButton;
    private ToggleButton mSkinBeautifyToggleButton;
    private SkinBeautifyFilter mSkinBeautifyFilter;
    private SeekBar mBrightnessSeekBar, mSkinSmoothnessSeekBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filters);

        StreamManager.initialize(MemberIdentity.ME)
                .addOnCompleteListener(new OnCompleteListener<StreamManager>() {
                    @Override
                    public void onComplete(@NonNull Task<StreamManager> task) {
                        if (!task.isSuccessful()) {
                            Log.e(TAG, "init fail " + task.getException());
                            return;
                        }
                        mStreamManager = task.getResult();
                        preview();
                    }
                });
        mTextureView = findViewById(R.id.preview);
        mTextureView.setKeepScreenOn(true);

        mSwitchCameraButton = findViewById(R.id.switch_camera);
        mSkinBeautifyToggleButton = findViewById(R.id.skin_beautify);
        try {
            mSkinBeautifyFilter = new SkinBeautifyFilter(0.5f, 0.5f);
        } catch (StreamException.InternalException e) {
            Log.e(TAG, "Generate SkinBeautifyFilter failed: " + e);
        }

        initSeekBars();
        checkPermissions();
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

    private void destroy() {
        if (mStreamManager != null) {
            mStreamManager.destroy();
        }
    }

    private int checkPermissions() {
        String[] requestPermissions = getPermissionsRequestArray(STREAM_PERMISSIONS);
        if (requestPermissions.length != 0) {
            ActivityCompat.requestPermissions(StreamingFiltersActivity.this, requestPermissions,
                    STREAM_PERMISSION_REQUEST);
        }
        return requestPermissions.length;
    }

    private String[] getPermissionsRequestArray(String[] permissions) {
        ArrayList<String> requestArray = new ArrayList<>();
        for (String permission : permissions) {
            if (ActivityCompat.checkSelfPermission(StreamingFiltersActivity.this, permission) !=
                    PackageManager.PERMISSION_GRANTED) {
                requestArray.add(permission);
            }
        }
        return requestArray.toArray(new String[0]);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == STREAM_PERMISSION_REQUEST) {
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(StreamingFiltersActivity.this, getResources().getString(R.string.hint_need_permission), Toast.LENGTH_SHORT).show();
                    return;
                }
            }
            preview();
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
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

    private StreamConfig getConfig() {
        return new StreamConfig.Builder()
                .camera(StreamConfig.CAMERA_FRONT)
                .fitAllCamera(true)
                .build();
    }

    private void enableAllButtons() {
        mSwitchCameraButton.setEnabled(true);
        mSkinBeautifyToggleButton.setEnabled(mSkinBeautifyFilter != null);
    }

    public void switchCamera(View view) {
        if (mCameraController != null) {
            mCameraController.switchCamera();
        }
    }

    public void skinBeautify(View view) {
        if (mStreamManager == null || mSkinBeautifyFilter == null) {
            return;
        }
        mStreamManager.setFilter(mSkinBeautifyToggleButton.isChecked()
                ? mSkinBeautifyFilter
                : null);
        mBrightnessSeekBar.setEnabled(mSkinBeautifyToggleButton.isChecked());
        mSkinSmoothnessSeekBar.setEnabled(mSkinBeautifyToggleButton.isChecked());
    }

    private void initSeekBars() {
        mBrightnessSeekBar = findViewById(R.id.seek_bar_brightness);
        mBrightnessSeekBar.setEnabled(false);
        mSkinSmoothnessSeekBar = findViewById(R.id.seek_bar_skin_smoothness);
        mSkinSmoothnessSeekBar.setEnabled(false);

        if (mSkinBeautifyFilter == null) {
            return;
        }
        mBrightnessSeekBar.setProgress((int) mapValueFromRangeToRange(
                mSkinBeautifyFilter.getBrightnessLevel(), MIN_BRIGHTNESS, MAX_BRIGHTNESS,
                0, mBrightnessSeekBar.getMax()));
        mBrightnessSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    mSkinBeautifyFilter.setBrightnessLevel(
                            mapValueFromRangeToRange(progress, 0, seekBar.getMax(),
                                    MIN_BRIGHTNESS, MAX_BRIGHTNESS));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        mSkinSmoothnessSeekBar.setProgress((int) mapValueFromRangeToRange(
                mSkinBeautifyFilter.getSkinSmoothnessLevel(), MIN_SKIN_SMOOTHNESS, MAX_SKIN_SMOOTHNESS,
                0, mSkinSmoothnessSeekBar.getMax()));
        mSkinSmoothnessSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    mSkinBeautifyFilter.setSkinSmoothnessLevel(
                            mapValueFromRangeToRange(progress, 0, seekBar.getMax(),
                                    MIN_SKIN_SMOOTHNESS, MAX_SKIN_SMOOTHNESS));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    private float mapValueFromRangeToRange(float value, float oldMin, float oldMax, float newMin, float newMax) {
        float oldRangeSize = oldMax - oldMin;
        float newRangeSize = newMax - newMin;
        float oldValueScale = (value - oldMin) / oldRangeSize;
        return newMin + newRangeSize * oldValueScale;
    }
}
