package io.straas.android.sdk.streaming.demo.screencast;

import android.annotation.TargetApi;
import android.app.Notification;
import android.content.Context;
import android.content.Intent;
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
import android.support.annotation.Nullable;
import android.support.v4.util.SimpleArrayMap;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Size;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import io.straas.android.sdk.demo.R;
import io.straas.android.sdk.streaming.LiveEventConfig;
import io.straas.android.sdk.streaming.ScreencastStreamConfig;
import io.straas.android.sdk.streaming.StreamManager;
import io.straas.android.sdk.streaming.StreamStatsReport;
import io.straas.android.sdk.streaming.error.StreamException.LiveCountLimitException;
import io.straas.android.sdk.streaming.interfaces.EventListener;
import io.straas.android.sdk.streaming.screencast.ScreencastSession;

import static android.content.Context.MEDIA_PROJECTION_SERVICE;
import static android.content.Context.WINDOW_SERVICE;
import static io.straas.android.sdk.streaming.StreamManager.STATE_CONNECTING;

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
@Keep
public final class MyScreencastSession implements ScreencastSession {

    private static final String TAG = MyScreencastSession.class.getSimpleName();

    public static final String EXTRA_SCREEN_CAPTURE_INTENT_RESULT_CODE = "EXTRA_SCREEN_CAPTURE_INTENT_RESULT_CODE";
    public static final String EXTRA_SCREEN_CAPTURE_INTENT_RESULT_DATA = "EXTRA_SCREEN_CAPTURE_INTENT_RESULT_DATA";
    public static final String EXTRA_LIVE_EVENT_TITLE = "EXTRA_LIVE_EVENT_TITLE";
    public static final String EXTRA_LIVE_EVENT_SYNOPSIS = "EXTRA_LIVE_EVENT_SYNOPSIS";
    public static final String EXTRA_LIVE_VIDEO_QUALITY = "EXTRA_LIVE_VIDEO_QUALITY";

    private static final int EVENT_UPDATE_STREAMING_TIME = 101;

    private static final SimpleArrayMap<Integer, Size> sResolutionLookup = new SimpleArrayMap<>();

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
            }
        }
    };

    private class MediaProjectionStopCallback extends MediaProjection.Callback {
        @Override
        public void onStop() {
            if (mMediaProjection != null) {
                mMediaProjection.unregisterCallback(MediaProjectionStopCallback.this);
                mMediaProjection = null;
            }
        }
    }

    private EventListener mEventListener = new EventListener() {
        @Override
        public void onStreamStatsReportUpdate(StreamStatsReport streamStatsReport) {
        }
        @Override
        public void onError(Exception error, @Nullable String liveId) {
            Log.e(TAG, "onError " + error);
            showErrorToast("EventListener onError: " + error);
            isStreaming = false;
            updateStreamingStatusUi();
            // In prepared state now, and your decision to end live or not
            endLiveEvent();
        }
    };

    private Context mContext;
    private SessionListener mListener;

    private WindowManager mWindowManager;
    private MediaProjectionManager mMediaProjectionManager;
    private MediaProjection mMediaProjection;
    private StreamManager mStreamManager;

    private int mResultCode;
    private Intent mResultData;
    private String mTitle;
    private String mSynopsis;
    private int mVideoQuality;

    private ControlOverlayLayout mControlOverlayLayout;
    private CameraOverlayLayout mCameraOverlayLayout;

    private String mLiveId;
    private boolean isPrepared = false;
    private boolean isStreaming = false;
    private long mStreamingStartTimeMillis;

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onSessionCreate(Context context, SessionListener listener, Bundle bundle) {
        this.mContext = context;
        this.mListener = listener;

        mWindowManager = (WindowManager) mContext.getSystemService(WINDOW_SERVICE);
        mMediaProjectionManager = (MediaProjectionManager) mContext.getSystemService(MEDIA_PROJECTION_SERVICE);

        this.mResultCode = bundle.getInt(EXTRA_SCREEN_CAPTURE_INTENT_RESULT_CODE);
        this.mResultData = bundle.getParcelable(EXTRA_SCREEN_CAPTURE_INTENT_RESULT_DATA);
        this.mVideoQuality = bundle.getInt(EXTRA_LIVE_VIDEO_QUALITY);
        this.mTitle = bundle.getString(EXTRA_LIVE_EVENT_TITLE);
        this.mSynopsis = bundle.getString(EXTRA_LIVE_EVENT_SYNOPSIS);
    }

    @Override
    public void onStreamManagerInitComplete(@NonNull Task<StreamManager> task) {
        if (!task.isSuccessful()) {
            Log.e(TAG, "StreamInit fail " + task.getException());
            showErrorToast("StreamInit fail:" + task.getException());
            destroyService();
            return;
        }

        mStreamManager = task.getResult();
        mStreamManager.addEventListener(mEventListener);
        showOverlay();
        prepare();
    }

    private void showOverlay() {
        mControlOverlayLayout = ControlOverlayLayout.create(mContext, mOverlayListener);
        mCameraOverlayLayout = CameraOverlayLayout.create(mContext, mOverlayListener);
        mWindowManager.addView(mControlOverlayLayout, mControlOverlayLayout.getParams());
        mWindowManager.addView(mCameraOverlayLayout, mCameraOverlayLayout.getParams());
    }

    private void removeOverlaOnUiThread() {
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

    private void prepare() {
        if (mStreamManager != null && mStreamManager.getStreamState() == StreamManager.STATE_IDLE) {
            mStreamManager.prepare(getConfig())
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Log.d(TAG, "Prepare succeeds");
                                isPrepared = true;
                                updateStreamingStatusUi();
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
        // register media projection stop callback
        mMediaProjection.registerCallback(new MediaProjectionStopCallback(), null);
        Size size = getScreencastSize(mVideoQuality);
        DisplayMetrics displayMetrics = getDisplayMetrics();
        return new ScreencastStreamConfig.Builder()
                .mediaProjection(mMediaProjection)
                .videoResolution(size.getWidth(), size.getHeight())
                .densityDpi(displayMetrics.densityDpi)
                .build();
    }

    private void broadcastClick() {
        if (mStreamManager == null || !isPrepared || mMediaProjection == null) {
            Log.e(TAG, "mStreamManager is null or not prepared or stopped MediaProjection.");
            return;
        }

        if (!isStreaming) {
            startStreaming(mTitle, mSynopsis);
            mControlOverlayLayout.updateStreamingStatus(STATE_CONNECTING);
            isStreaming = true;
        } else {
            stopStreaming();
            mControlOverlayLayout.updateStreamingStatus(STATE_CONNECTING);
            isStreaming = false;
        }
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
                    isStreaming = false;
                    showErrorToast("Create live event fails: " + error);
                    updateStreamingStatusUi();
                }
            }
        });
    }

    private void startStreaming(final String liveId) {
        mStreamManager.startStreamingWithLiveId(liveId).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Log.d(TAG, "Start streaming succeeds");
                    updateStreamingStatusUi();
                    mStreamingStartTimeMillis = SystemClock.elapsedRealtime();
                    mMainThreadHandler.removeMessages(EVENT_UPDATE_STREAMING_TIME);
                    mMainThreadHandler.sendEmptyMessage(EVENT_UPDATE_STREAMING_TIME);
                    mListener.updateNotification(new Notification.Builder(mContext)
                            .setSmallIcon(R.drawable.straas_icon_white_24px)
                            .setContentTitle(mContext.getString(R.string.screencast_service_title))
                            .setContentText("Streaming!!!")
                            .setAutoCancel(true)
                            .build());
                } else {
                    Log.e(TAG, "Start streaming fails " + task.getException());
                    isStreaming = false;
                    showErrorToast("Start streaming fails " + task.getException());
                    updateStreamingStatusUi();
                }
            }
        });
    }

    private void stopStreaming() {
        mStreamManager.stopStreaming().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Log.d(TAG, "Stop succeeds");
                    updateStreamingStatusUi();
                    endLiveEvent();
                } else {
                    Log.e(TAG, "Stop fails: " + task.getException());
                }
            }
        });
    }

    private void endLiveEvent() {
        if (mStreamManager != null || !TextUtils.isEmpty(mLiveId)) {
            mStreamManager.endLiveEvent(mLiveId).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Log.d(TAG, "End live event succeeds: " + mLiveId);
                    mLiveId = null;
                    destroyService();
                }
            });
        }
    }

    @Override
    public Notification getNotification() {
        String title = mContext.getString(R.string.screencast_service_title);
        String subtitle = mContext.getString(R.string.screencast_service_subtitle);
        return new Notification.Builder(mContext)
                .setSmallIcon(R.drawable.straas_icon_white_24px)
                .setContentTitle(title)
                .setContentText(subtitle)
                .setAutoCancel(true)
                .build();
    }

    private void updateStreamingStatusUi() {
        if (mControlOverlayLayout != null && mStreamManager != null) {
            mControlOverlayLayout.updateStreamingStatusOnUiThread(mStreamManager.getStreamState());
        }
    }

    @Override
    public void onSessionDestroy() {
        if (mStreamManager != null) {
            mStreamManager.destroy();
        }

        if (mMediaProjection != null) {
            mMediaProjection.stop();
        }
        removeOverlaOnUiThread();
    }

    private void showErrorToast(String errorText) {
        Toast.makeText(mContext, errorText, Toast.LENGTH_LONG).show();
    }

    private void destroyService() {
        if (mListener != null) {
            mListener.destroyService();
        }
    }

    private DisplayMetrics getDisplayMetrics() {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        mWindowManager.getDefaultDisplay().getMetrics(displayMetrics);
        return displayMetrics;
    }

    private Size getScreencastSize(int videoQuality) {
        return sResolutionLookup.get(videoQuality);
    }


    private OverlayLayout.Listener mOverlayListener = new OverlayLayout.Listener() {
        @Override
        public void onMove(OverlayLayout overlayLayout) {
            mWindowManager.updateViewLayout(overlayLayout, overlayLayout.getParams());
        }

        @Override
        public void onClick(final View v) {
            switch (v.getId()) {
                case R.id.screencast_overlay_start:
                    broadcastClick();
                    break;
                case R.id.screencast_overlay_finish:
                    destroyService();
                    break;
            }
        }
    };

}
