package io.straas.android.sdk.streaming.demo.screencast;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.format.DateUtils;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;
import io.straas.android.sdk.streaming.demo.R;

import static io.straas.android.sdk.streaming.StreamManager.STATE_CONNECTING;
import static io.straas.android.sdk.streaming.StreamManager.STATE_IDLE;
import static io.straas.android.sdk.streaming.StreamManager.STATE_PREPARED;
import static io.straas.android.sdk.streaming.StreamManager.STATE_STREAMING;

@SuppressLint("ViewConstructor")
final class ControlOverlayLayout extends OverlayLayout implements View.OnClickListener {
    private View mMoveView;
    private View mFinishView;
    private View mStartView;
    private View mLoadingView;
    private TextView mStreamingTimeView;

    static ControlOverlayLayout create(Context context, Listener listener) {
        return new ControlOverlayLayout(context, listener);
    }

    private ControlOverlayLayout(Context context, Listener listener) {
        super(context, listener);
    }

    @Override
    public int getInflateResource() {
        return R.layout.control_overlay_layout;
    }

    @SuppressLint("RtlHardcoded")
    public int getLayoutGravity() {
        return Gravity.TOP | Gravity.LEFT;
    }

    @Override
    public void initLayoutViews() {
        mMoveView = findViewById(R.id.screencast_overlay_move);

        mFinishView = findViewById(R.id.screencast_overlay_finish);
        mStartView = findViewById(R.id.screencast_overlay_start);
        mLoadingView = findViewById(R.id.screencast_overlay_loading);
        mStreamingTimeView = findViewById(R.id.screencast_overlay_elapsed_time);

        mFinishView.setOnClickListener(this);
        mStartView.setOnClickListener(this);
        updateStreamingStatus(STATE_IDLE);
    }

    @Override
    public View getMoveView() {
        return mMoveView;
    }

    @Override
    public void onClick(final View v) {
        if (mListener != null) {
            mListener.onClick(v);
        }
    }

    public void updateStreamingStatusOnUiThread(final int state) {
        post(new Runnable() {
            @Override
            public void run() {
                updateStreamingStatus(state);
            }
        });
    }

    public void updateStreamingStatus(int state) {
        switch(state) {
            case STATE_IDLE:
                mStartView.setEnabled(false);
                mStartView.setSelected(false);
                mLoadingView.setVisibility(View.GONE);
                mStreamingTimeView.setVisibility(View.GONE);
                mFinishView.setVisibility(View.VISIBLE);
                break;
            case STATE_PREPARED:
                mStartView.setEnabled(true);
                mStartView.setSelected(false);
                mLoadingView.setVisibility(View.GONE);
                mStreamingTimeView.setVisibility(View.GONE);
                mFinishView.setVisibility(View.VISIBLE);
                break;
            case STATE_CONNECTING:
                mStartView.setEnabled(false);
                mStartView.setSelected(false);
                mLoadingView.setVisibility(View.VISIBLE);
                mStreamingTimeView.setVisibility(View.GONE);
                mFinishView.setVisibility(View.GONE);
                break;
            case STATE_STREAMING:
                mStartView.setEnabled(true);
                mStartView.setSelected(true);
                mLoadingView.setVisibility(View.GONE);
                mStreamingTimeView.setVisibility(View.VISIBLE);
                mFinishView.setVisibility(View.GONE);
                break;
            default:
                break;
        }
    }

    public void updateStreamingTimeView(long seconds, boolean isStreaming) {
        mStreamingTimeView.setText(DateUtils.formatElapsedTime(seconds));
        mStreamingTimeView.setVisibility(isStreaming ? View.VISIBLE : View.GONE);
    }
}
