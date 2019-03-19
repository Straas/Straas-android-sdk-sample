package io.straas.android.media.demo;

import android.os.Bundle;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import io.straas.android.media.demo.widget.StraasPlayerView;
import io.straas.android.media.demo.widget.ui.AspectRatioFrameLayout;
import io.straas.android.sdk.demo.identity.MemberIdentity;
import io.straas.android.sdk.media.ImaHelper;
import io.straas.android.sdk.media.StraasMediaCore;

import static io.straas.android.media.demo.widget.StraasPlayerView.CUSTOM_COLUMN_BOTTOM_LEFT;
import static io.straas.android.media.demo.widget.StraasPlayerView.CUSTOM_COLUMN_BOTTOM_RIGHT1;
import static io.straas.android.media.demo.widget.StraasPlayerView.CUSTOM_COLUMN_BOTTOM_RIGHT2;
import static io.straas.android.media.demo.widget.StraasPlayerView.CUSTOM_COLUMN_TOP_RIGHT;


public class StraasPlayerViewCustomizationActivity extends AppCompatActivity {
    // change attribute to fit with your CMS.
    public static final String VIDEO_ID = "";

    private static final String TAG = StraasPlayerViewCustomizationActivity.class.getSimpleName();
    private StraasPlayerView mStraasPlayerView;
    private StraasMediaCore mStraasMediaCore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.straas_player_customization);

        AspectRatioFrameLayout aspectRatioFrameLayout = findViewById(R.id.straasPlayer);
        aspectRatioFrameLayout.setAspectRatio(1.778f);

        mStraasPlayerView = findViewById(R.id.straas);
        mStraasPlayerView.initialize(this);

        mStraasMediaCore = new StraasMediaCore(mStraasPlayerView, MemberIdentity.ME, mCallback)
                // remove setImaHelper if you don't want to include ad system (IMA)
                .setImaHelper(ImaHelper.newInstance());
        mStraasMediaCore.getMediaBrowser().connect();

    }

    @Override
    protected void onStop() {
        super.onStop();
        if (getMediaControllerCompat() != null) {
            getMediaControllerCompat().getTransportControls().pause();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (getMediaControllerCompat() != null) {
            getMediaControllerCompat().getTransportControls().stop();
            getMediaControllerCompat().unregisterCallback(mMediaControllerCallback);
        }
        mStraasMediaCore.getMediaBrowser().disconnect();
    }

    private MediaControllerCompat getMediaControllerCompat() {
        return MediaControllerCompat.getMediaController(this);
    }

    private MediaBrowserCompat.ConnectionCallback mCallback = new MediaBrowserCompat.ConnectionCallback() {
        @Override
        public void onConnected() {
            getMediaControllerCompat().registerCallback(mMediaControllerCallback);
            if (TextUtils.isEmpty(VIDEO_ID)) {
                Toast.makeText(StraasPlayerViewCustomizationActivity.this, "ID is empty!",
                        Toast.LENGTH_SHORT).show();
                return;
            }
            getMediaControllerCompat().getTransportControls().playFromMediaId(VIDEO_ID, null);
        }
    };

    private final MediaControllerCompat.Callback mMediaControllerCallback = new MediaControllerCompat.Callback() {

        @Override
        public void onMetadataChanged(MediaMetadataCompat metadata) {
            Log.d(TAG, "ID: " + metadata.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID) +
                    ", Title: " + metadata.getString(MediaMetadataCompat.METADATA_KEY_DISPLAY_TITLE) +
                    ", Description: " + metadata.getString(MediaMetadataCompat.METADATA_KEY_DISPLAY_DESCRIPTION) +
                    ", Thumbnail: " + metadata.getString(MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI) +
                    ", Created at: " + metadata.getString(MediaMetadataCompat.METADATA_KEY_DATE) +
                    ", Duration: " + metadata.getLong(MediaMetadataCompat.METADATA_KEY_DURATION));
        }

        @Override
        public void onPlaybackStateChanged(PlaybackStateCompat state) {
            if (TextUtils.isEmpty(state.getErrorMessage())) {
                Log.d(TAG, state.toString());
            } else {
                Log.e(TAG, state.toString());
            }
        }

        @Override
        public void onSessionEvent(String event, Bundle extras) {
            switch (event) {
                case StraasMediaCore.EVENT_MEDIA_BROWSER_SERVICE_ERROR:
                    String errorReason = extras.getString(StraasMediaCore.KEY_MEDIA_BROWSER_ERROR_REASON);
                    String errorMessage = extras.getString(StraasMediaCore.KEY_MEDIA_BROWSER_ERROR_MESSAGE);
                    Log.e(event, errorReason + ": " + errorMessage);
                    break;
            }
        }
    };

    public void onClickTopRight(View view) {
        mStraasPlayerView.setSwitchQualityViewPosition(CUSTOM_COLUMN_TOP_RIGHT);
    }

    public void onClickBottomLeft(View view) {
        mStraasPlayerView.setSwitchQualityViewPosition(CUSTOM_COLUMN_BOTTOM_LEFT);
    }

    public void onClickBottomRight1(View view) {
        mStraasPlayerView.setSwitchQualityViewPosition(CUSTOM_COLUMN_BOTTOM_RIGHT1);
    }

    public void onClickBottomRight2(View view) {
        mStraasPlayerView.setSwitchQualityViewPosition(CUSTOM_COLUMN_BOTTOM_RIGHT2);
    }

    public void onClickLogoTopRight(View view) {
        mStraasPlayerView.setLogo(R.mipmap.ic_launcher, CUSTOM_COLUMN_TOP_RIGHT);
    }

    public void onClickLogoBottomLeft(View view) {
        mStraasPlayerView.setLogo(R.mipmap.ic_launcher, CUSTOM_COLUMN_BOTTOM_LEFT);
    }

    public void onClickLogoBottomRight1(View view) {
        mStraasPlayerView.setLogo(R.mipmap.ic_launcher, CUSTOM_COLUMN_BOTTOM_RIGHT1);
    }

    public void onClickLogoBottomRight2(View view) {
        mStraasPlayerView.setLogo(R.mipmap.ic_launcher, CUSTOM_COLUMN_BOTTOM_RIGHT2);
    }

    public void onClickRemoveCustomTopRight(View view) {
        mStraasPlayerView.removeViewFromCustomColumn(CUSTOM_COLUMN_TOP_RIGHT);
    }

    public void onClickRemoveCustomBottomLeft1(View view) {
        mStraasPlayerView.removeViewFromCustomColumn(CUSTOM_COLUMN_BOTTOM_LEFT);
    }

    public void onClickRemoveCustomBottomRight1(View view) {
        mStraasPlayerView.removeViewFromCustomColumn(CUSTOM_COLUMN_BOTTOM_RIGHT1);
    }

    public void onClickRemoveCustomBottomRight2(View view) {
        mStraasPlayerView.removeViewFromCustomColumn(CUSTOM_COLUMN_BOTTOM_RIGHT2);
    }

    public void onClickRemoveAllCustomColumn(View view) {
        mStraasPlayerView.removeViewAllCustomColumn();
    }

    public void onClickSetIconOnTopRight(View view) {
        View switchQualityView = View.inflate(this, R.layout.switch_quality_layout, null);
        mStraasPlayerView.setCustomViewToColumn(switchQualityView, CUSTOM_COLUMN_TOP_RIGHT);
    }

    public void onClickSetIconOnBottomLeft(View view) {
        View switchQualityView = View.inflate(this, R.layout.switch_quality_layout, null);
        mStraasPlayerView.setCustomViewToColumn(switchQualityView, CUSTOM_COLUMN_BOTTOM_LEFT);
    }

    public void onClickSetIconOnBottomRight1(View view) {
        View switchQualityView = View.inflate(this, R.layout.switch_quality_layout, null);
        mStraasPlayerView.setCustomViewToColumn(switchQualityView, CUSTOM_COLUMN_BOTTOM_RIGHT1);
    }

    public void onClickSetIconOnBottomRight2(View view) {
        View switchQualityView = View.inflate(this, R.layout.switch_quality_layout, null);
        mStraasPlayerView.setCustomViewToColumn(switchQualityView, CUSTOM_COLUMN_BOTTOM_RIGHT2);
    }

}
