package io.straas.android.media.demo;

import android.content.*;

import androidx.loader.content.Loader;
import io.straas.android.sdk.demo.common.*;
import io.straas.android.sdk.media.*;

import static io.straas.android.media.demo.MediaCoreHostActivity.*;

public class StraasPlayerHostActivity extends StraasPlayerActivity {

    @Override
    protected Loader<StraasMediaCore> createStraasMediaCoreLoader() {
        Intent intent = getIntent();
        String restHost = intent.getStringExtra(EXTRA_REST_HOST);

        return new StraasMediaCoreLoader(StraasPlayerHostActivity.this, MemberIdentity.ME, restHost);
    }
}
