package io.straas.android.media.demo;

import android.os.Bundle;
import android.support.annotation.IntDef;

import com.google.android.exoplayer.util.Util;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import io.straas.android.sdk.media.StraasMediaCore;

public class MediaContentTypeHelper {
    public static final int CONTENT_TYPE_DASH = Util.TYPE_DASH;
    public static final int CONTENT_TYPE_SMOOTH_STREAMING = Util.TYPE_SS;
    public static final int CONTENT_TYPE_HLS = Util.TYPE_HLS;
    public static final int CONTENT_TYPE_OTHER = Util.TYPE_OTHER;
    @Retention(RetentionPolicy.SOURCE)
    @IntDef({CONTENT_TYPE_DASH, CONTENT_TYPE_SMOOTH_STREAMING, CONTENT_TYPE_HLS, CONTENT_TYPE_OTHER})
    public @interface ContentType {}

    public static Bundle mediaContentType(@ContentType int type) {
        Bundle bundle = new Bundle();
        switch (type) {
            case CONTENT_TYPE_HLS:
            case CONTENT_TYPE_DASH:
            case CONTENT_TYPE_SMOOTH_STREAMING:
            case CONTENT_TYPE_OTHER:
                bundle.putInt(StraasMediaCore.EXTRA_CONTENT_TYPE, type);
                break;
            default:
                throw new IllegalArgumentException("Must be one of TYPE_HLS, TYPE_DASH, TYPE_SS, TYPE_OTHER");
        }
        return bundle;
    }
}
