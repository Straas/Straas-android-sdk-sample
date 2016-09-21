package io.straas.android.media.demo;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaBrowserCompat.MediaItem;
import android.support.v4.media.MediaBrowserCompat.SubscriptionCallback;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import io.straas.android.media.demo.widget.StraasPlayerView;
import io.straas.android.sdk.media.ImaHelper;
import io.straas.android.sdk.media.StraasMediaCore;
import io.straas.android.sdk.media.VideoCustomMetadata;
import io.straas.android.sdk.mediacore.demo.R;
import io.straas.sdk.demo.MemberIdentity;

/**
 * Demo for querying video list, click item to play.
 * Using {@link Loader} to handle {@link Activity} lifecycle.
 */
public class StraasPlayerActivity extends AppCompatActivity {
    private static final String TAG = StraasPlayerActivity.class.getSimpleName();
    private Adapter mAdapter;
    private StraasMediaCore mStraasMediaCore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.straas_player_view);
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            getSupportActionBar().hide();
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }

        AspectRatioFrameLayout aspectRatioFrameLayout = (AspectRatioFrameLayout) findViewById(R.id.straasPlayer);
        if (aspectRatioFrameLayout != null) {
            aspectRatioFrameLayout.setAspectRatio(1.778f);
        }

        final StraasPlayerView playerView = (StraasPlayerView) findViewById(R.id.straas);
        playerView.initialize(this);

        getSupportLoaderManager().initLoader(0, null, new LoaderManager.LoaderCallbacks<StraasMediaCore>() {
            @Override
            public Loader<StraasMediaCore> onCreateLoader(int id, Bundle args) {
                return new StraasMediaCoreLoader(StraasPlayerActivity.this, MemberIdentity.ME);
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
                getSupportMediaController().registerCallback(mMediaControllerCallback);
                if (mAdapter != null) {
                    getMediaBrowser().unsubscribe(getMediaBrowser().getRoot());
                    getMediaBrowser().subscribe(getMediaBrowser().getRoot(), mSubscriptionCallback);
                }
            }

            @Override
            public void onLoaderReset(Loader<StraasMediaCore> loader) {

            }
        });
        RecyclerView recyclerView = (RecyclerView) findViewById(android.R.id.list);
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
                    getMediaBrowser().unsubscribe(pageToken);
                    getMediaBrowser().subscribe(pageToken, mSubscriptionCallback);
                }
            }
        });

    }

    private MediaBrowserCompat getMediaBrowser() {
        return mStraasMediaCore.getMediaBrowser();
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
        if (!isFinishing() && getSupportMediaController() != null) {
            getSupportMediaController().getTransportControls().pause();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (getSupportMediaController() != null) {
            getSupportMediaController().unregisterCallback(mMediaControllerCallback);
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
                ((ViewHolder)holder).bind(mMediaItems.get(position));
            } else if (holder instanceof LoadingViewHolder) {
                if (mLoadMoreListener != null) {
                    mLoadMoreListener.onLoadMore();
                }
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
                if (item.getDescription().getIconUri() != null) {
                    Glide.with(mIcon.getContext()).load(item.getDescription().getIconUri()).into(mIcon);
                }
                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        MediaControllerCompat controller = ((FragmentActivity)getActivity(v)).getSupportMediaController();
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
