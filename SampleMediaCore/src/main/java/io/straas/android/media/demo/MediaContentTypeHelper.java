package io.straas.android.media.demo;

import android.os.Bundle;

import com.google.android.exoplayer2.C.ContentType;

import io.straas.android.sdk.media.StraasMediaCore;

import static com.google.android.exoplayer2.C.TYPE_DASH;
import static com.google.android.exoplayer2.C.TYPE_HLS;
import static com.google.android.exoplayer2.C.TYPE_OTHER;
import static com.google.android.exoplayer2.C.TYPE_SS;

public class MediaContentTypeHelper {

    public static Bundle mediaContentType(@ContentType int type) {
        Bundle bundle = new Bundle();
        switch (type) {
            case TYPE_HLS:
            case TYPE_DASH:
            case TYPE_SS:
            case TYPE_OTHER:
                bundle.putInt(StraasMediaCore.EXTRA_CONTENT_TYPE, type);
                break;
            default:
                throw new IllegalArgumentException("Must be one of TYPE_HLS, TYPE_DASH, TYPE_SS, TYPE_OTHER");
        }
        return bundle;
    }
}
