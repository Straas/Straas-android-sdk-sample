package io.straas.android.sdk.streaming.demo;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.support.annotation.Keep;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Size;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;

import io.straas.android.sdk.streaming.CameraController;
import io.straas.android.sdk.streaming.LiveEventConfig;
import io.straas.android.sdk.streaming.ScreencastStreamConfig;
import io.straas.android.sdk.streaming.StreamManager;
import io.straas.android.sdk.streaming.screencast.ScreencastSession;
import io.straas.android.sdk.streaming.error.StreamException.LiveCountLimitException;
import io.straas.sdk.demo.MemberIdentity;

@Keep
public final class MyScreencastSession extends ScreencastSession {

    private static final String TAG = MyScreencastSession.class.getSimpleName();

    private final Handler mMainThreadHandler = new Handler(Looper.getMainLooper());

    Runnable mUpdateStreamingTimeRunnable = new Runnable() {
        @Override
        public void run() {
            long seconds = (SystemClock.elapsedRealtime() - mStreamingStartTimeMillis) / 1000;
            if (mOverlayLayout != null) {
                mOverlayLayout.updateStreamingTimeView(seconds, isStreaming);
            }

            if (isStreaming) {
                mMainThreadHandler.postDelayed(this, 1000);
            }
        }
    };

    private StreamManager mStreamManager;

    private OverlayLayout mOverlayLayout;

    private String mLiveId;
    boolean isStreaming = false;
    long mStreamingStartTimeMillis;

    @Override
    public void onStreamInit(StreamManager streamManager) {
        mStreamManager = streamManager;
    }

    @Override
    public void showOverlay() {
        OverlayLayout.Listener overlayListener = new OverlayLayout.Listener() {
            @Override
            public void onMove() {
                mWindowManager.updateViewLayout(mOverlayLayout, mOverlayLayout.getParams());
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

        mOverlayLayout = OverlayLayout.create(mContext, overlayListener);
        mWindowManager.addView(mOverlayLayout, mOverlayLayout.getParams());
    }

    @Override
    public void removeOverlay() {
        mMainThreadHandler.post(new Runnable() {
            @Override
            public void run() {
                if (mOverlayLayout != null) {
                    mWindowManager.removeView(mOverlayLayout);
                    mOverlayLayout = null;
                }
            }
        });
    }

    @Override
    public Task<Void> prepare() {
        if (mStreamManager != null && mStreamManager.getStreamState() == StreamManager.STATE_IDLE) {
              return mStreamManager.prepare(getConfig())
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
        return Tasks.forException(new IllegalStateException());
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private ScreencastStreamConfig getConfig() {
        mMediaProjection = mMediaProjectionManager.getMediaProjection(mResultCode, mResultData);
        DisplayMetrics displayMetrics = getDisplayMetrics();
        Size size = getScreencastSize(displayMetrics);
        return new ScreencastStreamConfig.Builder()
            .mediaProjection(mMediaProjection)
            .videoResolution(size.getWidth(), size.getHeight())
            .densityDpi(displayMetrics.densityDpi)
            .build();
    }

    public void broadcastClick() {
        Log.d(TAG, "broadcastClick state:" + mStreamManager.getStreamState());
        if (!isStreaming) {
            startStreaming(mTitle, mSynopsis);
            mOverlayLayout.setStartViewEnabled(false);
            mOverlayLayout.updateLoadingView(true);
            isStreaming = true;
        } else {
            stopStreaming();
            mOverlayLayout.setStartViewSelected(false);
            mOverlayLayout.updateLoadingView(true);
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
                    mOverlayLayout.updateStreamingStatusOnUiThread(mMainThreadHandler, false);
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
                    mOverlayLayout.updateStreamingStatusOnUiThread(mMainThreadHandler, true);
                    mStreamingStartTimeMillis = SystemClock.elapsedRealtime();
                    mMainThreadHandler.post(mUpdateStreamingTimeRunnable);
                } else {
                    Log.e(TAG, "Start streaming fails " + task.getException());
                    mOverlayLayout.updateStreamingStatusOnUiThread(mMainThreadHandler, false);
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
                    mOverlayLayout.updateStreamingStatusOnUiThread(mMainThreadHandler, false);
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
    public void destroy() {
        if (mStreamManager != null) {
            mStreamManager.destroy();
        }
        super.destroy();
    }

}
