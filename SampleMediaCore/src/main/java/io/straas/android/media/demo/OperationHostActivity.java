package io.straas.android.media.demo;

import android.content.*;

import io.straas.android.sdk.media.StraasMediaCore.*;

import static io.straas.android.media.demo.MediaCoreHostActivity.*;

public class OperationHostActivity extends OperationActivity {

    @Override
    protected void onCustomizeMediaCoreConfig(MediaCoreConfig.Builder builder) {
        Intent intent = getIntent();
        String restHost = intent.getStringExtra(EXTRA_REST_HOST);

        builder.setRestHost(restHost);
    }
}
