package io.straas.android.sdk.streaming.demo.filter;

import android.opengl.GLES11Ext;
import android.opengl.GLES20;

import io.straas.android.sdk.streaming.BaseImageFilter;

public class GrayImageFilter extends BaseImageFilter {

    private static final int BYTES_PER_FLOAT = 4;
    private static final int STRIDE_BYTES = COORDS_PER_VERTEX * BYTES_PER_FLOAT;

    private final String VERTEX_SHADER_CODE =
            "attribute vec4 vPosition;" +
            "attribute vec2 aTexPosition;" +
            "varying vec2 vTexPosition;" +
            "void main() {" +
            "  gl_Position = vPosition;" +
            "  vTexPosition = aTexPosition;" +
            "}";

    private final String FRAGMENT_SHADER_CODE =
            "precision mediump float;" +
            "uniform sampler2D uTexture;" +
            "varying vec2 vTexPosition;" +
            "void main() {" +
            "  vec4  color = texture2D(uTexture, vTexPosition);\n" +
            "  const vec3 monoMultiplier = vec3(0.299, 0.587, 0.114);" +
            "  float monoColor = dot(color.rgb, monoMultiplier);" +
            "  gl_FragColor = vec4(monoColor, monoColor, monoColor, 1.0);" +
            "}";

    private int mGlProgram;
    private int mTextureHandle;
    private int mPositionHandle;
    private int mTexturePositionHandle;

    @Override
    public void onInit(int width, int height) {
        super.onInit(width, height);
        mGlProgram = createProgram(VERTEX_SHADER_CODE, FRAGMENT_SHADER_CODE);
        GLES20.glUseProgram(mGlProgram);
        mPositionHandle = GLES20.glGetAttribLocation(mGlProgram, "vPosition");
        mTextureHandle = GLES20.glGetUniformLocation(mGlProgram, "uTexture");
        mTexturePositionHandle = GLES20.glGetAttribLocation(mGlProgram, "aTexPosition");
    }

    @Override
    public void onDraw(int textureName, int frameBufferName) {
        super.onDraw(textureName, frameBufferName);
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, frameBufferName);
        GLES20.glUseProgram(mGlProgram);
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureName);

        GLES20.glUniform1i(mTextureHandle, 0);

        mShapeCoordinate.position(0);
        GLES20.glEnableVertexAttribArray(mPositionHandle);
        GLES20.glVertexAttribPointer(mPositionHandle, COORDS_PER_VERTEX,
                GLES20.GL_FLOAT, false,
                STRIDE_BYTES, mShapeCoordinate);

        mTextureCoordinate.position(0);
        GLES20.glEnableVertexAttribArray(mTexturePositionHandle);
        GLES20.glVertexAttribPointer(mTexturePositionHandle, COORDS_PER_VERTEX,
                GLES20.GL_FLOAT, false,
                STRIDE_BYTES, mTextureCoordinate);

        GLES20.glViewport(0, 0, mWidth, mHeight);
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, mIndicesOrder.limit(), GLES20.GL_UNSIGNED_SHORT, mIndicesOrder);
        GLES20.glFinish();

        GLES20.glDisableVertexAttribArray(mPositionHandle);
        GLES20.glDisableVertexAttribArray(mTexturePositionHandle);
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, 0);
        GLES20.glUseProgram(0);
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        GLES20.glDeleteProgram(mGlProgram);
    }

    private int createProgram(String vertexShaderCode, String fragmentShaderCode) {
        int vertexShader = GLES20.glCreateShader(GLES20.GL_VERTEX_SHADER);
        GLES20.glShaderSource(vertexShader, vertexShaderCode);
        GLES20.glCompileShader(vertexShader);

        int fragmentShader = GLES20.glCreateShader(GLES20.GL_FRAGMENT_SHADER);
        GLES20.glShaderSource(fragmentShader, fragmentShaderCode);
        GLES20.glCompileShader(fragmentShader);

        int program = GLES20.glCreateProgram();
        GLES20.glAttachShader(program, vertexShader);
        GLES20.glAttachShader(program, fragmentShader);
        GLES20.glLinkProgram(program);
        return program;
    }

}
