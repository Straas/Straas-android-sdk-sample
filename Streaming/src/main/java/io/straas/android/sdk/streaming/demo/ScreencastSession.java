package io.straas.android.sdk.streaming.demo;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Size;
import android.view.Surface;
import android.view.View;
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
import io.straas.android.sdk.streaming.ScreencastStreamManager;
import io.straas.android.sdk.streaming.error.StreamException.LiveCountLimitException;
import io.straas.sdk.demo.MemberIdentity;

import static android.content.Context.MEDIA_PROJECTION_SERVICE;
import static android.content.Context.WINDOW_SERVICE;

public class ScreencastSession {

    private static final String TAG = ScreencastSession.class.getSimpleName();

    interface Listener {
        void onDestroy();
    }

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

    private final Context mContext;
    private final Listener mListener;

    private final WindowManager mWindowManager;
    private final MediaProjectionManager mMediaProjectionManager;
    private MediaProjection mMediaProjection;
    private ScreencastStreamManager mScreencastStreamManager;

    private OverlayLayout mOverlayLayout;

    private int mResultCode;
    private Intent mResultData;
    private String mTitle;
    private String mSynopsis;
    private int mPictureQuality;
    private String mLiveId;
    boolean isStreaming = false;
    long mStreamingStartTimeMillis;

    ScreencastSession(Context context, Listener mListener, int resultCode, Intent data, String title, String synopsis, int pictureQuality) {
        this.mContext = context;
        this.mListener = mListener;
        this.mResultCode = resultCode;
        this.mResultData = data;
        this.mPictureQuality = pictureQuality;
        this.mTitle = title;
        this.mSynopsis = synopsis;

        mWindowManager = (WindowManager) mContext.getSystemService(WINDOW_SERVICE);
        mMediaProjectionManager = (MediaProjectionManager) mContext.getSystemService(MEDIA_PROJECTION_SERVICE);
    }

    void showOverlay() {
        OverlayLayout.Listener overlayListener = new OverlayLayout.Listener() {
            @Override public void onMove() {
                mWindowManager.updateViewLayout(mOverlayLayout, mOverlayLayout.getParams());
            }
            @Override public void onStartClick() {
                broadcastClick();
            }
            @Override public void onDestroyClick() {
                removeOverlay();
                if (mListener != null) {
                    mListener.onDestroy();
                }
            }
        };

        mOverlayLayout = OverlayLayout.create(mContext, overlayListener);
        mWindowManager.addView(mOverlayLayout, mOverlayLayout.getParams());
    }

    private void removeOverlay() {
        if (mOverlayLayout != null) {
            mWindowManager.removeView(mOverlayLayout);
            mOverlayLayout = null;
        }
    }

    public void prepare() {
        ScreencastStreamManager.initialize(MemberIdentity.ME).continueWithTask(new Continuation<ScreencastStreamManager, Task<CameraController>>() {
            @Override
            public Task<CameraController> then(@NonNull Task<ScreencastStreamManager> task) throws Exception {
                if (!task.isSuccessful()) {
                    Log.e(TAG, "init fail " + task.getException());
                    throw task.getException();
                }
                mScreencastStreamManager = task.getResult();
                return preview();
            }
        });
    }

    private Task<CameraController> preview() {
        if (mScreencastStreamManager != null && mScreencastStreamManager.getStreamState() == ScreencastStreamManager.STATE_IDLE) {
              return mScreencastStreamManager.prepare(getConfig(), mOverlayLayout.getTextureView())
                      .addOnCompleteListener(new OnCompleteListener<CameraController>() {
                          @Override
                          public void onComplete(@NonNull Task<CameraController> task) {
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
        DisplayMetrics displayMetrics = new DisplayMetrics();
        mWindowManager.getDefaultDisplay().getMetrics(displayMetrics);
        Size size = getScreencastSize(displayMetrics);
        return new ScreencastStreamConfig.Builder()
            .mediaProjection(mMediaProjection)
            .videoResolution(size.getWidth(), size.getHeight())
            .densityDpi(displayMetrics.densityDpi)
            .build();
    }

    private Size getScreencastSize(DisplayMetrics displayMetrics) {
        float ratio;
        // Portrait mode
        if (displayMetrics.widthPixels < displayMetrics.heightPixels) {
            ratio = (float) displayMetrics.widthPixels / mPictureQuality;
            return new Size(mPictureQuality, (int) (displayMetrics.heightPixels / ratio));
        }
        ratio = (float) displayMetrics.heightPixels / mPictureQuality;
        return new Size((int) (displayMetrics.widthPixels / ratio), mPictureQuality);
    }

    public void broadcastClick() {
        Log.d(TAG, "broadcastClick state:" + mScreencastStreamManager.getStreamState());
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

    private void startStreaming(String title, String synopsis) {
        mScreencastStreamManager.createLiveEvent(new LiveEventConfig.Builder()
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
        mScreencastStreamManager.startStreaming(liveId).addOnCompleteListener(new OnCompleteListener<String>() {
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

    private void stopStreaming() {
        mScreencastStreamManager.stopStreaming().addOnCompleteListener(new OnCompleteListener<Void>() {
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
        if (mScreencastStreamManager != null || !TextUtils.isEmpty(mLiveId)) {
            mScreencastStreamManager.cleanLiveEvent(mLiveId).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Log.d(TAG, "End live event succeeds: " + mLiveId);
                    mLiveId = null;
                }
            });
        }
    }

    public void destroy() {
        if (mScreencastStreamManager != null) {
            mScreencastStreamManager.destroy();
        }
    }
}
