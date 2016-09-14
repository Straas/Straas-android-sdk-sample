package io.straas.android.media.demo;

import android.content.Context;
import android.support.v4.content.Loader;
import android.support.v4.media.MediaBrowserCompat.ConnectionCallback;

import io.straas.android.sdk.base.identity.Identity;
import io.straas.android.sdk.media.StraasMediaCore;

/**
 * A {@link Loader} which gives you a connected StraasMediaCore, and retaining the connection during a
 * configuration change
 */
public class StraasMediaCoreLoader extends Loader<StraasMediaCore> {
    private StraasMediaCore mStraasMediaCore;
    private Identity mIdentity;

    public StraasMediaCoreLoader(Context context, Identity identity) {
        super(context);
        mIdentity = identity;
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
            mStraasMediaCore = new StraasMediaCore(mIdentity, new ConnectionCallback() {
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
                    deliverResult(null);
                }
            });
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
