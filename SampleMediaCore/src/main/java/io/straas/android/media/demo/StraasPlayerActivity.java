package io.straas.android.media.demo;

import android.app.*;
import android.content.*;
import android.content.res.*;
import android.os.*;
import android.support.v4.media.*;
import android.support.v4.media.MediaBrowserCompat.*;
import android.support.v4.media.session.*;
import android.text.*;
import android.util.*;
import android.view.*;
import android.widget.*;

import com.bumptech.glide.*;

import java.util.*;

import androidx.annotation.*;
import androidx.appcompat.app.*;
import androidx.core.content.*;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.Loader;
import androidx.recyclerview.widget.*;
import io.straas.android.media.demo.widget.*;
import io.straas.android.media.demo.widget.ui.*;
import io.straas.android.sdk.demo.common.*;
import io.straas.android.sdk.media.*;

/**
 * Demo for querying video list, click item to play.
 * Using {@link Loader} to handle {@link Activity} lifecycle.
 */
public class StraasPlayerActivity extends AppCompatActivity {
    private static final String TAG = StraasPlayerActivity.class.getSimpleName();
    private Adapter mAdapter;
    private StraasMediaCore mStraasMediaCore;
    private EditText mPlaylistIdEditText;
    private String mLastParentId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.straas_player_view);
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            getSupportActionBar().hide();
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }

        AspectRatioFrameLayout aspectRatioFrameLayout = findViewById(R.id.straas_player);
        if (aspectRatioFrameLayout != null) {
            aspectRatioFrameLayout.setAspectRatio(1.778f);
        }

        final StraasPlayerView playerView = findViewById(R.id.straas);
        playerView.initialize(this);

        getSupportLoaderManager().initLoader(0, null, new LoaderManager.LoaderCallbacks<StraasMediaCore>() {
            @Override
            public Loader<StraasMediaCore> onCreateLoader(int id, Bundle args) {
                return createStraasMediaCoreLoader();
            }

            @Override
            public void onLoadFinished(Loader<StraasMediaCore> loader, StraasMediaCore data) {
                if (data == null || !data.getMediaBrowser().isConnected()) {
                    return;
                }
                mStraasMediaCore = data;
                mStraasMediaCore.setUiContainer(playerView)
                        // remove setImaHelper if you don't want to include ad system (IMA)
                        .setImaHelper(ImaHelper.newInstance());
                getMediaControllerCompat().registerCallback(mMediaControllerCallback);
            }

            @Override
            public void onLoaderReset(Loader<StraasMediaCore> loader) {

            }
        });
        RecyclerView recyclerView = findViewById(android.R.id.list);
        if (recyclerView == null) {
            return;
        }
        mAdapter = new Adapter();
        recyclerView.setAdapter(mAdapter);
        mAdapter.setLoadMoreListener(new Adapter.LoadMoreListener() {
            @Override
            public void onLoadMore() {
                String pageToken = Utils.extractPageToken(mAdapter.getMediaItems());
                if (!TextUtils.isEmpty(pageToken)) {
                    unsubscribe();
                    subscribe(pageToken);
                }
            }
        });
        mPlaylistIdEditText = findViewById(R.id.edit_text_playlist_id);
    }

    protected Loader<StraasMediaCore> createStraasMediaCoreLoader() {
        return new StraasMediaCoreLoader(StraasPlayerActivity.this, MemberIdentity.ME);
    }

    public void loadVodList(View view) {
        if (mStraasMediaCore == null || mAdapter == null) {
            return;
        }
        stopPlayer();
        unsubscribe();
        mAdapter.clearAllItems();
        subscribe(StraasMediaCore.PARENT_ID_VODS);
    }

    public void loadPlaylist(View view) {
        if (mStraasMediaCore == null || mAdapter == null) {
            return;
        }
        stopPlayer();
        unsubscribe();
        mAdapter.clearAllItems();
        subscribe(mPlaylistIdEditText.getText().toString());
    }

    private void subscribe(String parentId) {
        mLastParentId = parentId;
        if (TextUtils.isEmpty(parentId)) {
            return;
        }
        getMediaBrowser().subscribe(parentId, mSubscriptionCallback);
    }

    private void unsubscribe() {
        if (mStraasMediaCore == null || TextUtils.isEmpty(mLastParentId)) {
            return;
        }
        getMediaBrowser().unsubscribe(mLastParentId);
    }

    private void stopPlayer() {
        MediaControllerCompat.getMediaController(this).getTransportControls().stop();
    }

    private MediaBrowserCompat getMediaBrowser() {
        return mStraasMediaCore.getMediaBrowser();
    }


    private MediaControllerCompat getMediaControllerCompat() {
        return MediaControllerCompat.getMediaController(this);
    }
    
    @Override
    protected void onStart() {
        super.onStart();
        if (getMediaControllerCompat() != null) {
            getMediaControllerCompat().getTransportControls().play();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (!isFinishing() && getMediaControllerCompat() != null) {
            getMediaControllerCompat().getTransportControls().pause();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (getMediaControllerCompat() != null) {
            getMediaControllerCompat().unregisterCallback(mMediaControllerCallback);
        }
    }

    private SubscriptionCallback mSubscriptionCallback = new SubscriptionCallback() {
        @Override
        public void onChildrenLoaded(@NonNull String parentId, List<MediaItem> children) {
            if (children == null || children.isEmpty()) {
                mAdapter.enableLoadMore(false);
            } else {
                int start = mAdapter.getMediaItems().size();
                mAdapter.getMediaItems().addAll(children);
                mAdapter.notifyItemRangeInserted(start, children.size());
                String token = Utils.extractPageToken(mAdapter.getMediaItems());
                mAdapter.enableLoadMore(!TextUtils.isEmpty(token));
            }
            getMediaBrowser().unsubscribe(parentId);
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
            }
        }
    };

    private static class Adapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private List<MediaItem> mMediaItems = new ArrayList<>();
        private boolean mEnableLoadMore = false;
        private static final int TYPE_LOAD_MORE = 1;
        private LoadMoreListener mLoadMoreListener;

        public interface LoadMoreListener {
            void onLoadMore();
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            switch (viewType) {
                case TYPE_LOAD_MORE:
                    return new LoadingViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.loading_row, parent, false));
                default:
                    View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item, parent, false);
                    return new ViewHolder(v);
            }

        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            if (holder instanceof ViewHolder) {
                if (position == mMediaItems.size() - 1 && mEnableLoadMore) {
                    if (mLoadMoreListener != null) {
                        mLoadMoreListener.onLoadMore();
                    }
                }
                ((ViewHolder)holder).bind(mMediaItems.get(position));
            } else if (holder instanceof LoadingViewHolder) {
            }
        }

        public boolean enableLoadMore(boolean enable) {
            int loadMoreIndex = mMediaItems.size();

            if (0 == loadMoreIndex) {
                return false;
            }

            if (mEnableLoadMore == enable) {
                return false;
            }

            mEnableLoadMore = enable;

            if (enable) {
                notifyItemInserted(loadMoreIndex);
            } else {
                notifyItemRemoved(loadMoreIndex);
            }

            return true;
        }

        public void clearAllItems() {
            int itemCount = mEnableLoadMore ? mMediaItems.size() + 1 : mMediaItems.size();
            mEnableLoadMore = false;
            mMediaItems.clear();
            notifyItemRangeRemoved(0, itemCount);
        }

        @Override
        public int getItemViewType(int position) {
            if (isLoadMorePosition(position)) {
                return TYPE_LOAD_MORE;
            }
            return super.getItemViewType(position);
        }

        private boolean isLoadMorePosition(int position) {
            return mEnableLoadMore && position == mMediaItems.size();
        }

        public List<MediaItem> getMediaItems() {
            return mMediaItems;
        }

        public void setLoadMoreListener(LoadMoreListener listener) {
            mLoadMoreListener = listener;
        }

        @Override
        public int getItemCount() {
            return mMediaItems.size() + (mEnableLoadMore ? 1 : 0);
        }

        public static class LoadingViewHolder extends RecyclerView.ViewHolder {
            public LoadingViewHolder(View itemView) {
                super(itemView);
            }
        }


        public static class ViewHolder extends RecyclerView.ViewHolder {
            public ImageView mIcon;
            public TextView mTitle, mSubtitle;

            public ViewHolder(View itemView) {
                super(itemView);
                mIcon = (ImageView) itemView.findViewById(android.R.id.icon);
                mTitle = (TextView) itemView.findViewById(android.R.id.title);
                mSubtitle = (TextView) itemView.findViewById(android.R.id.summary);
            }

            public void bind(final MediaItem item) {
                mTitle.setText(item.getDescription().getTitle());
                mSubtitle.setText(item.getDescription().getDescription());
                Context context = mIcon.getContext();
                Bundle extras = item.getDescription().getExtras();
                if (item.getDescription().getIconUri() != null) {
                    Glide.with(mIcon.getContext()).load(item.getDescription().getIconUri()).into(mIcon);
                } else if (extras == null ||
                        extras.getInt(StraasMediaCore.KEY_VIDEO_RENDER_TYPE) != StraasMediaCore.VIDEO_RENDER_TYPE_NONE) {
                    Glide.with(mIcon.getContext()).load(ContextCompat.getDrawable(context,
                            R.drawable.vod_thumbnail))
                            .into(mIcon);
                } else {
                    Glide.with(mIcon.getContext()).load(ContextCompat.getDrawable(context,
                            R.drawable.vod_thumbnail_audio))
                            .into(mIcon);
                }
                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        MediaControllerCompat controller = MediaControllerCompat.getMediaController(getActivity(v));
                        if (controller != null) {
                            if (item.isPlayable()) {
                                controller.getTransportControls().playFromMediaId(item.getMediaId(), null);
                            } else if (item.isBrowsable()) {
                                //    TODO

                            }
                        }
                    }
                });
            }

            private static Activity getActivity(View view) {
                Context context = view.getContext();
                while (context instanceof ContextWrapper) {
                    if (context instanceof Activity) {
                        return (Activity)context;
                    }
                    context = ((ContextWrapper)context).getBaseContext();
                }
                return null;
            }
        }

    }

}
