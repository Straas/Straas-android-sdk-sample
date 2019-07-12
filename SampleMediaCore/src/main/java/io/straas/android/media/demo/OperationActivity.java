package io.straas.android.media.demo;

import android.app.NotificationChannel;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaBrowserCompat.ConnectionCallback;
import android.support.v4.media.MediaBrowserCompat.MediaItem;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Checkable;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.exoplayer2.C;

import java.util.List;

import io.straas.android.media.demo.widget.StraasPlayerView;
import io.straas.android.media.demo.widget.ui.AspectRatioFrameLayout;
import io.straas.android.sdk.demo.identity.MemberIdentity;
import io.straas.android.sdk.media.ImaHelper;
import io.straas.android.sdk.media.StraasMediaCore;
import io.straas.android.sdk.media.VideoCustomMetadata;
import io.straas.android.sdk.media.notification.NotificationOptions;
import pub.devrel.easypermissions.EasyPermissions;

import static android.app.NotificationManager.IMPORTANCE_LOW;

/**
 * Demo for some of the operations to browse and play medias.
 */
public class OperationActivity extends AppCompatActivity {

    public static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    private static final String SHARE_PREFERENCE_KEY = "StraaS";
    private static final String FOREGROUND_KEY = "foreground";

    // change these three attributes to fit with your CMS.
    private String PLAYLIST_ID = "";
    private String VIDEO_ID = "";
    private String LIVE_VIDEO_ID = "";

    private static final String TAG = OperationActivity.class.getSimpleName();
    private StraasMediaCore mStraasMediaCore;
    private Checkable mLowLatency, mDisableAudioSwitch;
    private boolean mIsForeground;
    private LocationCollector mLocationCollector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_operation);
        AspectRatioFrameLayout mAspectRatioFrameLayout = findViewById(R.id.aspectRatioFrameLayout);
        mAspectRatioFrameLayout.setAspectRatio(1.778f);

        StraasPlayerView playerView = findViewById(R.id.straas);
        playerView.initialize(this);

        prepareEditText();
        mLowLatency = findViewById(R.id.low_latency);
        mDisableAudioSwitch = findViewById(R.id.disableAudio);

        mStraasMediaCore = new StraasMediaCore(playerView, MemberIdentity.ME,
                new ConnectionCallback() {
                    @Override
                    public void onConnected() {
                        getMediaControllerCompat().registerCallback(mMediaControllerCallback);
                        if (mIsForeground != getMediaControllerCompat().getExtras().getBoolean(
                                VideoCustomMetadata.SERVICE_FOREGROUND_IS_ENABLED, !mIsForeground)) {
                            setForeground(mIsForeground);
                        }

                        mLocationCollector = new LocationCollector(OperationActivity.this,
                                getMediaControllerCompat());
                        if (mLocationCollector.checkPermission()) {
                            mLocationCollector.start();
                        } else {
                            EasyPermissions.requestPermissions(OperationActivity.this,
                                    getString(R.string.rationale_request_location),
                                    LocationCollector.REQUEST_CODE, LocationCollector.PERMISSIONS);
                        }
                    }
                })
                // remove setImaHelper if you don't want to include ad system (IMA)
                .setImaHelper(ImaHelper.newInstance());
        getMediaBrowser().connect();

        mIsForeground = getSharedPreferences(SHARE_PREFERENCE_KEY, Context.MODE_PRIVATE)
                .getBoolean(FOREGROUND_KEY, false);
        ((Checkable) findViewById(R.id.switch_foreground)).setChecked(mIsForeground);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LocationCollector.REQUEST_CODE) {
            EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, mLocationCollector);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mLocationCollector != null) {
            mLocationCollector.stop();
        }
        getMediaBrowser().disconnect();
    }

    @Override
    protected void onStart() {
        super.onStart();
        StraasPlayerView playerView = findViewById(R.id.straas);
        playerView.hideControllerViews();

        if (getMediaControllerCompat() != null) {
            getMediaControllerCompat().getTransportControls().play();
        }
    }

    private MediaControllerCompat getMediaControllerCompat() {
        return MediaControllerCompat.getMediaController(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (getMediaControllerCompat() != null && !mIsForeground) {
            if (isFinishing()) {
                getMediaControllerCompat().unregisterCallback(mMediaControllerCallback);
                getMediaControllerCompat().getTransportControls().stop();
            } else {
                getMediaControllerCompat().getTransportControls().pause();
            }
        }
    }

    private MediaBrowserCompat getMediaBrowser() {
        return mStraasMediaCore.getMediaBrowser();
    }

    public void playVodId(View view) {
        if (checkId(VIDEO_ID) || getMediaControllerCompat() == null) return;
        // play video id directly
        getMediaControllerCompat().getTransportControls().playFromMediaId(VIDEO_ID, null);
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
        if (getMediaControllerCompat() == null) {
            return;
        }
        // play video url directly
        getMediaControllerCompat().getTransportControls().playFromUri(
                Uri.parse("http://rmcdn.2mdn.net/MotifFiles/html/1248596/android_1330378998288.mp4"), null);
    }

    public void playUrlWithoutExtension(View view) {
        // play video url without filename extension
        String dashStreamLink = "http://www.youtube.com/api/manifest/dash/id/bf5bb2419360daf1/source/youtube?"
                + "as=fmp4_audio_clear,fmp4_sd_hd_clear&sparams=ip,ipbits,expire,source,id,as&ip=0.0.0.0&"
                + "ipbits=0&expire=19000000000&signature=51AF5F39AB0CEC3E5497CD9C900EBFEAECCCB5C7."
                + "8506521BFC350652163895D4C26DEE124209AA9E&key=ik0";
        MediaControllerCompatHelper.playAndSeekFromUri(this, Uri.parse(dashStreamLink),
                C.TYPE_DASH, 0);
    }

    public void playVR360Url(View view) {
        if (getMediaControllerCompat() == null) {
            return;
        }
        // play 360 video
        Bundle bundle = new Bundle();
        bundle.putInt(StraasMediaCore.KEY_VIDEO_RENDER_TYPE, StraasMediaCore.VIDEO_RENDER_TYPE_360);

        getMediaControllerCompat().getTransportControls().playFromUri(
                Uri.parse("https://bitmovin-a.akamaihd.net/content/playhouse-vr/mpds/105560.mpd"), bundle);
    }

    public void playPlaylist(View view) {
        if (checkId(PLAYLIST_ID) || getMediaControllerCompat() == null) return;
        getMediaBrowser().subscribe(PLAYLIST_ID, new MediaBrowserCompat.SubscriptionCallback() {
            @Override
            public void onChildrenLoaded(@NonNull String parentId, List<MediaItem> children) {
                getMediaControllerCompat().getTransportControls().playFromMediaId(children.get(0).getMediaId(), null);
            }
        });
    }

    public void playLiveStreaming(View view) {
        if (checkId(LIVE_VIDEO_ID) || getMediaControllerCompat() == null) return;
        // play live stream
        getMediaBrowser().getItem(StraasMediaCore.LIVE_ID_PREFIX + LIVE_VIDEO_ID, new MediaBrowserCompat.ItemCallback() {
            @Override
            public void onItemLoaded(MediaItem item) {
                if (item == null) {
                    return;
                }
                getMediaControllerCompat().getTransportControls()
                        .playFromMediaId(StraasMediaCore.LIVE_ID_PREFIX + LIVE_VIDEO_ID, getLiveStreamingExtras());
                if (item.isBrowsable()) {
                    // live event is ended, print VODs
                    getMediaBrowser().subscribe(StraasMediaCore.LIVE_ID_PREFIX + LIVE_VIDEO_ID, new MediaBrowserCompat.SubscriptionCallback() {
                        @Override
                        public void onChildrenLoaded(@NonNull String parentId, List<MediaItem> children) {
                            if (children == null) {
                                return;
                            }
                            for (MediaItem mediaItem : children) {
                                MediaDescriptionCompat mediaDescription = mediaItem.getDescription();
                                Log.d(TAG, "ID: " + mediaDescription.getMediaId() +
                                        ", Title: " + mediaDescription.getTitle() +
                                        ", Description: " + mediaDescription.getDescription() +
                                        ", Thumbnail: " + mediaDescription.getIconUri() +
                                        ", Views: " + mediaDescription.getExtras().getLong(VideoCustomMetadata.PLAY_COUNT_SUM) +
                                        ", Duration: " + mediaDescription.getExtras().getLong(MediaMetadataCompat.METADATA_KEY_DURATION));
                            }
                        }
                    });
                }
            }

            private Bundle getLiveStreamingExtras() {
                if (mLowLatency != null && mLowLatency.isChecked()) {
                    Bundle bundle = new Bundle();
                    bundle.putBoolean(StraasMediaCore.PLAY_OPTION_LIVE_LOW_LATENCY, true);
                    return bundle;
                }

                return null;
            }
        });

    }

    public void queryMediaItemInfo(View view) {
        if (checkId(VIDEO_ID) || getMediaControllerCompat() == null) return;
        // query video info only
        getMediaBrowser().getItem(VIDEO_ID, new MediaBrowserCompat.ItemCallback() {
            @Override
            public void onError(@NonNull String itemId) {
                Log.e(TAG, itemId + " load error");
            }

            @Override
            public void onItemLoaded(MediaItem item) {
                if (item == null) {
                    return;
                }
                if (item.isPlayable()) {
                    // display info to user
                    MediaDescriptionCompat mediaDescription = item.getDescription();
                    Log.d(TAG, "ID: " + mediaDescription.getMediaId() + ", Title: " + mediaDescription.getTitle() +
                            ", Description: " + mediaDescription.getDescription() + ", Thumbnail: " + mediaDescription.getIconUri() +
                            ", Views: " + mediaDescription.getExtras().getLong(VideoCustomMetadata.PLAY_COUNT_SUM) +
                            ", Duration: " + mediaDescription.getExtras().getLong(MediaMetadataCompat.METADATA_KEY_DURATION));
                } else if (item.isBrowsable()) {
                    getMediaBrowser().subscribe(VIDEO_ID, new MediaBrowserCompat.SubscriptionCallback() {
                        @Override
                        public void onChildrenLoaded(@NonNull String parentId, List<MediaItem> children) {
                            if (children == null) {
                                return;
                            }
                            for (MediaItem mediaItem : children) {
                                MediaDescriptionCompat mediaDescription = mediaItem.getDescription();
                                Log.d(TAG, "ID: " + mediaDescription.getMediaId() +
                                        ", Title: " + mediaDescription.getTitle() +
                                        ", Description: " + mediaDescription.getDescription() +
                                        ", Thumbnail: " + mediaDescription.getIconUri() +
                                        ", Views: " + mediaDescription.getExtras().getLong(VideoCustomMetadata.PLAY_COUNT_SUM) +
                                        ", Duration: " + mediaDescription.getExtras().getLong(MediaMetadataCompat.METADATA_KEY_DURATION));
                            }
                        }
                    });
                }
            }
        });
    }

    public void crop(View view) {
        mStraasMediaCore.setPlaneProjectionMode(StraasMediaCore.PLANE_PROJECTION_MODE_CROP);
    }

    public void fit(View view) {
        mStraasMediaCore.setPlaneProjectionMode(StraasMediaCore.PLANE_PROJECTION_MODE_FIT);
    }

    public void full(View view) {
        mStraasMediaCore.setPlaneProjectionMode(StraasMediaCore.PLANE_PROJECTION_MODE_FULL);
    }

    public void normal(View view) {
        mStraasMediaCore.setDisplayMode(StraasMediaCore.DISPLAY_MODE_NORMAL);
    }

    public void cardboard(View view) {
        mStraasMediaCore.setDisplayMode(StraasMediaCore.DISPLAY_MODE_CARDBOARD);
    }

    public void switchForeground(View toggleButton) {
        mIsForeground = ((Switch)toggleButton).isChecked();
        getSharedPreferences(SHARE_PREFERENCE_KEY, Context.MODE_PRIVATE)
                .edit().putBoolean(FOREGROUND_KEY, mIsForeground).apply();
        setForeground(mIsForeground);
    }

    private void setForeground(boolean foreground) {
        if (getMediaControllerCompat() == null) {
            return;
        }
        if (foreground) {
            NotificationOptions.Builder builder = new NotificationOptions.Builder()
                    .setTargetClassName(OperationActivity.class.getName());
            if (supportNotificationChannel()) {
                builder.setChannel(createNotificationChannel());
            }
            MediaControllerCompatHelper.startForeground(getMediaControllerCompat(),
                    builder.build());
        } else {
            MediaControllerCompatHelper.stopForeground(getMediaControllerCompat());
        }
    }

    private boolean supportNotificationChannel() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.O;
    }

    private NotificationChannel createNotificationChannel() {
        if (!supportNotificationChannel()) {
            return null;
        }
        NotificationChannel channel = new NotificationChannel(getString(R.string.notification_channel_id),
                getString(R.string.notification_channel_name), IMPORTANCE_LOW);
        channel.setDescription(getString(R.string.notification_channel_description));
        return channel;
    }

    public void setAudibility(View toggleButton) {
        boolean disable = ((Switch)toggleButton).isChecked();
        MediaControllerCompatHelper.setAudibility(getMediaControllerCompat(), disable);
    }

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
            } else if (state.getExtras() != null){
                Log.e(TAG, state.toString() + " " + state.getExtras().getString(StraasMediaCore.EVENT_PLAYER_ERROR_MESSAGE, ""));
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
                case VideoCustomMetadata.LIVE_STATISTICS_CCU:
                    // you could also pull the value from getMediaControllerCompat().getExtras().getInt(LIVE_EXTRA_STATISTICS_CCU);
                    Log.d(TAG, "ccu: " + extras.getInt(event));
                    break;
                case VideoCustomMetadata.LIVE_STATISTICS_HIT_COUNT:
                    // you could also pull the value from getMediaControllerCompat().getExtras().getInt(LIVE_EXTRA_STATISTICS_HIT_COUNT);
                    Log.d(TAG, "hit count: " + extras.getInt(event));
                    break;
            }
        }

        @Override
        public void onExtrasChanged(Bundle extras) {
            mDisableAudioSwitch.setChecked(extras.getBoolean(VideoCustomMetadata.IS_AUDIO_DISABLED));
        }
    };

    private void prepareEditText() {
        TextView vod = findViewById(R.id.vod);
        TextView live = findViewById(R.id.live);
        TextView playlist = findViewById(R.id.playlist);
        if (!TextUtils.isEmpty(VIDEO_ID)) {
            vod.setText(VIDEO_ID);
        }
        if (!TextUtils.isEmpty(LIVE_VIDEO_ID)) {
            live.setText(LIVE_VIDEO_ID);
        }
        if (!TextUtils.isEmpty(PLAYLIST_ID)) {
            playlist.setText(PLAYLIST_ID);
        }
        vod.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                VIDEO_ID = s.toString();
            }
        });

        live.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                LIVE_VIDEO_ID = s.toString();
            }
        });

        playlist.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                PLAYLIST_ID = s.toString();
            }
        });
    }
}
