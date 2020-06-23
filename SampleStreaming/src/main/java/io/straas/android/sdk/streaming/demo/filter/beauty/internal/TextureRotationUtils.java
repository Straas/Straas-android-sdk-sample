package io.straas.android.sdk.streaming.demo.filter.beauty.internal;

public class TextureRotationUtils {

    public static final int CoordsPerVertex = 2;

    public static final float CubeVertices[] = {
            -1.0f, -1.0f,  // 0 bottom left
            1.0f,  -1.0f,  // 1 bottom right
            -1.0f,  1.0f,  // 2 top left
            1.0f,   1.0f,  // 3 top right
    };

    public static final float TextureVertices[] = {
            0.0f, 0.0f,     // 0 left bottom
            1.0f, 0.0f,     // 1 right bottom
            0.0f, 1.0f,     // 2 left top
            1.0f, 1.0f      // 3 right top
    };
}
