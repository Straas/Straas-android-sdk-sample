package io.straas.android.sdk.streaming.demo.screencast;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.OrientationEventListener;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.WindowManager;

import java.io.IOException;

import io.straas.android.sdk.streaming.demo.R;

@SuppressLint("ViewConstructor")
final class CameraOverlayLayout extends OverlayLayout implements TextureView.SurfaceTextureListener {

    private static final String TAG = CameraOverlayLayout.class.getSimpleName();
    private static final int CAMERA_VIEW_SIZE_DP_AREA = 12000;

    private Camera mCamera;
    private TextureView mTextureView;
    private int mRotation;
    private OrientationEventListener mOrientationEventListener;
    private static final int EVENT_RESET_ORIENTATION = 0;

    private final Handler mMainThreadHandler = new Handler(Looper.getMainLooper()) {
        public void handleMessage(Message msg) {
            Log.d(TAG, "handleMessage: " + msg.what);
            switch(msg.what) {
                case EVENT_RESET_ORIENTATION:
                    resetOrientation(false);
                    break;
            }
        }
    };

    static CameraOverlayLayout create(Context context, Listener listener) {
        return new CameraOverlayLayout(context, listener);
    }

    private CameraOverlayLayout(Context context, Listener listener) {
        super(context, listener);
    }

    @Override
    public int getInflateResource() {
        return R.layout.camera_overlay_layout;
    }

    @SuppressLint("RtlHardcoded")
    public int getLayoutGravity() {
        return Gravity.BOTTOM | Gravity.LEFT;
    }

    public void initLayoutViews() {
        try {
            mCamera = Camera.open(Camera.CameraInfo.CAMERA_FACING_FRONT);
            double cameraViewPxArea = dpAreaToPxArea(CAMERA_VIEW_SIZE_DP_AREA, getDensity(getContext()));
            CameraHelper.setCameraPreviewSize(mCamera, (int) cameraViewPxArea);
        } catch (Exception e) {
            Log.e(TAG, "Open camera failed: " + e);
        }
        mTextureView = findViewById(R.id.camera_preview);
        resetOrientation(true);
        mTextureView.setSurfaceTextureListener(this);
        if (mTextureView.getSurfaceTexture() != null) {
            startPreview(mTextureView.getSurfaceTexture());
        }
    }

    @Override
    public View getMoveView() {
        return mTextureView;
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        startPreview(surface);
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
        // Ignored, Camera does all the work for us
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        if (mCamera != null) {
            mCamera.stopPreview();
            mCamera.release();
        }
        return true;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {
    }

    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        resetOrientation(false);
    }

    private void startPreview(SurfaceTexture surfaceTexture) {
        if (mCamera == null) {
            return;
        }
        try {
            mCamera.setPreviewTexture(surfaceTexture);
        } catch (IOException e) {
            Log.e(TAG, "setPreviewTexture failed: " + e);
        }
        mCamera.startPreview();
    }

    private void resetOrientation(boolean force) {
        WindowManager wm = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
        int rotation = wm.getDefaultDisplay().getRotation();
        if (!force && rotation == mRotation) {
            return;
        }
        mRotation = rotation;
        int displayOrientation = setCameraDisplayOrientation(rotation);
        resetTextureViewSize(displayOrientation);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        registerOrientationEventListener();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        unregisterOrientationEventListener();
    }

    private void registerOrientationEventListener() {
        if (mOrientationEventListener == null) {
            mOrientationEventListener = new OrientationEventListener(getContext(),
                    SensorManager.SENSOR_DELAY_NORMAL) {
                @Override
                public void onOrientationChanged(int orientation) {
                    mMainThreadHandler.removeMessages(EVENT_RESET_ORIENTATION);
                    mMainThreadHandler.sendEmptyMessage(EVENT_RESET_ORIENTATION);
                }
            };
        }
        mOrientationEventListener.enable();
    }

    private void unregisterOrientationEventListener() {
        if (mOrientationEventListener != null) {
            mOrientationEventListener.disable();
        }
    }

    private int setCameraDisplayOrientation(int rotation) {
        int degrees = 0;

        switch (rotation) {
            case Surface.ROTATION_0: degrees = 0; break;
            case Surface.ROTATION_90: degrees = 90; break;
            case Surface.ROTATION_180: degrees = 180; break;
            case Surface.ROTATION_270: degrees = 270; break;
        }

        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(Camera.CameraInfo.CAMERA_FACING_FRONT, info);
        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360;  // compensate the mirror
        } else {  // back-facing
            result = (info.orientation - degrees + 360) % 360;
        }
        if (mCamera != null) {
            mCamera.setDisplayOrientation(result);
        }
        return result;
    }

    private void resetTextureViewSize(int displayOrientation) {
        if (mCamera == null || mTextureView == null) {
            return;
        }
        Camera.Size size = mCamera.getParameters().getPreviewSize();
        double cameraViewSize = dpAreaToPxArea(CAMERA_VIEW_SIZE_DP_AREA, getDensity(getContext()));
        double previewSize = size.width * size.height;
        double shrinkRatio = Math.sqrt(cameraViewSize / previewSize);
        int width, height;
        if (displayOrientation == 0 || displayOrientation == 180) {
            width = (int) Math.round(size.width * shrinkRatio);
            height = (int) Math.round(size.height * shrinkRatio);
        } else {
            width = (int) Math.round(size.height * shrinkRatio);
            height = (int) Math.round(size.width * shrinkRatio);
        }
        mTextureView.setLayoutParams(new LayoutParams(width, height));
    }

    private static double dpAreaToPxArea(int dpArea, float density) {
        return dpArea * Math.pow(density, 2);
    }

    private static float getDensity(Context context){
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        return metrics.density;
    }
}
