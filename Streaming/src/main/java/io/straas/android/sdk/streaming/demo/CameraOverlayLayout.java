package io.straas.android.sdk.streaming.demo;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.view.Gravity;
import android.view.TextureView;
import android.view.View;
import android.view.WindowManager;

import io.straas.android.sdk.demo.R;

import java.io.IOException;

final class CameraOverlayLayout extends OverlayLayout implements TextureView.SurfaceTextureListener {

    private Camera mCamera;
    private TextureView mTextureView;

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
        mTextureView = (TextureView) findViewById(R.id.camera_preview);;
        mTextureView.setSurfaceTextureListener(this);
    }

    @Override
    public View getMoveView() {
        return mTextureView;
    }

    @Override
    public void onClick(final View v) {
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        mCamera = Camera.open(Camera.CameraInfo.CAMERA_FACING_FRONT);
        if (mCamera == null) {
            throw new RuntimeException("Default camera not available");
        }

        try {
            mCamera.setPreviewTexture(surface);
            setCameraDisplayOrientation();
            mCamera.startPreview();
        } catch (IOException ioe) {
        }
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
        // Ignored, Camera does all the work for us
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        mCamera.stopPreview();
        mCamera.release();
        return true;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {
    }

    public void onConfigurationChanged() {
        setCameraDisplayOrientation();
    }

    private void setCameraDisplayOrientation() {
         if (getContext().getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            mCamera.setDisplayOrientation(90);
         } else {
            mCamera.setDisplayOrientation(0);
         }
    }

    public boolean isGravityTop() {
        return false;
    }

}
