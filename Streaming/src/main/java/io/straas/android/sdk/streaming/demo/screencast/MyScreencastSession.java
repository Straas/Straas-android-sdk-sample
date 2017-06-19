package io.straas.android.sdk.streaming.demo.screencast;

import android.annotation.TargetApi;
import android.app.Notification;
import android.content.Context;
import android.content.Intent;
import android.hardware.SensorManager;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.support.annotation.Keep;
import android.support.annotation.NonNull;
import android.support.v4.util.SimpleArrayMap;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Size;
import android.view.OrientationEventListener;
import android.view.WindowManager;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;

import io.straas.android.sdk.demo.R;

import io.straas.android.sdk.streaming.CameraController;
import io.straas.android.sdk.streaming.LiveEventConfig;
import io.straas.android.sdk.streaming.ScreencastStreamConfig;
import io.straas.android.sdk.streaming.StreamConfig;
import io.straas.android.sdk.streaming.StreamManager;
import io.straas.android.sdk.streaming.screencast.ScreencastSession;
import io.straas.android.sdk.streaming.error.StreamException.LiveCountLimitException;
import io.straas.sdk.demo.MemberIdentity;

import static android.content.Context.MEDIA_PROJECTION_SERVICE;
import static android.content.Context.WINDOW_SERVICE;

@Keep
public final class MyScreencastSession implements ScreencastSession {

    private static final String TAG = MyScreencastSession.class.getSimpleName();

    public static final String EXTRA_SCREEN_CAPTURE_INTENT_RESULT_CODE = "EXTRA_SCREEN_CAPTURE_INTENT_RESULT_CODE";
    public static final String EXTRA_SCREEN_CAPTURE_INTENT_RESULT_DATA = "EXTRA_SCREEN_CAPTURE_INTENT_RESULT_DATA";
    public static final String EXTRA_LIVE_EVENT_TITLE = "EXTRA_LIVE_EVENT_TITLE";
    public static final String EXTRA_LIVE_EVENT_SYNOPSIS = "EXTRA_LIVE_EVENT_SYNOPSIS";
    public static final String EXTRA_LIVE_VIDEO_QUALITY = "EXTRA_LIVE_VIDEO_QUALITY";

    static final int EVENT_UPDATE_STREAMING_TIME = 101;
    static final int EVENT_ORIENTATION_CHANGED = 102;

    private static final SimpleArrayMap<Integer, Size> sResolutionLookup = new SimpleArrayMap<Integer, Size>();

    static {
        sResolutionLookup.put(240, new Size(426, 240));
        sResolutionLookup.put(360, new Size(640, 360));
        sResolutionLookup.put(480, new Size(854, 480));
        sResolutionLookup.put(720, new Size(1280, 720));
    }

    private final Handler mMainThreadHandler = new Handler(Looper.getMainLooper()) {
        public void handleMessage(Message msg) {
            Log.d(TAG, "handleMessage: " + msg.what);
            switch(msg.what) {
                case EVENT_UPDATE_STREAMING_TIME:
                    long seconds = (SystemClock.elapsedRealtime() - mStreamingStartTimeMillis) / 1000;
                    if (mControlOverlayLayout != null) {
                        mControlOverlayLayout.updateStreamingTimeView(seconds, isStreaming);
                    }

                    if (isStreaming) {
                        mMainThreadHandler.removeMessages(EVENT_UPDATE_STREAMING_TIME);
                        mMainThreadHandler.sendEmptyMessageDelayed(EVENT_UPDATE_STREAMING_TIME, 1000);
                    }
                    break;
                case EVENT_ORIENTATION_CHANGED:
                    if (mCameraOverlayLayout != null) {
                        mCameraOverlayLayout.onOrientationChanged();
                    }
                    break;
            }
        }
    };

    private Context mContext;
    private Listener mListener;

    private WindowManager mWindowManager;
    private MediaProjectionManager mMediaProjectionManager;
    private MediaProjection mMediaProjection;
    private StreamManager mStreamManager;
    private OrientationEventListener mOrientationEventListener;

    private int mResultCode;
    private Intent mResultData;
    private String mTitle;
    private String mSynopsis;
    private int mVideoQuality;

    private ControlOverlayLayout mControlOverlayLayout;
    private CameraOverlayLayout mCameraOverlayLayout;

    private String mLiveId;
    boolean isStreaming = false;
    long mStreamingStartTimeMillis;

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void initContext(Context context, Listener listener) {
        this.mContext = context;
        this.mListener = listener;

        mWindowManager = (WindowManager) mContext.getSystemService(WINDOW_SERVICE);
        mMediaProjectionManager = (MediaProjectionManager) mContext.getSystemService(MEDIA_PROJECTION_SERVICE);
        registerOrientationEventListener();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void getScreencastExtras(Bundle bundle) {
        this.mResultCode = bundle.getInt(EXTRA_SCREEN_CAPTURE_INTENT_RESULT_CODE);
        this.mResultData = bundle.getParcelable(EXTRA_SCREEN_CAPTURE_INTENT_RESULT_DATA);
        this.mVideoQuality = bundle.getInt(EXTRA_LIVE_VIDEO_QUALITY);
        this.mTitle = bundle.getString(EXTRA_LIVE_EVENT_TITLE);
        this.mSynopsis = bundle.getString(EXTRA_LIVE_EVENT_SYNOPSIS);
    }

    @Override
    public void onStreamInit(StreamManager streamManager) {
        mStreamManager = streamManager;
        prepare();
    }

    @Override
    public void showOverlay() {
        OverlayLayout.Listener overlayListener = new OverlayLayout.Listener() {
            @Override
            public void onMove(OverlayLayout overlayLayout) {
                mWindowManager.updateViewLayout(overlayLayout, overlayLayout.getParams());
            }
            @Override
            public void onStartClick() {
                broadcastClick();
            }
            @Override
            public void onDestroyClick() {
                if (mListener != null) {
                    mListener.onDestroy();
                }
            }
        };

        mControlOverlayLayout = ControlOverlayLayout.create(mContext, overlayListener);
        mCameraOverlayLayout = CameraOverlayLayout.create(mContext, overlayListener);
        mWindowManager.addView(mControlOverlayLayout, mControlOverlayLayout.getParams());
        mWindowManager.addView(mCameraOverlayLayout, mCameraOverlayLayout.getParams());
    }

    public void removeOverlaOnUiThread() {
        mMainThreadHandler.post(new Runnable() {
            @Override
            public void run() {
                if (mControlOverlayLayout != null) {
                    mWindowManager.removeView(mControlOverlayLayout);
                    mControlOverlayLayout = null;
                }
                if (mCameraOverlayLayout != null) {
                    mWindowManager.removeView(mCameraOverlayLayout);
                    mCameraOverlayLayout = null;
                }
            }
        });
    }

    public void prepare() {
        if (mStreamManager != null && mStreamManager.getStreamState() == StreamManager.STATE_IDLE) {
            mStreamManager.prepare(getConfig())
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Log.d(TAG, "Prepare succeeds");
                            } else {
                                Log.e(TAG, "Prepare fails " + task.getException());
                            }
                        }
                });
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private ScreencastStreamConfig getConfig() {
        mMediaProjection = mMediaProjectionManager.getMediaProjection(mResultCode, mResultData);
        Size size = getScreencastSize(mVideoQuality);
        DisplayMetrics displayMetrics = getDisplayMetrics();
        return new ScreencastStreamConfig.Builder()
            .mediaProjection(mMediaProjection)
            .videoResolution(size.getWidth(), size.getHeight())
            .densityDpi(displayMetrics.densityDpi)
            .build();
    }

    public void broadcastClick() {
        if (mStreamManager == null) {
            Log.e(TAG, "mStreamManager is null.");
            return;
        }

        Log.d(TAG, "broadcastClick state:" + mStreamManager.getStreamState());
        if (!isStreaming) {
            startStreaming(mTitle, mSynopsis);
            mControlOverlayLayout.setStartViewEnabled(false);
            mControlOverlayLayout.updateLoadingView(true);
            isStreaming = true;
        } else {
            stopStreaming();
            mControlOverlayLayout.setStartViewSelected(false);
            mControlOverlayLayout.updateLoadingView(true);
            isStreaming = false;
        }
    }

    public void startStreaming(String title, String synopsis) {
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
                    mControlOverlayLayout.updateStreamingStatusOnUiThread(false);
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
                    mControlOverlayLayout.updateStreamingStatusOnUiThread(true);
                    mStreamingStartTimeMillis = SystemClock.elapsedRealtime();
                    mMainThreadHandler.removeMessages(EVENT_UPDATE_STREAMING_TIME);
                    mMainThreadHandler.sendEmptyMessage(EVENT_UPDATE_STREAMING_TIME);
                } else {
                    Log.e(TAG, "Start streaming fails " + task.getException());
                    mControlOverlayLayout.updateStreamingStatusOnUiThread(false);
                }
            }
        });
    }

    public void stopStreaming() {
        mStreamManager.stopStreaming().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Log.d(TAG, "Stop succeeds");
                    mControlOverlayLayout.updateStreamingStatusOnUiThread(false);
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
                    if (mListener != null) {
                        mListener.onDestroy();
                    }
                }
            });
        }
    }

    @Override
    public Notification getNotification() {
        String title = mContext.getString(R.string.screencast_service_title);
        String subtitle = mContext.getString(R.string.screencast_service_subtitle);
        Notification notification = new Notification.Builder(mContext)
            .setContentTitle(title)
            .setContentText(subtitle)
            .setAutoCancel(true)
            .build();
        return notification;
    }

    @Override
    public void onStreamInitError(Exception e) {
        Log.e(TAG, "onStreamInitError: " + e);
    }

    @Override
    public void onDestroy() {
        if (mStreamManager != null) {
            mStreamManager.destroy();
        }
        unregisterOrientationEventListener();

        if (mMediaProjection != null) {
            mMediaProjection.stop();
        }
        removeOverlaOnUiThread();
    }

    void registerOrientationEventListener() {
        mOrientationEventListener = new OrientationEventListener(mContext, SensorManager.SENSOR_DELAY_NORMAL) {
            @Override
            public void onOrientationChanged(int orientation) {
                mMainThreadHandler.removeMessages(EVENT_ORIENTATION_CHANGED);
                mMainThreadHandler.sendEmptyMessage(EVENT_ORIENTATION_CHANGED);
            }
        };
        mOrientationEventListener.enable();
    }

    void unregisterOrientationEventListener() {
        if (mOrientationEventListener != null) {
            mOrientationEventListener.disable();
        }
    }

    public DisplayMetrics getDisplayMetrics() {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        mWindowManager.getDefaultDisplay().getMetrics(displayMetrics);
        return displayMetrics;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    protected Size getScreencastSize(int videoQuality) {
        return sResolutionLookup.get(videoQuality);
    }

}
