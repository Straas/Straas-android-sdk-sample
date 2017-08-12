package io.straas.android.media.demo.widget;

import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.widget.MediaController;

import io.straas.android.sdk.media.StraasMediaCore;


/**
 * An implementation of {@link MediaController.MediaPlayerControl} for controlling an {@link MediaControllerCompat} instance.
 * <p>
 * This class is provided for convenience, however it is expected that most applications will
 * implement their own player controls and therefore not require this class.
 * You could use {@link FragmentActivity#setSupportMediaController(MediaControllerCompat)} for later retrieval via
 * {@link FragmentActivity#getSupportMediaController()}
 * <pre>
 *     MediaControllerCompat mediaController = new MediaControllerCompat(this, token);
 *     setSupportMediaController(mediaController);
 *     getSupportMediaController().getTransportControls().playFromMediaId("blablabla", null);
 * </pre>
 *
 * @see {@link FragmentActivity#setSupportMediaController(MediaControllerCompat)}
 * @see {@link FragmentActivity#getSupportMediaController()}
 */
public class PlayerControl implements MediaController.MediaPlayerControl {
    private final MediaControllerCompat mMediaControllerCompat;
    private PlaybackStateCompat mLastPlaybackState;
    private MediaMetadataCompat mLastMediaMetadataCompat;
    private final MediaControllerCompat.Callback mCallback;

    public PlayerControl(@NonNull MediaControllerCompat mediaControllerCompat, final MediaController mediaController) {
        mMediaControllerCompat = mediaControllerCompat;
        mCallback = new MediaControllerCompat.Callback() {
            @Override
            public void onPlaybackStateChanged(PlaybackStateCompat state) {
                processPlaybackState(state, mediaController);
            }

            @Override
            public void onMetadataChanged(MediaMetadataCompat metadata) {
                mLastMediaMetadataCompat = metadata;
            }
        };
        mMediaControllerCompat.registerCallback(mCallback);
        mLastMediaMetadataCompat = mMediaControllerCompat.getMetadata();
        processPlaybackState(mMediaControllerCompat.getPlaybackState(), mediaController);
    }

    private void processPlaybackState(PlaybackStateCompat state, MediaController mediaController) {
        if (state == null || state.getActiveQueueItemId() == StraasMediaCore.AD_PLAYBACK_ID) {
            return;
        }
        mLastPlaybackState = state;
        if (mediaController != null && mediaController.isShowing()) {
            mediaController.show();
        }
    }

    public PlayerControl(@NonNull FragmentActivity fragmentActivity, MediaController mediaController) {
        this(MediaControllerCompat.getMediaController(fragmentActivity), mediaController);
    }

    public void release() {
        mMediaControllerCompat.unregisterCallback(mCallback);
    }

    @Override
    public void start() {
        mMediaControllerCompat.getTransportControls().play();
    }

    @Override
    public void pause() {
        mMediaControllerCompat.getTransportControls().pause();
    }

    @Override
    public int getDuration() {
        if (mLastMediaMetadataCompat == null) {
            return 0;
        }
        return (int) mLastMediaMetadataCompat.getLong(MediaMetadataCompat.METADATA_KEY_DURATION);
    }

    @Override
    public int getCurrentPosition() {
        if (mLastPlaybackState == null) {
            return 0;
        }
        long currentPosition = mLastPlaybackState.getPosition();
        if (mLastPlaybackState.getState() == PlaybackStateCompat.STATE_PLAYING) {
            // Calculate the elapsed time between the last position update and now and unless
            // paused, we can assume (delta * speed) + current position is approximately the
            // latest position. This ensure that we do not repeatedly call the getPlaybackState()
            // on MediaControllerCompat.
            long timeDelta = SystemClock.elapsedRealtime() -
                    mLastPlaybackState.getLastPositionUpdateTime();
            currentPosition += (int) timeDelta * mLastPlaybackState.getPlaybackSpeed();
        }
        return (int) currentPosition;
    }

    @Override
    public void seekTo(int pos) {
        mMediaControllerCompat.getTransportControls().seekTo(pos);
    }

    @Override
    public boolean isPlaying() {
        return mLastPlaybackState != null && mLastPlaybackState.getState() == PlaybackStateCompat.STATE_PLAYING;
    }

    @Override
    public int getBufferPercentage() {
        if (mLastPlaybackState == null) {
            return 0;
        }
        long bufferedPosition = mLastPlaybackState.getBufferedPosition();
        long duration = getDuration();
        return (int) (duration == 0 ? 100 : (bufferedPosition * 100) / duration);
    }

    @Override
    public boolean canPause() {
        return true;
    }

    @Override
    public boolean canSeekBackward() {
        return mLastPlaybackState == null || (mLastPlaybackState.getActions() & PlaybackStateCompat.ACTION_SEEK_TO) != 0;
    }

    @Override
    public boolean canSeekForward() {
        return mLastPlaybackState == null || (mLastPlaybackState.getActions() & PlaybackStateCompat.ACTION_SEEK_TO) != 0;
    }

    @Override
    public int getAudioSessionId() {
        throw new UnsupportedOperationException();
    }

}
