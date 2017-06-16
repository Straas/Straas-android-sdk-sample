package io.straas.android.sdk.streaming.demo;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.util.Log;
import android.view.Gravity;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.WindowManager;

import io.straas.android.sdk.demo.R;

final class CameraOverlayLayout extends OverlayLayout implements TextureView.SurfaceTextureListener {

    private static final String TAG = CameraOverlayLayout.class.getSimpleName();

    private Camera mCamera;
    private TextureView mTextureView;
    private int mDisplayOrientation = -1;

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

    @Override
    public int getLayoutWidth(Context context) {
        return context.getResources().getDimensionPixelSize(R.dimen.camera_preview_width);
    }

    public int getLayoutGravity() {
        return Gravity.BOTTOM | Gravity.LEFT;
    }

    public void initLayoutViews() {
        mTextureView = (TextureView) findViewById(R.id.camera_preview);
        mTextureView.setSurfaceTextureListener(this);
    }

    @Override
    public View getMoveView() {
        return mTextureView;
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        try {
            mCamera = Camera.open(Camera.CameraInfo.CAMERA_FACING_FRONT);
            mCamera.setPreviewTexture(surface);
            setCameraDisplayOrientation();
            mCamera.startPreview();
        } catch (Exception e) {
            Log.e(TAG, "Open camera failed: " + e);
        }
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

    public void onOrientationChanged() {
        setCameraDisplayOrientation();
    }

    private void setCameraDisplayOrientation() {
        WindowManager wm = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
        int rotation = wm.getDefaultDisplay().getRotation();
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
        if (mCamera != null && mDisplayOrientation != result) {
            mCamera.setDisplayOrientation(result);
            mDisplayOrientation = result;
        }
    }

}
