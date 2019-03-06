package io.straas.android.sdk.streaming.demo.filter;


import android.opengl.GLES20;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import io.straas.android.sdk.streaming.BaseImageFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageFilter;

public class GPUImageSupportFilter<T extends GPUImageFilter> extends BaseImageFilter {

    private static final int BYTES_PER_FLOAT = 4;

    private static final float SHAPE_COORDINATE[] = {
            -1.0f, -1.0f,
            1.0f, -1.0f,
            -1.0f, 1.0f,
            1.0f, 1.0f,
    };

    private static final float TEXTURE_COORDINATE[] = {
            0.0f, 0.0f,
            1.0f, 0.0f,
            0.0f, 1.0f,
            1.0f, 1.0f,
    };

    private T mInnerFilter;

    public GPUImageSupportFilter(T innerFilter) {
        mInnerFilter = innerFilter;
    }

    @Override
    public void onInit(int width, int height) {
        super.onInit(width, height);
        mInnerFilter.init();
        mInnerFilter.onOutputSizeChanged(width, height);
    }

    @Override
    public void onDraw(int textureName, int frameBufferName) {
        super.onDraw(textureName, frameBufferName);
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, frameBufferName);
        mInnerFilter.onDraw(textureName, getFloatBuffer(SHAPE_COORDINATE), getFloatBuffer(TEXTURE_COORDINATE));
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
    }

    private FloatBuffer getFloatBuffer(float[] origin) {
        FloatBuffer buffer = ByteBuffer.allocateDirect(origin.length * BYTES_PER_FLOAT)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        buffer.put(origin);
        buffer.position(0);
        return buffer;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mInnerFilter.destroy();
    }
}
