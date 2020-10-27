package io.straas.android.sdk.streaming.demo.camera;

import android.content.*;
import android.widget.*;

import io.straas.android.sdk.streaming.*;

import static io.straas.android.sdk.streaming.demo.camera.ConfigSettingActivity.EXTRA_MAX_BITRATE;

public class CameraCustomConfigActivity extends MainActivity {

    @Override
    protected void onCustomStreamConfig(StreamConfig.Builder builder) {
        Intent intent = getIntent();
        if (intent.hasExtra(EXTRA_MAX_BITRATE)) {
            int maxBitrate = intent.getIntExtra(EXTRA_MAX_BITRATE, 0);
            try {
                builder.maxBitrate(maxBitrate);
            } catch (IllegalArgumentException e) {
                Toast.makeText(this, e.toString(), Toast.LENGTH_LONG).show();
                onBackPressed();
            }
        }
    }
}
