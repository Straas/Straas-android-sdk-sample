package io.straas.android.media.demo;

import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaBrowserCompat.ConnectionCallback;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.exoplayer.AspectRatioFrameLayout;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import io.straas.android.media.demo.widget.StraasPlayerView;
import io.straas.android.sdk.media.ImaHelper;
import io.straas.android.sdk.media.StraasMediaCore;
import io.straas.android.sdk.media.VideoCustomMetadata;
import io.straas.android.sdk.mediacore.demo.R;
import io.straas.sdk.demo.MemberIdentity;

/**
 * Demo for some of the operations to browse and play medias.
 */
public class OperationActivity extends AppCompatActivity {
    // change these three attributes to fit with your CMS.
    public static final String PLAYLIST_ID = "";
    public static final String VIDEO_ID = "";
    public static final String LIVE_VIDEO_ID = "";

    private static final String TAG = OperationActivity.class.getSimpleName();
    private StraasMediaCore mStraasMediaCore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_operation);
        AspectRatioFrameLayout mAspectRatioFrameLayout = (AspectRatioFrameLayout) findViewById(R.id.aspectRatioFrameLayout);
        mAspectRatioFrameLayout.setAspectRatio(1.778f);

        StraasPlayerView playerView = (StraasPlayerView) findViewById(R.id.straas);
        playerView.initialize(this);

        mStraasMediaCore = new StraasMediaCore(playerView, MemberIdentity.ME,
                new ConnectionCallback() {
                    @Override
                    public void onConnected() {
                        getSupportMediaController().registerCallback(mMediaControllerCallback);
                    }
                })
                // remove setImaHelper if you don't want to include ad system (IMA)
                .setImaHelper(ImaHelper.newInstance());
        getMediaBrowser().connect();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (getSupportMediaController() != null) {
            getSupportMediaController().getTransportControls().play();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (getSupportMediaController() != null) {
            if (isFinishing()) {
                getSupportMediaController().unregisterCallback(mMediaControllerCallback);
                getSupportMediaController().getTransportControls().stop();
            } else {
                getSupportMediaController().getTransportControls().pause();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        getMediaBrowser().disconnect();
    }

    private MediaBrowserCompat getMediaBrowser() {
        return mStraasMediaCore.getMediaBrowser();
    }

    public void playVodId(View view) {
        if (checkId(VIDEO_ID) || getSupportMediaController() == null) return;
        // play video id directly
        getSupportMediaController().getTransportControls().playFromMediaId(VIDEO_ID, null);
    }

    private boolean checkId(String id) {
        if (TextUtils.isEmpty(id)) {
            Toast.makeText(this, "ID is empty!", Toast.LENGTH_SHORT).show();
            return true;
        }
        return false;
    }

    public void playAndSeekVodId(View view) {
        if (checkId(VIDEO_ID)) return;
        // play video id directly with seek time(ms)
        MediaControllerCompatHelper.playAndSeekFromMediaId(this, VIDEO_ID, 30000);
    }

    public void playUrl(View view) {
        if (getSupportMediaController() == null) {
            return;
        }
        // play video url directly
        getSupportMediaController().getTransportControls().playFromUri(
                Uri.parse("http://rmcdn.2mdn.net/MotifFiles/html/1248596/android_1330378998288.mp4"), null);
    }

    public void playUrlWithoutExtension(View view) {
        // play video url without filename extension
        String dashStreamLink = "http://www.youtube.com/api/manifest/dash/id/bf5bb2419360daf1/source/youtube?"
                + "as=fmp4_audio_clear,fmp4_sd_hd_clear&sparams=ip,ipbits,expire,source,id,as&ip=0.0.0.0&"
                + "ipbits=0&expire=19000000000&signature=51AF5F39AB0CEC3E5497CD9C900EBFEAECCCB5C7."
                + "8506521BFC350652163895D4C26DEE124209AA9E&key=ik0";
        MediaControllerCompatHelper.playAndSeekFromUri(this, Uri.parse(dashStreamLink),
                MediaContentTypeHelper.CONTENT_TYPE_DASH, 0);
    }

    public void playVR360Url(View view) {
        if (getSupportMediaController() == null) {
            return;
        }
        // play 360 video
        Bundle bundle = new Bundle();
        bundle.putInt(StraasMediaCore.KEY_VIDEO_RENDER_TYPE, StraasMediaCore.VIDEO_RENDER_TYPE_360);

        getSupportMediaController().getTransportControls().playFromUri(
                Uri.parse("https://eu-storage-bitcodin.storage.googleapis.com/bitStorage/" +
                        "6_9420dbf3e029ff61639267a40b89436e/105560_716c9b2b8abe754541275a8d39d251a3/mpds/105560.mpd"), bundle);
    }

    public void playPlaylist(View view) {
        if (checkId(PLAYLIST_ID) || getSupportMediaController() == null) return;
        getMediaBrowser().subscribe(PLAYLIST_ID, new MediaBrowserCompat.SubscriptionCallback() {
            @Override
            public void onChildrenLoaded(@NonNull String parentId, List<MediaBrowserCompat.MediaItem> children) {
                getSupportMediaController().getTransportControls().playFromMediaId(children.get(0).getMediaId(), null);
            }
        });
    }

    public void playLiveStreaming(View view) {
        if (checkId(LIVE_VIDEO_ID) || getSupportMediaController() == null) return;
        // play live stream
        getSupportMediaController().getTransportControls()
                .playFromMediaId(StraasMediaCore.LIVE_ID_PREFIX + LIVE_VIDEO_ID, null);
    }

    public void queryMediaItemInfo(View view) {
        if (checkId(VIDEO_ID) || getSupportMediaController() == null) return;
        // query video info only
        getMediaBrowser().getItem(VIDEO_ID, new MediaBrowserCompat.ItemCallback() {
            @Override
            public void onError(@NonNull String itemId) {
                Log.e(TAG, itemId + " load error");
            }

            @Override
            public void onItemLoaded(MediaBrowserCompat.MediaItem item) {
                if (item == null) {
                    return;
                }
                if (item.isPlayable()) {
                    // display info to user
                    MediaDescriptionCompat mediaDescription = item.getDescription();
                    Log.d(TAG, "ID: " + mediaDescription.getMediaId() + ", Title: " + mediaDescription.getTitle() +
                    ", Description: " + mediaDescription.getDescription() + ", Thumbnail: " + mediaDescription.getIconUri() +
                    ", Views: " + mediaDescription.getExtras().getLong(VideoCustomMetadata.CUSTOM_METADATA_VIEWS_COUNT) +
                    ", Duration: " + mediaDescription.getExtras().getLong(MediaMetadataCompat.METADATA_KEY_DURATION));
                }
            }
        });
    }

    private final MediaControllerCompat.Callback mMediaControllerCallback = new MediaControllerCompat.Callback() {

        @Override
        public void onMetadataChanged(MediaMetadataCompat metadata) {
            Log.d(TAG, "ID: " + metadata.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID) +
            ", Title: " + metadata.getString(MediaMetadataCompat.METADATA_KEY_DISPLAY_TITLE) +
            ", Description: " + metadata.getString(MediaMetadataCompat.METADATA_KEY_DISPLAY_DESCRIPTION) +
            ", Thumbnail: " + metadata.getString(MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI) +
            ", Created at: " + metadata.getString(MediaMetadataCompat.METADATA_KEY_DATE) +
            ", Views: " + metadata.getBundle().getLong(VideoCustomMetadata.CUSTOM_METADATA_VIEWS_COUNT) +
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
            try {
                JSONObject jsonObject = new JSONObject(event);
                String eventType = jsonObject.getString(StraasMediaCore.EVENT_TYPE);
                switch (eventType) {
                    case StraasMediaCore.EVENT_PLAYER_ERROR_MESSAGE:
                        String error = jsonObject.getString(eventType);
                        Log.e(eventType, error);
                        break;
                    case StraasMediaCore.EVENT_MEDIA_BROWSER_SERVICE_ERROR:
                        String errorReason = jsonObject.getString(StraasMediaCore.KEY_MEDIA_BROWSER_ERROR_REASON);
                        String errorMessage = jsonObject.getString(StraasMediaCore.KEY_MEDIA_BROWSER_ERROR_MESSAGE);
                        Log.e(eventType, errorReason + ": " + errorMessage);
                        break;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };
}
