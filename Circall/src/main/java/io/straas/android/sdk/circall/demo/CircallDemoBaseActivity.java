package io.straas.android.sdk.circall.demo;

import android.Manifest;
import android.databinding.BindingMethod;
import android.databinding.BindingMethods;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import io.straas.android.sdk.circall.CircallManager;
import io.straas.android.sdk.circall.CircallStream;

@BindingMethods({
        @BindingMethod(type = android.widget.ImageView.class,
                attribute = "app:srcCompat",
                method = "setImageDrawable")
})
public abstract class CircallDemoBaseActivity extends AppCompatActivity {

    public static final String INTENT_CIRCALL_TOKEN = "circall_token";

    protected static final String ALBUM_FOLDER = "StraaS";

    public static final String[] STORAGE_PERMISSION = {
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    protected static final int STORAGE_REQUEST = 1;

    public static final int STATE_IDLE = 0;
    public static final int STATE_CONNECTING = 1;
    public static final int STATE_CONNECTED = 2;
    public static final int STATE_PUBLISHED = 3;
    public static final int STATE_SUBSCRIBED = 4;

    protected Bitmap mCapturedPicture;
    protected Handler mHandler = new Handler();

    protected CircallManager mCircallManager;
    protected CircallStream mRemoteCircallStream;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Utils.requestFullscreenMode(this);
    }

    //=====================================================================
    // Abstract methods
    //=====================================================================
    protected abstract String getTag();
}
