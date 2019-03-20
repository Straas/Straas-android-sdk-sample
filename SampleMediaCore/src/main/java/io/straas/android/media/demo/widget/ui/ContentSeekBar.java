package io.straas.android.media.demo.widget.ui;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.ikala.android.utils.iKalaUtils;

import java.lang.ref.WeakReference;
import java.util.Formatter;
import java.util.Locale;

import io.straas.android.media.demo.R;
import io.straas.android.media.demo.widget.PlayerControl;
import io.straas.android.media.demo.widget.StraasPlayerView.PlaybackMode;

import static io.straas.android.media.demo.widget.StraasPlayerView.PLAYBACK_MODE_LIVE_DVR;
import static io.straas.android.media.demo.widget.StraasPlayerView.PLAYBACK_MODE_LIVE_EDGE;
import static io.straas.android.media.demo.widget.StraasPlayerView.PLAYBACK_MODE_UNKNOWN;
import static io.straas.android.media.demo.widget.StraasPlayerView.PLAYBACK_MODE_VOD;

public class ContentSeekBar extends RelativeLayout {

    private PlayerControl mPlayer;

    private ProgressBar mProgress;
    private TextView mEndTime, mCurrentTime;
    private boolean mDragging;
    private static final int CHECK_PLAYER_POS_DURATION = 1000;
    private static final int SHOW_PROGRESS = 2;
    private static final int CHECK_PLAYER_POSITION = 3;

    private StringBuilder mFormatBuilder;
    private Formatter mFormatter;

    private Handler mHandler = new MessageHandler(this);

    private TrackingListener mTrackingListener;
    private LiveDvrPositionTimeStringListener mLiveDvrPositionTimeStringListener;

    @PlaybackMode private int mPlaybackMode = PLAYBACK_MODE_UNKNOWN;

    public interface TrackingListener {
        void onTrackingTouch(boolean isTracking);
    }

    public interface LiveDvrPositionTimeStringListener {
        void onLiveDvrPositionTimeStringChanged(String timeString);
    }

    public ContentSeekBar(Context context) {
        this(context, null);
    }

    public ContentSeekBar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ContentSeekBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        View v = LayoutInflater.from(getContext()).inflate(R.layout.media_controller, null);
        mEndTime = iKalaUtils.getView(v, R.id.time_duration);
        mCurrentTime = iKalaUtils.getView(v, R.id.time_current);
        mFormatBuilder = new StringBuilder();
        mFormatter = new Formatter(mFormatBuilder, Locale.getDefault());
        mProgress = iKalaUtils.getView(v, R.id.media_controller_progress);

        if (mProgress != null) {
            if (mProgress instanceof SeekBar) {
                SeekBar seeker = (SeekBar) mProgress;
                seeker.setOnSeekBarChangeListener(mSeekListener);
            }
        }

        LayoutParams layoutParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        addView(v, layoutParams);
    }

    public void setMediaPlayer(PlayerControl player) {
        mPlayer = player;

        if (mPlayer == null) {
            mHandler.removeMessages(CHECK_PLAYER_POSITION);
        }

        checkPlayerPosition();
    }

    private String stringForTime(int timeMs) {
        int totalSeconds = (int) ((float)timeMs / 1000 + 0.5f);

        int seconds = totalSeconds % 60;
        int minutes = (totalSeconds / 60) % 60;
        int hours = totalSeconds / 3600;

        mFormatBuilder.setLength(0);
        if (hours > 0) {
            return mFormatter.format("%d:%02d:%02d", hours, minutes, seconds).toString();
        } else {
            return mFormatter.format("%02d:%02d", minutes, seconds).toString();
        }
    }

    private int setProgress() {
        if (mPlayer == null || mDragging) {
            return 0;
        }

        int position = mPlayer.getCurrentPosition();
        int duration = mPlayer.getDuration();
        if (mProgress != null) {
            if (mPlaybackMode == PLAYBACK_MODE_LIVE_EDGE) {
                mProgress.setProgress(mProgress.getMax());
            } else if (duration > 0) {
                // use long to avoid overflow
                long pos = 1000L * position / duration;
                mProgress.setProgress((int) pos);
            }
            int percent = mPlayer.getBufferPercentage();
            mProgress.setSecondaryProgress(percent * 10);
        }

        if (mEndTime != null)
            mEndTime.setText(stringForTime(duration));
        if (mCurrentTime != null)
            mCurrentTime.setText(stringForTime(position));

        return position;
    }

    public void setPlaybackMode(@PlaybackMode int PlaybackMode) {
        mPlaybackMode = PlaybackMode;
        switch (mPlaybackMode) {
            case PLAYBACK_MODE_LIVE_EDGE:
                if (mProgress != null) {
                    mProgress.setProgress(mProgress.getMax());
                }
            case PLAYBACK_MODE_LIVE_DVR:
            case PLAYBACK_MODE_UNKNOWN:
                if (mCurrentTime != null) {
                    mCurrentTime.setVisibility(View.GONE);
                }
                if (mEndTime != null) {
                    mEndTime.setVisibility(View.GONE);
                }
                break;
            case PLAYBACK_MODE_VOD:
            default:
                if (mCurrentTime != null) {
                    mCurrentTime.setVisibility(View.VISIBLE);
                }
                if (mEndTime != null) {
                    mEndTime.setVisibility(View.VISIBLE);
                }
                break;
        }
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (mPlayer == null) {
            return true;
        }

        int keyCode = event.getKeyCode();
        final boolean uniqueDown = event.getRepeatCount() == 0
                && event.getAction() == KeyEvent.ACTION_DOWN;
        if (keyCode == KeyEvent.KEYCODE_HEADSETHOOK
                || keyCode == KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE
                || keyCode == KeyEvent.KEYCODE_SPACE) {
            if (uniqueDown) {
            }
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_MEDIA_PLAY) {
            if (uniqueDown && !mPlayer.isPlaying()) {
                mPlayer.start();
            }
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_MEDIA_STOP
                || keyCode == KeyEvent.KEYCODE_MEDIA_PAUSE) {
            if (uniqueDown && mPlayer.isPlaying()) {
                mPlayer.pause();
            }
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN
                || keyCode == KeyEvent.KEYCODE_VOLUME_UP
                || keyCode == KeyEvent.KEYCODE_VOLUME_MUTE) {
            // don't show the controls for volume adjustment
            return super.dispatchKeyEvent(event);
        } else if (keyCode == KeyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_MENU) {
            return true;
        }

        return super.dispatchKeyEvent(event);
    }

    // There are two scenarios that can trigger the seekbar listener to trigger:
    //
    // The first is the user using the touchpad to adjust the position of the
    // seekbar's thumb. In this case onStartTrackingTouch is called followed by
    // a number of onProgressChanged notifications, concluded by onStopTrackingTouch.
    // We're setting the field "mDragging" to true for the duration of the dragging
    // session to avoid jumps in the position in case of ongoing playback.
    //
    // The second scenario involves the user operating the scroll ball, in this
    // case there WON'T BE onStartTrackingTouch/onStopTrackingTouch notifications,
    // we will simply apply the updated position without suspending regular updates.
    private SeekBar.OnSeekBarChangeListener mSeekListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onStartTrackingTouch(SeekBar bar) {
            mDragging = true;

            sendTrackingEvent();
        }

        @Override
        public void onProgressChanged(SeekBar bar, int progress, boolean fromUser) {
            if (mPlayer == null) {
                return;
            }

            if (!fromUser) {
                // We're not interested in programmatically generated changes to
                // the progress bar's position.
                return;
            }

            long duration = mPlayer.getDuration();
            long newPosition = (duration * progress) / bar.getMax();
            if (mCurrentTime != null)
                mCurrentTime.setText(stringForTime((int) newPosition));

            if (mPlaybackMode == PLAYBACK_MODE_LIVE_EDGE || mPlaybackMode == PLAYBACK_MODE_LIVE_DVR) {
                sendLiveDvrPositionTimeStringChangedEvent(getLiveDvrPositionTimeString((int) Math.max(duration - newPosition, 0L)));
            }
        }

        private String getLiveDvrPositionTimeString(int offset) {
            String offsetTimeString = stringForTime(Math.max(offset, 0));
            if (offset <= 0) {
                return offsetTimeString;
            }

            return String.format("-%s", offsetTimeString);
        }

        @Override
        public void onStopTrackingTouch(SeekBar bar) {
            mDragging = false;
            sendTrackingEvent();

            if (mPlayer == null) {
                return;
            }
            long newPosition = ((long) mPlayer.getDuration() * bar.getProgress()) / bar.getMax();
            mPlayer.seekTo((int) newPosition);
        }
    };

    public void checkPlayerPosition() {
        if (mPlayer == null) {
            return;
        }

        setProgress();
        mHandler.removeMessages(CHECK_PLAYER_POSITION);
        mHandler.sendEmptyMessageDelayed(CHECK_PLAYER_POSITION,
                mPlayer.isPlaying() ? CHECK_PLAYER_POS_DURATION : CHECK_PLAYER_POS_DURATION * 3);
    }

    public void destroy() {
        if (mPlayer != null) {
            mPlayer.release();
            mPlayer = null;
        }
        cleanMessage();
    }

    private void cleanMessage() {
        mHandler.removeMessages(SHOW_PROGRESS);
        mHandler.removeMessages(CHECK_PLAYER_POSITION);
    }

    private static class MessageHandler extends Handler {
        private final WeakReference<ContentSeekBar> mView;

        MessageHandler(ContentSeekBar view) {
            mView = new WeakReference<>(view);
        }

        @Override
        public void handleMessage(Message msg) {
            ContentSeekBar view = mView.get();
            if (view == null || view.mPlayer == null) {
                return;
            }

            switch (msg.what) {
                case CHECK_PLAYER_POSITION:
                    view.checkPlayerPosition();
                    break;
            }
        }
    }

    public void setOnTrackingListener(TrackingListener listener) {
        mTrackingListener = listener;
    }

    private void sendTrackingEvent() {
        if (mTrackingListener != null) {
            mTrackingListener.onTrackingTouch(mDragging);
        }
    }

    public void setLiveDvrPositionTimeStringListener(LiveDvrPositionTimeStringListener listener) {
        mLiveDvrPositionTimeStringListener = listener;
    }

    private void sendLiveDvrPositionTimeStringChangedEvent(String timeString) {
        if (mLiveDvrPositionTimeStringListener != null) {
            mLiveDvrPositionTimeStringListener.onLiveDvrPositionTimeStringChanged(timeString);
        }
    }
}
