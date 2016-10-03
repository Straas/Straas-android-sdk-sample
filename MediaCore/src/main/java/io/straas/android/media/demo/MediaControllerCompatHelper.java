package io.straas.android.media.demo;

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
    public static void playAndSeekFromMediaId(FragmentActivity activity, String mediaId, long positionMs) {
        if (activity == null || activity.getSupportMediaController() == null) {
            return;
        }
        playAndSeekFromMediaId(activity.getSupportMediaController(), mediaId, positionMs);
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
    public static void playAndSeekFromUri(FragmentActivity activity, Uri uri, long positionMs) {
        if (activity == null || activity.getSupportMediaController() == null) {
            return;
        }
        playAndSeekFromUri(activity.getSupportMediaController(), uri, positionMs);
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
    public static void playAndSeekFromUri(FragmentActivity activity, Uri uri, @ContentType int type,
                                          long positionMs) {
        if (activity == null || activity.getSupportMediaController() == null) {
            return;
        }
        playAndSeekFromUri(activity.getSupportMediaController(), uri, type, positionMs);
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
    public static void setVideoQualityIndex(FragmentActivity activity, int index) {
        if (activity == null || activity.getSupportMediaController() == null) {
            return;
        }
        setVideoQualityIndex(activity.getSupportMediaController(), index);
    }

    /**
     * Retrieve all video {@link Format} and current selected index from current media playback.
     * <p>
     * Later you could use {@link MediaControllerCompatHelper#setVideoQualityIndex(FragmentActivity, int)}
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
        if (activity == null || activity.getSupportMediaController() == null) {
            return;
        }

        activity.getSupportMediaController().sendCommand(StraasMediaCore.COMMAND_GET_VIDEO_FORMATS, null,
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
     * Later you could use {@link MediaControllerCompatHelper#setVideoQualityIndex(FragmentActivity, int)}
     * or {@link MediaControllerCompatHelper#setVideoQualityIndex(MediaControllerCompat, int)} to
     * change new index.
     */
    public static void getVideoQualityInfo(FragmentActivity activity, VideoQualityInfoCallback callback) {
        if (activity == null || activity.getSupportMediaController() == null || callback == null) {
            return;
        }
        getVideoQualityInfo(activity.getSupportMediaController(), callback);
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

}
