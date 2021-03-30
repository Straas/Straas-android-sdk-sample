package io.straas.android.media.demo;

import android.content.*;
import android.support.v4.media.MediaBrowserCompat.*;
import android.widget.*;

import androidx.loader.content.Loader;
import io.straas.android.sdk.authentication.identity.*;
import io.straas.android.sdk.media.*;

/**
 * A {@link Loader} which gives you a connected StraasMediaCore, and retaining the connection during a
 * configuration change
 */
public class StraasMediaCoreLoader extends Loader<StraasMediaCore> {
    private StraasMediaCore mStraasMediaCore;
    private Identity mIdentity;
    private String mRestHost;

    public StraasMediaCoreLoader(Context context, Identity identity) {
        this(context, identity, null);
    }

    public StraasMediaCoreLoader(Context context, Identity identity, String restHost) {
        super(context);
        mIdentity = identity;
        mRestHost = restHost;
    }

    @Override
    protected void onStartLoading() {
        if (mStraasMediaCore != null) {
            if (!mStraasMediaCore.getMediaBrowser().isConnected()) {
                deliverResult(null);
                mStraasMediaCore.getMediaBrowser().connect();
            } else {
                deliverResult(mStraasMediaCore);
            }
        } else {
            StraasMediaCore.MediaCoreConfig config = new StraasMediaCore.MediaCoreConfig.Builder()
                    .setIdentity(mIdentity)
                    .setConnectionCallback(new ConnectionCallback() {
                        @Override
                        public void onConnected() {
                            deliverResult(mStraasMediaCore);
                        }

                        @Override
                        public void onConnectionSuspended() {
                            deliverResult(null);
                        }

                        @Override
                        public void onConnectionFailed() {
                            Toast.makeText(getContext(),
                                    "Connection fails, this may be caused by validation failure",
                                    Toast.LENGTH_LONG)
                                    .show();
                            deliverResult(null);
                        }
                    })
                    .setRestHost(mRestHost)
                    .build();
            mStraasMediaCore = new StraasMediaCore(config);
            deliverResult(null);
            mStraasMediaCore.getMediaBrowser().connect();
        }
    }

    @Override
    protected void onReset() {
        if (mStraasMediaCore.getMediaController() != null) {
            mStraasMediaCore.getMediaController().getTransportControls().stop();
        }
        mStraasMediaCore.getMediaBrowser().disconnect();
    }
}
