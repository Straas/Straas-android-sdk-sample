package io.straas.android.sdk.streaming.demo.filter.beauty;

import android.content.*;
import android.opengl.*;

import java.nio.*;

import io.straas.android.sdk.streaming.*;
import io.straas.android.sdk.streaming.demo.filter.beauty.internal.*;

import static io.straas.android.sdk.streaming.demo.filter.beauty.internal.OpenGLUtils.*;
import static io.straas.android.sdk.streaming.demo.filter.beauty.internal.TextureRotationUtils.*;

/**
 * 实时美颜，这里用的是高反差保留磨皮法
 */
public class BeautyFilter extends BaseImageFilter {

    // 美肤滤镜
    private GLImageBeautyComplexionFilter mComplexionFilter;
    // 高斯模糊
    private GLImageBeautyBlurFilter mBeautyBlurFilter;
    // 高通滤波
    private GLImageBeautyHighPassFilter mHighPassFilter;
    // 高通滤波做高斯模糊处理，保留边沿细节
    private GLImageGaussianBlurFilter mHighPassBlurFilter;
    // 磨皮程度调节滤镜
    private GLImageBeautyAdjustFilter mBeautyAdjustFilter;

    private final Context mContext;
    private float mBrightness;
    private float mSmoothness;
    private final FloatBuffer mBeautyVertexBuffer;
    private final FloatBuffer mBeautyTextureBuffer;

    // 缩放
    private float mBlurScale = 0.5f;

    public BeautyFilter(Context context) {
        mContext = context;
        mBeautyVertexBuffer = createFloatBuffer(CubeVertices);
        mBeautyTextureBuffer = createFloatBuffer(TextureVertices);
        mSmoothness = 0.5f;
        mBrightness = 0.5f;
    }

    //============================================================================
    // BaseImageFilter
    //============================================================================
    @Override
    public void onInit(int width, int height) {
        super.onInit(width, height);
        initFilters();
        setSmoothnessLevel(mSmoothness);
        setBrightnessLevel(mBrightness);
        onInputSizeChanged(width, height);
        onDisplaySizeChanged(width, height);
        initFrameBuffer(width, height);
    }

    private void initFilters() {
        mComplexionFilter = new GLImageBeautyComplexionFilter(mContext);
        mBeautyBlurFilter = new GLImageBeautyBlurFilter(mContext);
        mHighPassFilter = new GLImageBeautyHighPassFilter(mContext);
        mHighPassBlurFilter = new GLImageGaussianBlurFilter(mContext);
        mBeautyAdjustFilter = new GLImageBeautyAdjustFilter(mContext);
    }

    @Override
    public void onDraw(int imageTexture, int targetFrameBuffer) {
        super.onDraw(imageTexture, targetFrameBuffer);
        int currentTexture = imageTexture;
        int sourceTexture = mComplexionFilter.drawFrameBuffer(currentTexture, mBeautyVertexBuffer, mBeautyTextureBuffer);
        currentTexture = sourceTexture;

        int blurTexture = currentTexture;
        int highPassBlurTexture = currentTexture;
        // 高斯模糊
        if (mBeautyBlurFilter != null) {
            blurTexture = mBeautyBlurFilter.drawFrameBuffer(currentTexture, mBeautyVertexBuffer, mBeautyTextureBuffer);
            currentTexture = blurTexture;
        }
        // 高通滤波，做高反差保留
        if (mHighPassFilter != null) {
            mHighPassFilter.setBlurTexture(currentTexture);
            currentTexture = mHighPassFilter.drawFrameBuffer(sourceTexture, mBeautyVertexBuffer, mBeautyTextureBuffer);
        }
        // 对高反差保留的结果进行高斯模糊，过滤边沿数值
        if (mHighPassBlurFilter != null) {
            highPassBlurTexture = mHighPassBlurFilter.drawFrameBuffer(currentTexture, mBeautyVertexBuffer, mBeautyTextureBuffer);
        }
        // 混合处理
        if (mBeautyAdjustFilter != null) {
            currentTexture = sourceTexture;
            mBeautyAdjustFilter.setBlurTexture(blurTexture, highPassBlurTexture);
            GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, targetFrameBuffer);
            mBeautyAdjustFilter.drawFrame(currentTexture, mBeautyVertexBuffer, mBeautyTextureBuffer);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        destroyFrameBuffer();
        release();
    }

    //============================================================================
    // GLImageFilter
    //============================================================================
    private void onInputSizeChanged(int width, int height) {
        if (mComplexionFilter != null) {
            mComplexionFilter.onInputSizeChanged(width, height);
        }
        if (mBeautyBlurFilter != null) {
            mBeautyBlurFilter.onInputSizeChanged((int) (width * mBlurScale), (int) (height * mBlurScale));
        }
        if (mHighPassFilter != null) {
            mHighPassFilter.onInputSizeChanged((int) (width * mBlurScale), (int) (height * mBlurScale));
        }
        if (mHighPassBlurFilter != null) {
            mHighPassBlurFilter.onInputSizeChanged((int) (width * mBlurScale), (int) (height * mBlurScale));
        }
        if (mBeautyAdjustFilter != null) {
            mBeautyAdjustFilter.onInputSizeChanged(width, height);
        }
    }

    private void onDisplaySizeChanged(int width, int height) {
        if (mComplexionFilter != null) {
            mComplexionFilter.onDisplaySizeChanged(width, height);
        }
        if (mBeautyBlurFilter != null) {
            mBeautyBlurFilter.onDisplaySizeChanged(width, height);
        }
        if (mHighPassFilter != null) {
            mHighPassFilter.onDisplaySizeChanged(width, height);
        }
        if (mHighPassBlurFilter != null) {
            mHighPassBlurFilter.onDisplaySizeChanged(width, height);
        }
        if (mBeautyAdjustFilter != null) {
            mBeautyAdjustFilter.onDisplaySizeChanged(width, height);
        }
    }

    private void initFrameBuffer(int width, int height) {
        if (mComplexionFilter != null) {
            mComplexionFilter.initFrameBuffer(width, height);
        }
        if (mBeautyBlurFilter != null) {
            mBeautyBlurFilter.initFrameBuffer((int) (width * mBlurScale), (int) (height * mBlurScale));
        }
        if (mHighPassFilter != null) {
            mHighPassFilter.initFrameBuffer((int) (width * mBlurScale), (int) (height * mBlurScale));
        }
        if (mHighPassBlurFilter != null) {
            mHighPassBlurFilter.initFrameBuffer((int) (width * mBlurScale), (int) (height * mBlurScale));
        }
        if (mBeautyAdjustFilter != null) {
            mBeautyAdjustFilter.initFrameBuffer(width, height);
        }
    }

    private void destroyFrameBuffer() {
        if (mComplexionFilter != null) {
            mComplexionFilter.destroyFrameBuffer();
        }
        if (mBeautyBlurFilter != null) {
            mBeautyBlurFilter.destroyFrameBuffer();
        }
        if (mHighPassFilter != null) {
            mHighPassFilter.destroyFrameBuffer();
        }
        if (mHighPassBlurFilter != null) {
            mHighPassBlurFilter.destroyFrameBuffer();
        }
        if (mBeautyAdjustFilter != null) {
            mBeautyAdjustFilter.destroyFrameBuffer();
        }
    }

    private void release() {
        if (mComplexionFilter != null) {
            mComplexionFilter.release();
            mComplexionFilter = null;
        }
        if (mBeautyBlurFilter != null) {
            mBeautyBlurFilter.release();
            mBeautyBlurFilter = null;
        }
        if (mHighPassFilter != null) {
            mHighPassFilter.release();
            mHighPassFilter = null;
        }
        if (mHighPassBlurFilter != null) {
            mHighPassBlurFilter.release();
            mHighPassBlurFilter = null;
        }
        if (mBeautyAdjustFilter != null) {
            mBeautyAdjustFilter.release();
            mBeautyAdjustFilter = null;
        }
    }

    //============================================================================
    // functions
    //============================================================================
    /**
     * Sets the brightness level.
     *
     * @param brightness The brightness level of image. The value range is between 0.0f to 1.0f.
     * @see #getBrightnessLevel()
     */
    public void setBrightnessLevel(float brightness) {
        mBrightness = brightness;
        mComplexionFilter.setComplexionLevel(brightness);
    }

    /**
     * Sets the smoothness level.
     *
     * @param smoothness The smoothness level of image. The value range is between 0.0f to 1.0f.
     * @see #getSmoothnessLevel()
     */
    public void setSmoothnessLevel(float smoothness) {
        mSmoothness = smoothness;
        mBeautyAdjustFilter.setSkinBeautyIntensity(smoothness);
    }

    /**
     * Gets the smoothness level.
     *
     * @return the smoothness level of image
     * @see #setSmoothnessLevel(float)
     */
    public float getSmoothnessLevel() {
        return mSmoothness;
    }

    /**
     * Gets the brightness level.
     *
     * @return the brightness level of image
     * @see #setBrightnessLevel(float)
     */
    public float getBrightnessLevel() {
        return mBrightness;
    }
}
