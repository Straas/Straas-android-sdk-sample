package io.straas.android.media.demo.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.DrawableRes;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StyleRes;
import android.support.graphics.drawable.VectorDrawableCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v4.media.MediaBrowserCompat.ConnectionCallback;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat.QueueItem;
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v4.util.SparseArrayCompat;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v4.widget.TextViewCompat;
import android.support.v7.widget.AppCompatImageButton;
import android.support.v7.widget.AppCompatImageView;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.exoplayer2.Format;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.List;

import io.straas.android.media.demo.MediaControllerCompatHelper;
import io.straas.android.media.demo.MediaControllerCompatHelper.VideoQualityInfo;
import io.straas.android.media.demo.MediaControllerCompatHelper.VideoQualityInfoCallback;
import io.straas.android.media.demo.Utils;
import io.straas.android.media.demo.widget.ui.ContentSeekBar;
import io.straas.android.media.demo.widget.ui.SwitchQualityDialog;
import io.straas.android.sdk.demo.R;
import io.straas.android.sdk.media.StraasMediaCore;
import io.straas.android.sdk.media.StraasMediaCore.ErrorReason;
import io.straas.android.sdk.media.VideoCustomMetadata;

public final class StraasPlayerView extends FrameLayout implements StraasMediaCore.UiContainer {
    private static final String TAG = StraasPlayerView.class.getSimpleName();

    private static final int AUTO_HIDE_DELAY_MILLIS = 3000;

    private boolean mEnableDefaultWidget;
    private boolean mEnableDefaultSwitchQualityIcon;
    private boolean mEnableDefaultSwitchQualityDialog;
    private boolean mEnableDefaultChannelName;
    private boolean mEnableDefaultSummaryViewer;
    private boolean mEnableDefaultLoadingProgressBar;
    private boolean mEnableDefaultContentSeekBar;
    private boolean mEnableDefaultPlay;
    private boolean mEnableDefaultPause;
    private boolean mEnableDefaultReplay;
    private boolean mEnableDefaultPrevious;
    private boolean mEnableDefaultNext;
    private boolean mEnableDefaultErrorMessage;
    private boolean mEnableDefaultBroadcastStateMessage;

    private boolean mIsBind;
    private boolean mIsLive = false;
    private boolean mCanToggleControllerUi = false;

    private View mControllerContainer;
    private View mColumnPlayPause;

    private ViewGroup mColumnSummaryViewer;
    private ViewGroup mColumnChannelName;
    private ViewGroup mColumnContentSeekBar;
    private ViewGroup mColumnLoadingBar;
    private ViewGroup mColumnPlay;
    private ViewGroup mColumnPause;
    private ViewGroup mColumnReplay;
    private ViewGroup mColumnPrevious;
    private ViewGroup mColumnNext;
    private ViewGroup mColumnAdPlay;
    private ViewGroup mColumnErrorMessage;
    private ViewGroup mColumnBroadcastState;

    private ViewGroup mColumnTopRight;
    private ViewGroup mColumnBottomLeft;
    private ViewGroup mColumnBottomRight1;
    private ViewGroup mColumnBottomRight2;

    private TextView mChannelNameTextView;
    private TextView mSummaryViewerTextView;
    private TextView mErrorMessageTextView;
    private View mBroadcastStateView;
    private View mSwitchQualityView;
    private View mLogoView;
    private ContentSeekBar mContentSeekBar;

    private FrameLayout mVideoView;
    private FrameLayout mAdView;

    private FragmentActivity mFragmentActivity;

    private Bundle mLiveBundle;
    private MediaMetadataCompat mLastMediaMetadata;
    private PlaybackStateCompat mLastPlaybackStateCompat;
    private StraasMediaCore mStraasMediaCore;
    private int mImageButtonBackground;
    private Context mThemeContext;
    private GestureDetectorCompat mGestureDetector, mGestureTapDetector, mGestureFakeDetector;
    private SwitchQualityViewClickListener mSwitchQualityViewListener;
    private ChannelNameMetadataListener mChannelNameMetadataListener = new ChannelNameMetadataListener();
    private SummaryViewerMetadataListener mSummaryViewerMetadataListener = new SummaryViewerMetadataListener();
    private ErrorMessageListener mErrorMessageListener = new ErrorMessageListener();
    private BroadcastStateListener mBroadcastStateListener = new BroadcastStateListener();

    private List<ConnectionCallback> mMediaConnectedListenerList = new ArrayList<>();
    private SparseArrayCompat<ViewGroup> mCustomColumnList = new SparseArrayCompat<>();
    private List<QueueItem> mLastQueueList;
    private String mLiveState = null;

    public interface SwitchQualityViewClickListener {
        void onFormatCallback(ArrayList<Format> formats, int currentIndex);
    }

    public static final int CUSTOM_COLUMN_TOP_RIGHT = 0;
    public static final int CUSTOM_COLUMN_BOTTOM_LEFT = 1;
    public static final int CUSTOM_COLUMN_BOTTOM_RIGHT1 = 2;
    public static final int CUSTOM_COLUMN_BOTTOM_RIGHT2 = 3;

    @IntDef({CUSTOM_COLUMN_TOP_RIGHT, CUSTOM_COLUMN_BOTTOM_LEFT, CUSTOM_COLUMN_BOTTOM_RIGHT1,
            CUSTOM_COLUMN_BOTTOM_RIGHT2})
    @Retention(RetentionPolicy.SOURCE)
    public @interface CustomColumnPosition {
    }

    public StraasPlayerView(Context context) {
        this(context, null);
    }

    public StraasPlayerView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public StraasPlayerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.StraasLayout, defStyleAttr, 0);
        getCustomizedAttr(typedArray);
        typedArray.recycle();
    }

    /**
     * Initialize a StrassPlayerView, which can be used to generate layout and control UI.
     *
     * @param fragmentActivity A FragmentActivity which use StraasPlayerView.
     */
    public void initialize(@NonNull FragmentActivity fragmentActivity) {
        initialize(fragmentActivity, null);
    }

    /**
     * Initialize a StrassPlayerView, which can be used to generate layout and control UI.
     *
     * @param fragmentActivity A FragmentActivity which use StraasPlayerView.
     * @param configuration    A configuration for whether default widget to use or not.
     */
    public void initialize(@NonNull FragmentActivity fragmentActivity, @Nullable StraasConfiguration configuration) {

        mFragmentActivity = fragmentActivity;

        if (configuration != null) {
            initConfiguration(configuration);
        }

        init();
    }

    public Context getThemeContext() {
        return mThemeContext;
    }

    private void init() {
        ViewGroup straasMainContainer = (ViewGroup) LayoutInflater.from(getContext())
                .inflate(R.layout.straas_main_container, this, false);
        mThemeContext = straasMainContainer.getContext();
        int[] attr = new int[]{R.attr.selectableItemBackgroundBorderless};
        TypedArray typedArray = mThemeContext.obtainStyledAttributes(attr);
        mImageButtonBackground = Build.VERSION.SDK_INT > 21 ? typedArray.getResourceId(0, 0) : 0;
        typedArray.recycle();

        mControllerContainer = Utils.getView(straasMainContainer, R.id.controllerContainer);

        Utils.getView(straasMainContainer, R.id.upContainer).setBackground(
                new GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP,
                        new int[]{ContextCompat.getColor(mThemeContext, android.R.color.transparent),
                                ContextCompat.getColor(mThemeContext, R.color.color_controller_background_dark)}));
        Utils.getView(straasMainContainer, R.id.bottomContainer).setBackground(
                new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,
                        new int[]{ContextCompat.getColor(mThemeContext, android.R.color.transparent),
                                ContextCompat.getColor(mThemeContext, R.color.color_controller_background_dark)}));

        mVideoView = Utils.getView(straasMainContainer, R.id.videoSurfaceView);

        initColumn(straasMainContainer);

        mGestureTapDetector = new GestureDetectorCompat(getContext(), new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onSingleTapUp(MotionEvent event) {
                if (!mCanToggleControllerUi) {
                    return false;
                }

                Utils.toggleViewVisibilityWithAnimation(AUTO_HIDE_DELAY_MILLIS, mControllerContainer, mColumnPlayPause);
                return true;
            }

            @Override
            public boolean onDown(MotionEvent e) {
                return true;
            }
        });

        mGestureFakeDetector = new GestureDetectorCompat(getContext(), new GestureDetector.SimpleOnGestureListener());

        if (mEnableDefaultWidget) {
            if (mEnableDefaultSwitchQualityIcon) {
                View switchQualityView = View.inflate(mThemeContext, R.layout.switch_quality_layout, null);
                setSwitchQualityViewPosition(switchQualityView, CUSTOM_COLUMN_TOP_RIGHT);
            }
            if (mEnableDefaultChannelName) {
                TextView channelNameTextView = (TextView) View.inflate(mThemeContext, R.layout.channel_name, null);
                setCustomChannelName(channelNameTextView);
            }
            if (mEnableDefaultSummaryViewer) {
                TextView summaryViewerTextView = (TextView) View.inflate(mThemeContext, R.layout.summary_viewer, null);
                TextViewCompat.setCompoundDrawablesRelativeWithIntrinsicBounds(summaryViewerTextView,
                        VectorDrawableCompat.create(getResources(), R.drawable.ic_eye_24dp, null), null, null, null);
                setCustomSummaryViewerView(summaryViewerTextView);
            }
            if (mEnableDefaultLoadingProgressBar) {
                ProgressBar progressBar = (ProgressBar) View.inflate(mThemeContext, R.layout.loading_progress_bar, null);
                setCustomLoadingProgressBar(progressBar);

            }
            if (mEnableDefaultContentSeekBar) {
                ContentSeekBar contentSeekBar = new ContentSeekBar(mThemeContext);
                setCustomContentSeekBar(contentSeekBar);
            }
            if (mEnableDefaultPlay) {
                setCustomPlayIcon(R.drawable.ic_play_arrow_48dp);
            }
            if (mEnableDefaultPause) {
                setCustomPauseIcon(R.drawable.ic_pause_48dp);
            }
            if (mEnableDefaultReplay) {
                setCustomReplayIcon(R.drawable.ic_replay_48dp);
            }
            if (mEnableDefaultPrevious) {
                setCustomSkipToPreviousIcon(R.drawable.ic_skip_previous_48dp);
            }
            if (mEnableDefaultNext) {
                setCustomSkipToNextIcon(R.drawable.ic_skip_next_48px);
            }
            if (mEnableDefaultErrorMessage) {
                TextView errorMessageTextView = (TextView) View.inflate(mThemeContext, R.layout.error_message, null);
                Drawable drawable = VectorDrawableCompat.create(getResources(), R.drawable.ic_error_player, null);
                TextViewCompat.setCompoundDrawablesRelativeWithIntrinsicBounds(errorMessageTextView, drawable, null, null, null);
                setCustomErrorMessage(errorMessageTextView);
            }
            if (mEnableDefaultBroadcastStateMessage) {
                View broadcastStateOffline = View.inflate(mThemeContext, R.layout.broadcast_offline, null);
                setCustomBroadcastState(broadcastStateOffline);
            }
        }

        mAdView = Utils.getView(straasMainContainer, R.id.adSurfaceView);
        addView(straasMainContainer);

        setAutoHideControllerUiWhenTouch(true);
    }

    public void hideControllerViews() {
        mControllerContainer.setVisibility(GONE);
        mColumnPlayPause.setVisibility(GONE);
        mColumnAdPlay.setVisibility(GONE);
        mColumnLoadingBar.setVisibility(GONE);
        mColumnBroadcastState.setVisibility(GONE);
        mColumnErrorMessage.setVisibility(GONE);
    }

    private void initConfiguration(StraasConfiguration configuration) {
        mEnableDefaultWidget = configuration.isEnableDefaultWidget();
        mEnableDefaultSwitchQualityIcon = configuration.isEnableDefaultSwitchQuality();
        mEnableDefaultChannelName = configuration.isEnableDefaultChannelName();
        mEnableDefaultSummaryViewer = configuration.isEnableDefaultSummaryViewer();
        mEnableDefaultLoadingProgressBar = configuration.isEnableDefaultLoadingProgressBar();
        mEnableDefaultContentSeekBar = configuration.isEnableDefaultContentProgressBar();
        mEnableDefaultPlay = configuration.isEnableDefaultPlay();
        mEnableDefaultPause = configuration.isEnableDefaultPause();
        mEnableDefaultReplay = configuration.isEnableDefaultReplay();
        mEnableDefaultPrevious = configuration.isEnableDefaultSkipToPrevious();
        mEnableDefaultNext = configuration.isEnableDefaultSkipToNext();
        mEnableDefaultErrorMessage = configuration.isEnableDefaultErrorMessage();
        mEnableDefaultBroadcastStateMessage = configuration.isEnableDefaultBroadcastStateMessage();
    }

    private final MediaControllerCompat.Callback mMediaControllerCallback = new MediaControllerCompat.Callback() {
        @Override
        public void onQueueChanged(List<QueueItem> queue) {
            mLastQueueList = queue;
            if (mLastQueueList == null) {
                mLastMediaMetadata = null;
                if (mColumnPrevious.getVisibility() != GONE) {
                    mColumnPrevious.setVisibility(GONE);
                }
                if (mColumnNext.getVisibility() != GONE) {
                    mColumnNext.setVisibility(GONE);
                }
            }
        }

        @Override
        public void onSessionDestroyed() {
        }

        @Override
        public void onMetadataChanged(MediaMetadataCompat metadata) {
            if (metadata == null || (mLastMediaMetadata != null &&
                    TextUtils.equals(metadata.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID),
                            mLastMediaMetadata.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID)) &&
                    TextUtils.equals(metadata.getString(MediaMetadataCompat.METADATA_KEY_DISPLAY_TITLE),
                            mLastMediaMetadata.getString(MediaMetadataCompat.METADATA_KEY_DISPLAY_TITLE)))) {
                return;
            }

            mLastMediaMetadata = metadata;
            String mediaId = mLastMediaMetadata.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID);
            if (TextUtils.isEmpty(mediaId)) {
                return;
            }

            if (mediaId.startsWith(StraasMediaCore.LIVE_ID_PREFIX)) {
                switchMode(true);
                Bundle liveBundle = (mLiveBundle != null) ? mLiveBundle :
                        getMediaControllerCompat().getExtras();

                handleMediaSessionExtra(liveBundle, true);
            } else {
                switchMode(false);
            }

            if (mLastQueueList != null) {
                if (mLastPlaybackStateCompat.getActiveQueueItemId() == 0) {
                    if (mColumnPrevious.getVisibility() != GONE) {
                        mColumnPrevious.setVisibility(GONE);
                    }
                } else if (mColumnPrevious.getVisibility() != VISIBLE) {
                    mColumnPrevious.setVisibility(VISIBLE);
                }

                if (mLastPlaybackStateCompat.getActiveQueueItemId() == mLastQueueList.size() - 1) {
                    if (mColumnNext.getVisibility() != GONE) {
                        mColumnNext.setVisibility(GONE);
                    }
                } else if (mColumnNext.getVisibility() != VISIBLE) {
                    mColumnNext.setVisibility(VISIBLE);
                }
            }

            String title = metadata.getString(MediaMetadataCompat.METADATA_KEY_DISPLAY_TITLE);
            long summaryViewer = metadata.getBundle().getLong(VideoCustomMetadata.PLAY_COUNT_SUM);

            mChannelNameMetadataListener.onMetaChanged(mChannelNameTextView, title);
            mSummaryViewerMetadataListener.onMetaChanged(mSummaryViewerTextView, summaryViewer);
        }

        @Override
        public void onPlaybackStateChanged(PlaybackStateCompat state) {
            if (state == null || (mLastPlaybackStateCompat != null && mLastPlaybackStateCompat.getState() == state.getState() &&
                    mLastPlaybackStateCompat.getActiveQueueItemId() == state.getActiveQueueItemId())) {
                return;
            }
            mLastPlaybackStateCompat = state;
            if (!TextUtils.isEmpty(state.getErrorMessage())) {
                @ErrorReason.ErrorReasonType String errorType = state.getErrorMessage().toString();
                mErrorMessageListener.onError(mErrorMessageTextView, errorType);
                if (mIsLive) {
                    Bundle liveBundle = (mLiveBundle != null) ? mLiveBundle : getMediaControllerCompat().getExtras();

                    handleMediaSessionExtra(liveBundle, true);
                }
                if (getKeepScreenOn()) {
                    setKeepScreenOn(false);
                }
                return;
            }

            if (mColumnErrorMessage.getVisibility() != GONE) {
                setErrorMessageVisibility(GONE);
            }

            if (state.getState() == PlaybackStateCompat.STATE_PLAYING) {
                setErrorMessageVisibility(GONE);
                setBroadcastStateVisibility(GONE);
                if (mControllerContainer.getVisibility() != GONE) {
                    Utils.toggleViewVisibilityWithAnimation(AUTO_HIDE_DELAY_MILLIS, mControllerContainer, mColumnPlayPause);
                }
            }

            int loadingProgressBarVisibility = GONE;

            if (state.getActiveQueueItemId() == StraasMediaCore.AD_PLAYBACK_ID) {

                if (mControllerContainer.getVisibility() != GONE) {
                    mControllerContainer.setVisibility(GONE);
                }

                if (mColumnPlayPause.getVisibility() != GONE) {
                    mColumnPlayPause.setVisibility(GONE);
                }

                switch (state.getState()) {
                    case PlaybackStateCompat.STATE_BUFFERING:
                        loadingProgressBarVisibility = VISIBLE;
                        break;
                    case PlaybackStateCompat.STATE_PLAYING:
                        mColumnAdPlay.setVisibility(GONE);
                        break;
                    case PlaybackStateCompat.STATE_PAUSED:
                        mColumnAdPlay.setVisibility(VISIBLE);
                        break;
                }
            } else {
                if (mColumnAdPlay.getVisibility() != GONE) {
                    mColumnAdPlay.setVisibility(GONE);
                }

                switch (state.getState()) {
                    case PlaybackStateCompat.STATE_BUFFERING:
                        loadingProgressBarVisibility = VISIBLE;

                        mColumnPlay.setVisibility(GONE);
                        mColumnPause.setVisibility(GONE);
                        break;
                    case PlaybackStateCompat.STATE_PLAYING:
                        mCanToggleControllerUi = true;

                        switchToPause();
                        if (!getKeepScreenOn()) {
                            setKeepScreenOn(true);
                        }
                        break;
                    case PlaybackStateCompat.STATE_PAUSED:
                        mCanToggleControllerUi = false;

                        showControllerUi();
                        showPlayUi();
                        if (getKeepScreenOn()) {
                            setKeepScreenOn(false);
                        }
                        break;
                    case PlaybackStateCompat.STATE_NONE:
                        mCanToggleControllerUi = false;
                        hideControllerViews();
                        if (getKeepScreenOn()) {
                            setKeepScreenOn(false);
                        }
                    case PlaybackStateCompat.STATE_STOPPED:
                        if (mIsLive) {
                            Bundle liveBundle = (mLiveBundle != null) ? mLiveBundle :
                                    getMediaControllerCompat().getExtras();

                            handleMediaSessionExtra(liveBundle, true);
                        }
                        switchToReplay();
                        if (getKeepScreenOn()) {
                            setKeepScreenOn(false);
                        }
                        break;
                }
            }

            setLoadingProgressBarVisible(loadingProgressBarVisibility == VISIBLE);
        }

        @Override
        public void onSessionEvent(String event, Bundle extras) {
            switch (event) {
                case StraasMediaCore.EVENT_MEDIA_BROWSER_SERVICE_ERROR:
                    mErrorMessageListener.onError(mErrorMessageTextView,
                            extras.getString(StraasMediaCore.KEY_MEDIA_BROWSER_ERROR_REASON));
                    break;
            }
        }

        @Override
        public void onExtrasChanged(Bundle extras) {
            mLiveBundle = extras;
            if (!mIsLive) {
                return;
            }
            boolean isStopPlay = mLastPlaybackStateCompat != null &&
                    (mLastPlaybackStateCompat.getState() == PlaybackStateCompat.STATE_STOPPED ||
                            mLastPlaybackStateCompat.getState() == PlaybackStateCompat.STATE_NONE ||
                            mLastPlaybackStateCompat.getState() == PlaybackStateCompat.STATE_ERROR);
            handleMediaSessionExtra(extras, isStopPlay);
        }
    };

    private void handleMediaSessionExtra(Bundle extras, boolean shouldShowStateUi) {
        String liveState = extras.getString(StraasMediaCore.LIVE_EXTRA_BROADCAST_STATE, "");
        if (TextUtils.equals(liveState, mLiveState)) {
            return;
        }
        if (!TextUtils.isEmpty(liveState)) {
            switch (liveState) {
                case StraasMediaCore.LIVE_EXTRA_BROADCAST_STATE_LIVE:
                    mLiveState = liveState;
                    mBroadcastStateListener.online();
                    return;
                case StraasMediaCore.LIVE_EXTRA_BROADCAST_STATE_WAIT_STREAM:
                    if (shouldShowStateUi) {
                        mLiveState = liveState;
                        mBroadcastStateListener.waitForStream(mBroadcastStateView);
                    }
                    return;
            }
        } else if (shouldShowStateUi){
            String eventState = extras.getString(StraasMediaCore.LIVE_EXTRA_EVENT_STATE, "");
            mLiveState = eventState;
            switch (eventState) {
                case StraasMediaCore.LIVE_EXTRA_EVENT_STATE_ENDED:
                    mBroadcastStateListener.endEvent(mBroadcastStateView);
                    return;
                case StraasMediaCore.LIVE_EXTRA_EVENT_STATE_READY:
                    mBroadcastStateListener.offline(mBroadcastStateView);
                    return;
            }
        }
        mLiveState = null;
    }


    /**
     * To set a channel name metadata change event. If channel name metadata is changed, it will trigger this listener
     *
     * @param listener A listener to listen channel name metadata changed event.
     */
    public void setChannelNameMetalistener(@NonNull ChannelNameMetadataListener listener) {
        mChannelNameMetadataListener = listener;
    }

    /**
     * To set a summary viewer metadata change event. If summary viewer metadata is changed, it will trigger this listener
     *
     * @param listener A listener to listen summary viewer metadata changed event.
     */
    public void setSummaryViewerMetadataListener(@NonNull SummaryViewerMetadataListener listener) {
        mSummaryViewerMetadataListener = listener;
    }

    private void getCustomizedAttr(TypedArray typedArray) {
        mEnableDefaultWidget = typedArray.getBoolean(R.styleable.StraasLayout_defaultWidget, true);
        mEnableDefaultSwitchQualityIcon = typedArray.getBoolean(R.styleable.StraasLayout_defaultSwitchQualityIcon, true);
        mEnableDefaultSwitchQualityDialog = typedArray.getBoolean(R.styleable.StraasLayout_defaultSwitchQualityDialog, true);
        mEnableDefaultChannelName = typedArray.getBoolean(R.styleable.StraasLayout_defaultChannelName, true);
        mEnableDefaultSummaryViewer = typedArray.getBoolean(R.styleable.StraasLayout_defaultSummaryViewer, true);
        mEnableDefaultLoadingProgressBar = typedArray.getBoolean(R.styleable.StraasLayout_defaultLoadingProgressBar, true);
        mEnableDefaultContentSeekBar = typedArray.getBoolean(R.styleable.StraasLayout_defaultContentSeekbar, true);
        mEnableDefaultPlay = typedArray.getBoolean(R.styleable.StraasLayout_defaultPlay, true);
        mEnableDefaultPause = typedArray.getBoolean(R.styleable.StraasLayout_defaultPause, true);
        mEnableDefaultReplay = typedArray.getBoolean(R.styleable.StraasLayout_defaultReplay, true);
        mEnableDefaultPrevious = typedArray.getBoolean(R.styleable.StraasLayout_defaultSkipToPrevious, true);
        mEnableDefaultNext = typedArray.getBoolean(R.styleable.StraasLayout_defaultSkipToNext, true);
        mEnableDefaultErrorMessage = typedArray.getBoolean(R.styleable.StraasLayout_defaultErrorMessage, true);
        mEnableDefaultBroadcastStateMessage = typedArray.getBoolean(R.styleable.StraasLayout_defaultBroadcastStateMessage, true);
    }

    private void initColumn(View root) {
        mColumnSummaryViewer = Utils.getView(root, R.id.summaryViewerColumn);
        mColumnChannelName = Utils.getView(root, R.id.channelNameColumn);
        mColumnLoadingBar = Utils.getView(root, R.id.loadingBarProgressColumn);
        mColumnContentSeekBar = Utils.getView(root, R.id.contentProgressBarColumn);
        mColumnPlay = Utils.getView(root, R.id.playColumn);
        mColumnPause = Utils.getView(root, R.id.pauseColumn);
        mColumnReplay = Utils.getView(root, R.id.replayColumn);
        mColumnPrevious = Utils.getView(root, R.id.previousColumn);
        mColumnNext = Utils.getView(root, R.id.nextColumn);
        mColumnPlayPause = Utils.getView(root, R.id.playPauseColumn);
        mColumnAdPlay = Utils.getView(root, R.id.adPlayColumn);
        mColumnErrorMessage = Utils.getView(root, R.id.errorMessageColumn);
        mColumnBroadcastState = Utils.getView(root, R.id.onLineOfflineColumn);

        mColumnTopRight = Utils.getView(root, R.id.customColumnTopRight);
        mColumnBottomLeft = Utils.getView(root, R.id.bottomLeftColumn);
        mColumnBottomRight1 = Utils.getView(root, R.id.bottomRightColumn1);
        mColumnBottomRight2 = Utils.getView(root, R.id.bottomRightColumn2);

        mCustomColumnList.put(CUSTOM_COLUMN_TOP_RIGHT, mColumnTopRight);
        mCustomColumnList.put(CUSTOM_COLUMN_BOTTOM_LEFT, mColumnBottomLeft);
        mCustomColumnList.put(CUSTOM_COLUMN_BOTTOM_RIGHT1, mColumnBottomRight1);
        mCustomColumnList.put(CUSTOM_COLUMN_BOTTOM_RIGHT2, mColumnBottomRight2);
    }

    /**
     * To set a custom channel name view to instead of default widget.
     *
     * @param channelName custom TextView to show channel Name.
     */
    public void setCustomChannelName(@NonNull TextView channelName) {
        mColumnChannelName.removeAllViews();
        mColumnChannelName.setVisibility(VISIBLE);
        mColumnChannelName.addView(channelName);
        mChannelNameTextView = channelName;
    }

    /**
     * To set visibility of channel name TextView.
     *
     * @param visibility visibility for channel name.
     */
    public void setChannelNameVisibility(int visibility) {
        mColumnChannelName.setVisibility(visibility);
    }

    /**
     * To set a custom error message view to instead of default widget.
     *
     * @param errorMessage custom TextView to show error message.
     */
    public void setCustomErrorMessage(@NonNull TextView errorMessage) {
        mColumnErrorMessage.removeAllViews();
        RelativeLayout errorBackground = (RelativeLayout) View.inflate(mThemeContext, R.layout.error_background, null);
        errorBackground.addView(errorMessage);
        mColumnErrorMessage.addView(errorBackground);
        mErrorMessageTextView = errorMessage;
    }

    /**
     * To set visibility of error message TextView.
     *
     * @param visibility visibility for error message.
     */
    public void setErrorMessageVisibility(int visibility) {
        mColumnErrorMessage.setVisibility(visibility);
    }

    /**
     * To set a custom broadcast state message view to instead of default widget.
     *
     * @param message custom View to show broadcast state message.
     */
    public void setCustomBroadcastState(@NonNull View message) {
        mColumnBroadcastState.removeAllViews();
        mColumnBroadcastState.addView(message);
        mBroadcastStateView = message;
    }

    /**
     * To set visibility of broadcast state message TextView.
     *
     * @param visibility visibility for broadcast state message.
     */
    public void setBroadcastStateVisibility(int visibility) {
        mColumnBroadcastState.setVisibility(visibility);
    }

    /**
     * To set a channel name TextView style via res id.
     *
     * @param resId a style res id which to style channel name TextView.
     */
    public void setChannelNameAppearance(@StyleRes int resId) {
        if (mChannelNameTextView == null) {
            Log.e(TAG, "channel name text view is null.");
            return;
        }

        TextViewCompat.setTextAppearance(mChannelNameTextView, resId);
    }

    /**
     * To set a custom summary viewer view to instead of default widget.
     *
     * @param summaryViewer custom TextView to show summary viewer.
     */
    public void setCustomSummaryViewerView(@NonNull TextView summaryViewer) {
        mColumnSummaryViewer.removeAllViews();
        mColumnSummaryViewer.setVisibility(VISIBLE);
        mColumnSummaryViewer.addView(summaryViewer);
        mSummaryViewerTextView = summaryViewer;
    }

    /**
     * To set a summary viewer textview style via res id.
     *
     * @param resId a style res id which to style summary viewer TextView.
     */
    public void setSummaryViewerAppearance(@StyleRes int resId) {
        if (mSummaryViewerTextView == null) {
            Log.e(TAG, "summary viewer text view is null.");
            return;
        }

        TextViewCompat.setTextAppearance(mSummaryViewerTextView, resId);
    }

    /**
     * To set visibility of summary viewer.
     *
     * @param visibility visibility for summary viewer.
     */
    public void setSummaryViewerVisibility(int visibility) {
        mColumnSummaryViewer.setVisibility(visibility);
    }

    /**
     * To set visibility of content progress SeekBar.
     *
     * @param visibility visibility for content seek bar.
     */
    public void setContentSeekBarVisibility(int visibility) {
        mColumnContentSeekBar.setVisibility(visibility);
    }

    /**
     * To set a custom contentSeekBar view to instead of default widget.
     *
     * @param contentSeekBar custom ContentSeekBar to show progress.
     */
    public void setCustomContentSeekBar(@NonNull ContentSeekBar contentSeekBar) {
        mColumnContentSeekBar.removeAllViews();
        mColumnContentSeekBar.setVisibility(VISIBLE);
        mColumnContentSeekBar.addView(contentSeekBar);
        mContentSeekBar = contentSeekBar;
        mContentSeekBar.setOnTrackingListener(mTrackingListener);
    }

    /**
     * To set a custom loadingProgressBar view to instead of default widget.
     *
     * @param progressBar custom ProgressBar to show loading while player is buffering.
     */
    public void setCustomLoadingProgressBar(@NonNull ProgressBar progressBar) {
        mColumnLoadingBar.removeAllViews();
        mColumnLoadingBar.addView(progressBar);
    }

    /**
     * To set visibility of loading ProgressBar.
     *
     * @param visible true for visible, false for gone.
     */
    public void setLoadingProgressBarVisible(boolean visible) {
        mColumnLoadingBar.setVisibility(visible ? VISIBLE : GONE);
    }

    /**
     * To set switch quality position to other custom column.
     *
     * @param switchQuality the custom view to instead of default icon.
     * @param position      this position which to set up icon.
     */
    public void setSwitchQualityViewPosition(@NonNull View switchQuality, @CustomColumnPosition int position) {
        ViewGroup column = mCustomColumnList.get(position);
        ViewParent parent = null;

        if (mSwitchQualityView != null) {
            parent = mSwitchQualityView.getParent();
        }

        if (parent != null && parent instanceof ViewGroup) {
            ((ViewGroup) (parent)).removeView(mSwitchQualityView);
            ((ViewGroup) (parent)).setVisibility(GONE);
        }

        column.setVisibility(VISIBLE);
        column.removeAllViews();
        column.addView(switchQuality);
        mSwitchQualityView = switchQuality;
        mSwitchQualityView.setBackgroundResource(mImageButtonBackground);
        mSwitchQualityView.setOnClickListener(mSwitchQualityClickListener);
    }

    /**
     * To set switch quality position to other custom column.
     *
     * @param position this position which to set up icon.
     */
    public void setSwitchQualityViewPosition(@CustomColumnPosition int position) {
        if (mSwitchQualityView == null) {
            mSwitchQualityView = View.inflate(mThemeContext, R.layout.switch_quality_layout, null);
        }

        setSwitchQualityViewPosition(mSwitchQualityView, position);
    }

    /**
     * To enable default switch quality dialog to open when click switch quality icon view.
     *
     * @param enable true for enable, false for disable.
     */
    public void setEnableDefaultSwitchDialog(boolean enable) {
        mEnableDefaultSwitchQualityDialog = enable;
    }

    /**
     * To set listener. If switch quality view is clicked, it will trigger listener.
     *
     * @param listener a listener to listen switch quality view click event.
     */
    public void setOnClickSwitchQualityViewListener(SwitchQualityViewClickListener listener) {
        mSwitchQualityViewListener = listener;
    }

    /**
     * To set listener. If player throws error message, it will trigger this listener .
     *
     * @param listener a listener to listen error message.
     */
    public void setOnErrorMessageThrowListener(ErrorMessageListener listener) {
        mErrorMessageListener = listener;
    }

    /**
     * To set custom view to specific custom column.
     *
     * @param customView this custom view.
     * @param position   this position to set up custom view.
     */
    public void setCustomViewToColumn(@NonNull View customView, @CustomColumnPosition int position) {
        ViewGroup column = mCustomColumnList.get(position);

        column.setVisibility(VISIBLE);
        column.removeAllViews();
        column.addView(customView);
    }

    /**
     * To remove view in specific column.
     *
     * @param position this position to remove view.
     */
    public void removeViewFromCustomColumn(@CustomColumnPosition int position) {
        ViewGroup column = mCustomColumnList.get(position);

        column.setVisibility(GONE);
        column.removeAllViews();
    }

    /**
     * To remove views in all custom column.
     */
    public void removeViewAllCustomColumn() {
        mColumnTopRight.removeAllViews();
        mColumnTopRight.setVisibility(GONE);
        mColumnBottomLeft.removeAllViews();
        mColumnBottomLeft.setVisibility(GONE);
        mColumnBottomRight1.removeAllViews();
        mColumnBottomRight1.setVisibility(GONE);
        mColumnBottomRight2.removeAllViews();
        mColumnBottomRight2.setVisibility(GONE);
    }

    /**
     * To set logo at specific position.
     *
     * @param resId    the logo icon res id.
     * @param position this position to set up logo.
     */
    public View setLogo(@DrawableRes int resId, @CustomColumnPosition int position) {
        ViewGroup column = mCustomColumnList.get(position);
        ImageView imageView = null;

        if (mLogoView != null) {
            ViewParent parent = mLogoView.getParent();

            if (parent != null && parent instanceof ViewGroup) {
                ((ViewGroup) (parent)).removeView(mLogoView);
                ((ViewGroup) (parent)).setVisibility(GONE);
            }
        }

        column.setVisibility(VISIBLE);
        column.removeAllViews();
        imageView = new AppCompatImageView(mThemeContext);
        mLogoView = imageView;
        imageView.setImageResource(resId);
        column.addView(imageView);

        return imageView;
    }

    /**
     * To set custom play icon.
     *
     * @param resId the play icon res id.
     */
    public void setCustomPlayIcon(@DrawableRes int resId) {
        ImageButton imageButton;

        mColumnPlay.removeAllViews();
        imageButton = new AppCompatImageButton(mThemeContext);
        imageButton.setImageResource(resId);
        imageButton.setBackgroundResource(mImageButtonBackground);
        imageButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {

                if (!mIsBind && mStraasMediaCore != null) {
                    mStraasMediaCore.setUiContainer(StraasPlayerView.this);
                }

                mCanToggleControllerUi = true;

                getMediaControllerCompat().getTransportControls().play();

                resetPlayPauseUiWithControllerVisibility();
                switchToPause();

                Utils.toggleViewVisibilityWithAnimation(AUTO_HIDE_DELAY_MILLIS, mControllerContainer, mColumnPlayPause);
            }
        });
        mColumnPlay.addView(imageButton);

        mColumnAdPlay.removeAllViews();
        imageButton = new AppCompatImageButton(mThemeContext);
        imageButton.setImageResource(resId);
        imageButton.setBackgroundResource(mImageButtonBackground);
        imageButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                mColumnAdPlay.setVisibility(GONE);
                getMediaControllerCompat().getTransportControls().play();
            }
        });
        mColumnAdPlay.addView(imageButton);
    }

    /**
     * To set custom pause icon.
     *
     * @param resId the pause icon res id.
     */
    public void setCustomPauseIcon(@DrawableRes int resId) {
        ImageButton imageButton;

        mColumnPause.removeAllViews();
        imageButton = new AppCompatImageButton(mThemeContext);
        imageButton.setImageResource(resId);
        imageButton.setBackgroundResource(mImageButtonBackground);
        imageButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                mCanToggleControllerUi = false;

                getMediaControllerCompat().getTransportControls().pause();
                showControllerUi();
                showPlayUi();
            }
        });
        mColumnPause.addView(imageButton);
    }

    /**
     * To set custom replay icon.
     *
     * @param resId the replay icon res id.
     */
    public void setCustomReplayIcon(@DrawableRes int resId) {
        ImageButton imageButton;

        mColumnReplay.removeAllViews();
        imageButton = new AppCompatImageButton(mThemeContext);
        imageButton.setImageResource(resId);
        imageButton.setBackgroundResource(mImageButtonBackground);
        imageButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                mCanToggleControllerUi = true;
                getMediaControllerCompat().getTransportControls().seekTo(0);
                switchToPause();
            }
        });
        mColumnReplay.addView(imageButton);
    }

    /**
     * To set custom skip-to-previous icon.
     *
     * @param resId the replay icon res id.
     */
    public void setCustomSkipToPreviousIcon(@DrawableRes int resId) {
        ImageButton imageButton;

        mColumnPrevious.removeAllViews();
        imageButton = new AppCompatImageButton(mThemeContext);
        imageButton.setImageResource(resId);
        imageButton.setBackgroundResource(mImageButtonBackground);
        imageButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                getMediaControllerCompat().getTransportControls().skipToPrevious();
            }
        });
        mColumnPrevious.addView(imageButton);
    }

    /**
     * To set custom skip-to-next icon.
     *
     * @param resId the replay icon res id.
     */
    public void setCustomSkipToNextIcon(@DrawableRes int resId) {
        ImageButton imageButton;

        mColumnNext.removeAllViews();
        imageButton = new AppCompatImageButton(mThemeContext);
        imageButton.setImageResource(resId);
        imageButton.setBackgroundResource(mImageButtonBackground);
        imageButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                getMediaControllerCompat().getTransportControls().skipToNext();
            }
        });
        mColumnNext.addView(imageButton);
    }

    /**
     * To enable controller ui to show when touch screen.
     *
     * @param enable true for enable, false for disable.
     */
    public void setAutoHideControllerUiWhenTouch(boolean enable) {
        if (enable) {
            mGestureDetector = mGestureTapDetector;
        } else {
            mGestureDetector = mGestureFakeDetector;
        }
    }

    /**
     * To add listener. If player is onConnected, it will trigger listeners.
     *
     * @param listener a listener to listen player onConnected event.
     */
    public void addMediaConnectedListener(ConnectionCallback listener) {
        mMediaConnectedListenerList.add(listener);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        mGestureDetector.onTouchEvent(event);
        return super.onInterceptTouchEvent(event);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP) {
            mGestureDetector.onTouchEvent(event);
            return super.onTouchEvent(event);
        } else {
            return true;
        }
    }

    public void setTitle(CharSequence title) {
        if (mChannelNameTextView != null) {
            mChannelNameTextView.setText(title);
        }
    }

    private class ChannelNameMetadataListener {
        void onMetaChanged(TextView channelNameTextView, String channelName) {
            if (channelNameTextView != null && !TextUtils.isEmpty(channelName)) {
                channelNameTextView.setText(channelName);
            }
        }
    }

    private class SummaryViewerMetadataListener {
        void onMetaChanged(TextView summaryViewerTextView, long summaryViewer) {
            if (summaryViewerTextView != null) {
                summaryViewerTextView.setText(Utils.convertLong(summaryViewer));
            }
        }
    }

    private class ErrorMessageListener {
        void onError(TextView errorMessageTextView, @ErrorReason.ErrorReasonType String errorType) {
            if (errorMessageTextView != null) {
                mColumnErrorMessage.setVisibility(VISIBLE);

                String message;

                switch (errorType) {
                    case ErrorReason.NETWORK_ERROR:
                        message = getContext().getString(R.string.network_no_connection);
                        break;
                    case ErrorReason.NOT_FOUND:
                        message = getContext().getString(R.string.content_no_exist);
                        break;
                    case ErrorReason.NOT_PUBLIC:
                        message = getContext().getString(R.string.content_no_public);
                        break;
                    case ErrorReason.MEDIA_PERMISSION_DENIAL:
                        message = getContext().getString(R.string.access_permission_denial);
                        break;
                    case ErrorReason.TEMPORARILY_UNAVAILABLE:
                    case ErrorReason.DATA_DESERIALIZE_ERROR:
                    case ErrorReason.INTERNAL_ERROR:
                    default:
                        message = getContext().getString(R.string.common_error);
                        break;
                }

                mErrorMessageTextView.setText(message);
            }
        }
    }

    private class BroadcastStateListener {
        void offline(View broadcastStateView) {
            if (broadcastStateView == null) {
                return;
            }
            TextView textView = Utils.getView(broadcastStateView, android.R.id.text1);
            if (textView == null) {
                return;
            }
            setErrorMessageVisibility(GONE);
            setBroadcastStateVisibility(VISIBLE);
            textView.setText(R.string.broadcast_state_offline);
        }

        void endEvent(View broadcastStateView) {
            if (broadcastStateView == null) {
                return;
            }
            TextView textView = Utils.getView(broadcastStateView, android.R.id.text1);
            if (textView == null) {
                return;
            }
            setErrorMessageVisibility(GONE);
            setBroadcastStateVisibility(VISIBLE);
            textView.setText(R.string.broadcast_state_ended);
        }

        void online() {
            setErrorMessageVisibility(GONE);
            setBroadcastStateVisibility(GONE);
        }

        void waitForStream(View broadcastStateView) {
            if (broadcastStateView == null) {
                return;
            }
            TextView textView = Utils.getView(broadcastStateView, android.R.id.text1);
            if (textView == null) {
                return;
            }
            setErrorMessageVisibility(GONE);
            setBroadcastStateVisibility(VISIBLE);
            textView.setText(R.string.broadcast_state_wait_for_stream);
        }
    }

    private MediaControllerCompat getMediaControllerCompat() {
        return MediaControllerCompat.getMediaController(mFragmentActivity);
    }

    private OnClickListener mSwitchQualityClickListener = new OnClickListener() {
        @Override
        public void onClick(final View view) {
            MediaControllerCompatHelper.getVideoQualityInfo(getMediaControllerCompat(),
                    new VideoQualityInfoCallback() {
                        @Override
                        public void onGetVideoQualityInfo(VideoQualityInfo info) {
                            if (mEnableDefaultSwitchQualityDialog) {
                                SwitchQualityDialog dialog = SwitchQualityDialog.newInstance(
                                        info.mFormats,
                                        info.mCurrentSelectedIndex);
                                dialog.show(mFragmentActivity.getSupportFragmentManager(),
                                        SwitchQualityDialog.class.getSimpleName());
                            }

                            if (mSwitchQualityViewListener != null) {
                                mSwitchQualityViewListener.onFormatCallback(info.mFormats,
                                        info.mCurrentSelectedIndex);
                            }
                        }
                    });
        }
    };

    private ContentSeekBar.TrackingListener mTrackingListener = new ContentSeekBar.TrackingListener() {

        @Override
        public void onTrackingTouch(boolean isTracking) {
            if (isTracking) {
                Utils.stopAnimation(mControllerContainer, mColumnPlayPause);
            } else {
                Utils.toggleViewVisibilityWithAnimation(AUTO_HIDE_DELAY_MILLIS, mControllerContainer, mColumnPlayPause);
            }
        }
    };

    @Override
    protected void onDetachedFromWindow() {
        mMediaConnectedListenerList.clear();

        if (mContentSeekBar != null) {
            mContentSeekBar.destroy();
        }

        super.onDetachedFromWindow();
    }

    @NonNull
    @Override
    public ViewGroup getVideoContainer() {
        return mVideoView;
    }

    @Nullable
    @Override
    public ViewGroup getAdContainer() {
        return mAdView;
    }

    @Override
    public void onUnbind(StraasMediaCore client) {
        mIsBind = false;
        mStraasMediaCore = client;

        MediaControllerCompat mediaControllerCompat = getMediaControllerCompat();
        if (mediaControllerCompat != null) {
            mediaControllerCompat.unregisterCallback(mMediaControllerCallback);
        }
        if (mContentSeekBar != null) {
            mContentSeekBar.destroy();
        }
        mCanToggleControllerUi = false;

        showControllerUi();
        showPlayUi();
    }

    @Override
    public void onMediaBrowserConnected(StraasMediaCore client) {
        mIsBind = true;
        if (mFragmentActivity == null) {
            Log.e(TAG, "It's not FragmentActivity.");
            return;
        }
        MediaControllerCompat.setMediaController(mFragmentActivity, client.getMediaController());
        for (ConnectionCallback connectionCallback : mMediaConnectedListenerList) {
            connectionCallback.onConnected();
        }

        mMediaControllerCallback.onExtrasChanged(getMediaControllerCompat().getExtras());
        mMediaControllerCallback.onMetadataChanged(getMediaControllerCompat().getMetadata());
        mMediaControllerCallback.onPlaybackStateChanged(getMediaControllerCompat().getPlaybackState());
        mMediaControllerCallback.onQueueChanged(getMediaControllerCompat().getQueue());
        mMediaControllerCallback.onQueueTitleChanged(getMediaControllerCompat().getQueueTitle());

        getMediaControllerCompat().registerCallback(mMediaControllerCallback);

        if (mContentSeekBar != null) {
            mContentSeekBar.setMediaPlayer(new PlayerControl(mFragmentActivity, null));
        }
    }

    @Override
    public void onMediaBrowserConnectionSuspended() {
        mIsBind = false;
    }

    @Override
    public void onMediaBrowserConnectionFailed() {
        mIsBind = false;
    }

    private void showControllerUi() {
        Utils.stopAnimation(mControllerContainer);
        Utils.resetAlpha(mControllerContainer);
        mControllerContainer.setVisibility(VISIBLE);
    }

    private void showPlayUi() {
        Utils.stopAnimation(mColumnPlayPause);
        Utils.resetAlpha(mColumnPlayPause);
        mColumnPlayPause.setVisibility(VISIBLE);
        switchToPlay();
    }

    private void resetPlayPauseUiWithControllerVisibility() {
        if (mColumnPlayPause.getVisibility() != mControllerContainer.getVisibility()) {
            mColumnPlayPause.setVisibility(mControllerContainer.getVisibility());
        }
    }

    private void switchToPlay() {
        mColumnPlay.setVisibility(VISIBLE);
        mColumnReplay.setVisibility(INVISIBLE);
        mColumnPause.setVisibility(INVISIBLE);
    }

    private void switchToPause() {
        mColumnPlay.setVisibility(INVISIBLE);
        mColumnReplay.setVisibility(INVISIBLE);
        mColumnPause.setVisibility(VISIBLE);
    }

    private void switchToReplay() {
        mColumnReplay.setVisibility(VISIBLE);
        mColumnPlay.setVisibility(INVISIBLE);
        mColumnPause.setVisibility(INVISIBLE);
    }

    private void switchMode(boolean isLive) {
        if (mIsLive == isLive) {
            return;
        }
        mIsLive = isLive;

        if (isLive) {
            setContentSeekBarVisibility(GONE);
            setSummaryViewerVisibility(INVISIBLE);

            TextView live = (TextView) View.inflate(mThemeContext, R.layout.live_view, null);
            Drawable drawable = VectorDrawableCompat.create(getResources(), R.drawable.ic_live_player, null);
            TextViewCompat.setCompoundDrawablesRelativeWithIntrinsicBounds(live, drawable, null, null, null);
            setCustomViewToColumn(live, CUSTOM_COLUMN_BOTTOM_LEFT);
        } else {
            setContentSeekBarVisibility(VISIBLE);
            setSummaryViewerVisibility(VISIBLE);

            removeViewFromCustomColumn(CUSTOM_COLUMN_BOTTOM_LEFT);
        }
    }
}
