package io.straas.android.sdk.streaming.demo;

import android.app.Notification;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.hardware.SensorManager;
import android.media.projection.MediaProjection;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.support.annotation.Keep;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Size;
import android.view.OrientationEventListener;

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

@Keep
public final class MyScreencastSession extends ScreencastSession {

    private static final String TAG = MyScreencastSession.class.getSimpleName();

    private final Handler mMainThreadHandler = new Handler(Looper.getMainLooper());

    private Runnable mUpdateStreamingTimeRunnable = new Runnable() {
        @Override
        public void run() {
            long seconds = (SystemClock.elapsedRealtime() - mStreamingStartTimeMillis) / 1000;
            if (mControlOverlayLayout != null) {
                mControlOverlayLayout.updateStreamingTimeView(seconds, isStreaming);
            }

            if (isStreaming) {
                mMainThreadHandler.postDelayed(this, 1000);
            }
        }
    };

    private OrientationEventListener mOrientationEventListener;
    private StreamManager mStreamManager;
    private MediaProjection mMediaProjection;

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

    @Override
    public void initContext(Context context, Listener listener) {
        super.initContext(context, listener);
        registerOrientationEventListener();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void parseScreencastConfig(Bundle bundle) {
        this.mResultCode = bundle.getInt(StreamConfig.EXTRA_SCREEN_CAPTURE_INTENT_RESULT_CODE);
        this.mResultData = bundle.getParcelable(StreamConfig.EXTRA_SCREEN_CAPTURE_INTENT_RESULT_DATA);
        this.mVideoQuality = bundle.getInt(StreamConfig.EXTRA_LIVE_VIDEO_QUALITY);
        this.mTitle = bundle.getString(StreamConfig.EXTRA_LIVE_EVENT_TITLE);
        this.mSynopsis = bundle.getString(StreamConfig.EXTRA_LIVE_EVENT_SYNOPSIS);
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

    @Override
    public void stopMediaProjection() {
        if (mMediaProjection != null) {
            mMediaProjection.stop();
        }
    }

    @Override
    public void removeOverlay() {
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
                    mMainThreadHandler.post(mUpdateStreamingTimeRunnable);
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
    public void destroy() {
        if (mStreamManager != null) {
            mStreamManager.destroy();
        }
        unregisterOrientationEventListener();
        super.destroy();
    }

    void registerOrientationEventListener() {
        mOrientationEventListener = new OrientationEventListener(mContext, SensorManager.SENSOR_DELAY_NORMAL) {
            @Override
            public void onOrientationChanged(int orientation) {
                if (mCameraOverlayLayout != null) {
                    mCameraOverlayLayout.onOrientationChanged();
                }
            }
        };
        mOrientationEventListener.enable();
    }

    void unregisterOrientationEventListener() {
        if (mOrientationEventListener != null) {
            mOrientationEventListener.disable();
        }
    }
}
