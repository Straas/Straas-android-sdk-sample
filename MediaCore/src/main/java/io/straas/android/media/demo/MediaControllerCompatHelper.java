package io.straas.android.media.demo;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.support.v4.media.session.MediaControllerCompat;

import com.google.android.exoplayer2.C.ContentType;
import com.google.android.exoplayer2.Format;

import java.util.ArrayList;

import io.straas.android.media.demo.widget.ui.SwitchQualityDialog;
import io.straas.android.sdk.media.StraasMediaCore;
import io.straas.android.sdk.media.notification.NotificationOptions;

/**
 * This class wrap some {@link MediaControllerCompat} functionality which needs {@link Bundle}
 */
public class MediaControllerCompatHelper {


    /**
     * Seeks to a position specified in milliseconds and play.
     *
     * @param mediaId The id of the requested media.
     * @param positionMs The seek position.
     */
    public static void playAndSeekFromMediaId(@NonNull MediaControllerCompat controller, String mediaId, long positionMs) {
        Bundle bundle = new Bundle();
        bundle.putLong(StraasMediaCore.PLAY_OPTION_SEEK_TIME, positionMs);
        controller.getTransportControls().playFromMediaId(mediaId, bundle);
    }

    /**
     * Seeks to a position specified in milliseconds and play.
     *
     * @param mediaId The id of the requested media.
     * @param positionMs The seek position.
     */
    public static void playAndSeekFromMediaId(Activity activity, String mediaId, long positionMs) {
        if (activity == null || MediaControllerCompat.getMediaController(activity) == null) {
            return;
        }
        playAndSeekFromMediaId(MediaControllerCompat.getMediaController(activity), mediaId, positionMs);
    }

    /**
     * Seeks to a position specified in milliseconds and play.
     *
     * @param uri The link of the requested media, which needs filename extension explicitly.
     * @param positionMs The seek position.
     */
    public static void playAndSeekFromUri(@NonNull MediaControllerCompat controller, @NonNull Uri uri, long positionMs) {
        Bundle bundle = new Bundle();
        if (positionMs > 0) {
            bundle.putLong(StraasMediaCore.PLAY_OPTION_SEEK_TIME, positionMs);
        }
        controller.getTransportControls().playFromUri(uri, bundle);
    }

    /**
     * Seeks to a position specified in milliseconds and play.
     *
     * @param uri The link of the requested media, which needs filename extension explicitly.
     * @param positionMs The seek position.
     */
    public static void playAndSeekFromUri(Activity activity, Uri uri, long positionMs) {
        if (activity == null || MediaControllerCompat.getMediaController(activity) == null) {
            return;
        }
        playAndSeekFromUri(MediaControllerCompat.getMediaController(activity), uri, positionMs);
    }

    /**
     * Seeks to a position specified in milliseconds and play.
     *
     * @param uri The link of the requested media.
     * @param positionMs The seek position.
     */
    public static void playAndSeekFromUri(@NonNull MediaControllerCompat controller, @NonNull Uri uri,
                                          @ContentType int type, long positionMs) {
        Bundle bundle = MediaContentTypeHelper.mediaContentType(type);
        if (positionMs > 0) {
            bundle.putLong(StraasMediaCore.PLAY_OPTION_SEEK_TIME, positionMs);
        }
        controller.getTransportControls().playFromUri(uri, bundle);
    }

    /**
     * Seeks to a position specified in milliseconds and play.
     *
     * @param uri The link of the requested media.
     * @param positionMs The seek position.
     */
    public static void playAndSeekFromUri(Activity activity, Uri uri, @ContentType int type,
                                          long positionMs) {
        if (activity == null || MediaControllerCompat.getMediaController(activity) == null) {
            return;
        }
        playAndSeekFromUri(MediaControllerCompat.getMediaController(activity), uri, type, positionMs);
    }

    /**
     * Set new video quality index.
     * @param index the index from {@link VideoQualityInfo#mFormats}
     */
    public static void setVideoQualityIndex(@NonNull MediaControllerCompat controller, int index) {
        controller.getTransportControls().sendCustomAction(
                StraasMediaCore.COMMAND_SET_FORMAT_INDEX, Utils.setNewFormatIndex(index));
    }

    /**
     * Set new video quality index.
     * @param index the index from {@link VideoQualityInfo#mFormats}
     */
    public static void setVideoQualityIndex(Activity activity, int index) {
        if (activity == null || MediaControllerCompat.getMediaController(activity) == null) {
            return;
        }
        setVideoQualityIndex(MediaControllerCompat.getMediaController(activity), index);
    }

    /**
     * Retrieve all video {@link Format} and current selected index from current media playback.
     * <p>
     * Later you could use {@link MediaControllerCompatHelper#setVideoQualityIndex(Activity, int)}
     * or {@link MediaControllerCompatHelper#setVideoQualityIndex(MediaControllerCompat, int)} to
     * change new index.
     */
    public static void getVideoQualityInfo(@NonNull MediaControllerCompat controller,
                                           @NonNull final VideoQualityInfoCallback callback) {
        controller.sendCommand(StraasMediaCore.COMMAND_GET_VIDEO_FORMATS, null,
                new ResultReceiver(new Handler()) {
                    @Override
                    protected void onReceiveResult(int resultCode, Bundle resultData) {
                        resultData.setClassLoader(Format.class.getClassLoader());
                        if (resultData.containsKey(StraasMediaCore.KEY_CURRENT_VIDEO_FORMAT_INDEX)) {
                            ArrayList<Format> formats = resultData.getParcelableArrayList(StraasMediaCore.KEY_ALL_VIDEO_FORMATS);
                            int selectedIndex = resultData.getInt(StraasMediaCore.KEY_CURRENT_VIDEO_FORMAT_INDEX);
                            callback.onGetVideoQualityInfo(new VideoQualityInfo(formats, selectedIndex));
                        }
                    }
                });
    }

    public static void showVideoQualityListDialog(final FragmentActivity activity) {
        if (activity == null || MediaControllerCompat.getMediaController(activity) == null) {
            return;
        }

        MediaControllerCompat.getMediaController(activity).sendCommand(StraasMediaCore.COMMAND_GET_VIDEO_FORMATS, null,
                new ResultReceiver(new Handler()) {
                    @Override
                    protected void onReceiveResult(int resultCode, Bundle resultData) {
                        SwitchQualityDialog dialog = SwitchQualityDialog.newInstance(resultData);
                        dialog.show(activity.getSupportFragmentManager(), SwitchQualityDialog.class.getSimpleName());
                    }
                });
    }

    /**
     * Retrieve all video {@link Format} and current selected index from current media playback.
     * <p>
     * Later you could use {@link MediaControllerCompatHelper#setVideoQualityIndex(Activity, int)}
     * or {@link MediaControllerCompatHelper#setVideoQualityIndex(MediaControllerCompat, int)} to
     * change new index.
     */
    public static void getVideoQualityInfo(Activity activity, VideoQualityInfoCallback callback) {
        if (activity == null || MediaControllerCompat.getMediaController(activity) == null || callback == null) {
            return;
        }
        getVideoQualityInfo(MediaControllerCompat.getMediaController(activity), callback);
    }

    /**
     * Retrieve current speed.
     *
     */
    public static void getPlayerCurrentSpeed(@NonNull MediaControllerCompat controller,
                                           @NonNull final PlayerSpeedCallback callback) {
        controller.sendCommand(StraasMediaCore.COMMAND_GET_PLAYBACK_SPEED, null,
                new ResultReceiver(new Handler()) {
                    @Override
                    protected void onReceiveResult(int resultCode, Bundle resultData) {
                        resultData.setClassLoader(Format.class.getClassLoader());
                        if (resultData.containsKey(StraasMediaCore.KEY_PLAYBACK_SPEED)) {
                            callback.onGetPlayerSpeed(resultData.getFloat(StraasMediaCore.KEY_PLAYBACK_SPEED));
                        }
                    }
                });
    }

    public static void startForeground(@NonNull MediaControllerCompat controller,
                                       NotificationOptions options) {
        Bundle bundle = new Bundle();
        bundle.putParcelable(StraasMediaCore.KEY_NOTIFICATION_OPTIONS, options);
        controller.getTransportControls().sendCustomAction(StraasMediaCore.COMMAND_FOREGROUND, bundle);
    }

    public static void stopForeground(@NonNull MediaControllerCompat controller) {
        controller.getTransportControls().sendCustomAction(StraasMediaCore.COMMAND_STOP_FOREGROUND, null);
    }

    public static void setAudibility(@NonNull MediaControllerCompat controller, boolean disable) {
        Bundle bundle = new Bundle();
        bundle.putBoolean(StraasMediaCore.KEY_DISABLE_AUDIO, disable);
        controller.getTransportControls().sendCustomAction(StraasMediaCore.COMMAND_DISABLE_AUDIO, bundle);
    }

    public static void playAtLiveEdge(@NonNull MediaControllerCompat controller) {
        controller.getTransportControls().sendCustomAction(StraasMediaCore.COMMAND_PLAY_AT_LIVE_EDGE, null);
    }

    public static void setPlaybackSpeed(@NonNull MediaControllerCompat controller, float speed) {
        Bundle bundle = new Bundle();
        bundle.putFloat(StraasMediaCore.KEY_PLAYBACK_SPEED, speed);
        controller.getTransportControls().sendCustomAction(StraasMediaCore.COMMAND_SET_PLAYBACK_SPEED, bundle);
    }

    public static class VideoQualityInfo {
        public ArrayList<Format> mFormats;
        public int mCurrentSelectedIndex;

        public VideoQualityInfo(ArrayList<Format> formats, int currentSelectedIndex) {
            mFormats = formats;
            mCurrentSelectedIndex = currentSelectedIndex;
        }
    }

    public interface VideoQualityInfoCallback {
        void onGetVideoQualityInfo(VideoQualityInfo info);
    }

    public interface PlayerSpeedCallback {
        void onGetPlayerSpeed(float speed);
    }

}
