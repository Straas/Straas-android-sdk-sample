package io.straas.android.sdk.streaming.demo;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;

import io.straas.android.sdk.demo.R;

public final class ScreencastService extends Service {
    private static final String TAG = ScreencastService.class.getSimpleName();

    private static final String EXTRA_RESULT_CODE = "result_code";
    private static final String EXTRA_DATA = "data";
    private static final String EXTRA_TITLE = "title";
    private static final String EXTRA_SYNOPSIS = "synopsis";
    private static final String EXTRA_PICTURE_QUALITY = "picture_quality";

    private static final int NOTIFICATION_ID = 55688;

    private ScreencastSession mScreencastSession;
    boolean mIsServiceStarted = false;

    private final ScreencastSession.Listener listener = new ScreencastSession.Listener() {
        @Override public void onDestroy() {
            stopSelf();
        }
    };

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    static public Intent newIntent(Context context, int resultCode, Intent data, String title, String synopsis, int pictureQuality) {
        Intent intent = new Intent();
        intent.setClassName("io.straas.android.sdk.demo", "io.straas.android.sdk.streaming.demo.ScreencastService");
        intent.putExtra(EXTRA_RESULT_CODE, resultCode);
        intent.putExtra(EXTRA_DATA, data);
        intent.putExtra(EXTRA_TITLE, title);
        intent.putExtra(EXTRA_SYNOPSIS, synopsis);
        intent.putExtra(EXTRA_PICTURE_QUALITY, pictureQuality);
        return intent;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public int onStartCommand(@NonNull Intent intent, int flags, int startId) {
        if (mIsServiceStarted) {
            return START_NOT_STICKY;
        }
        mIsServiceStarted = true;

        int resultCode = intent.getIntExtra(EXTRA_RESULT_CODE, 0);
        Intent data = intent.getParcelableExtra(EXTRA_DATA);
        if (resultCode == 0 || data == null) {
            throw new IllegalStateException("Result code or data missing.");
        }

        int pictureQuality = intent.getIntExtra(EXTRA_PICTURE_QUALITY, 0);
        String title = intent.getStringExtra(EXTRA_TITLE);
        String synopsis = intent.getStringExtra(EXTRA_SYNOPSIS);
        initScreencastSession(resultCode, data, title, synopsis, pictureQuality);

        startForeground();
        return START_NOT_STICKY;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    void initScreencastSession(int resultCode, Intent data, String title, String synopsis, int pictureQuality) {
        mScreencastSession = new ScreencastSession(this, listener, resultCode, data, title, synopsis, pictureQuality);
        mScreencastSession.showOverlay();
        mScreencastSession.prepare();
    }

    void destroyScreencastSession() {
        mScreencastSession.destroy();
        mScreencastSession = null;
    }

    void startForeground() {
        Context context = getApplicationContext();
        String title = context.getString(R.string.screencast_service_title);
        String subtitle = context.getString(R.string.screencast_service_subtitle);
        Notification notification = new Notification.Builder(context)
            .setContentTitle(title)
            .setContentText(subtitle)
            .setAutoCancel(true)
            .build();
        startForeground(NOTIFICATION_ID, notification);
    }

    @Override
    public void onDestroy() {
        destroyScreencastSession();
        super.onDestroy();
    }

    @Override
    public IBinder onBind(@NonNull Intent intent) {
        throw new AssertionError("Not supported.");
    }

}
