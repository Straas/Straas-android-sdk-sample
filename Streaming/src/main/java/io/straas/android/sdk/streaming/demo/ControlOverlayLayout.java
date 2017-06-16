package io.straas.android.sdk.streaming.demo;

import android.content.Context;
import android.text.format.DateUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import io.straas.android.sdk.demo.R;

final class ControlOverlayLayout extends OverlayLayout implements View.OnClickListener {

    private View mMoveView;
    private View mFinishView;
    private View mStartView;
    private ViewGroup mStatusLayout;
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

    @Override
    public int getLayoutWidth(Context context) {
        return ViewGroup.LayoutParams.WRAP_CONTENT;
    }

    public int getLayoutGravity() {
        return Gravity.TOP | Gravity.LEFT;
    }

    @Override
    public void initLayoutViews() {
        mMoveView = findViewById(R.id.screencast_overlay_move);

        mFinishView = findViewById(R.id.screencast_overlay_finish);
        mStartView = findViewById(R.id.screencast_overlay_start);
        mStatusLayout = (ViewGroup) findViewById(R.id.screencast_overlay_status_layout);
        mLoadingView = findViewById(R.id.screencast_overlay_loading);
        mStreamingTimeView = (TextView) findViewById(R.id.screencast_overlay_elapsed_time);

        mFinishView.setOnClickListener(this);
        mStartView.setOnClickListener(this);
    }

    @Override
    public View getMoveView() {
        return mMoveView;
    }

    @Override
    public void onClick(final View v) {
        switch (v.getId()) {
        case R.id.screencast_overlay_finish:
            if (mListener != null) {
                mListener.onDestroyClick();
            }
            break;
        case R.id.screencast_overlay_start:
            if (mListener != null) {
                mListener.onStartClick();
            }
            break;

        default:
            break;
        }
    }

    public void setStartViewEnabled(boolean enabled) {
         mStartView.setEnabled(enabled);
    }

    public void setStartViewSelected(boolean selected) {
         mStartView.setSelected(selected);
    }

    public void updateStreamingStatusOnUiThread(final boolean selected) {
        post(new Runnable() {
            @Override
            public void run() {
                mStartView.setSelected(selected);
                mStartView.setEnabled(true);
                updateLoadingView(false);
            }
        });
    }

    public void updateLoadingView(boolean isLoading) {
        mLoadingView.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        updateStatusLayoutVisibility();
    }

    public void updateStreamingTimeView(long seconds, boolean isStreaming) {
        mStreamingTimeView.setText(DateUtils.formatElapsedTime(seconds));
        mStreamingTimeView.setVisibility(isStreaming ? View.VISIBLE : View.GONE);
        updateStatusLayoutVisibility();
    }

    private void updateStatusLayoutVisibility() {
        boolean visible = (mLoadingView.getVisibility() == View.VISIBLE) || (mStreamingTimeView.getVisibility() == View.VISIBLE);
        mStatusLayout.setVisibility(visible ? VISIBLE : GONE);
    }
}
