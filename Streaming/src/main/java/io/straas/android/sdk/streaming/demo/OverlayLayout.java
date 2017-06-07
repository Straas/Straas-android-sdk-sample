package io.straas.android.sdk.streaming.demo;

import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Handler;
import android.text.format.DateUtils;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.TextView;

import io.straas.android.sdk.demo.R;

final class OverlayLayout extends FrameLayout implements View.OnClickListener {

    interface Listener {
        void onMove();
        void onStartClick();
        void onDestroyClick();
    }

    private WindowManager.LayoutParams mLayoutParams;
    private View mMoveView;
    private View mFinishView;
    private View mStartView;
    private View mLoadingView;
    private TextView mStreamingTimeView;
    private final Listener mListener;

    static OverlayLayout create(Context context, Listener listener) {
        return new OverlayLayout(context, listener);
    }

    private WindowManager.LayoutParams createLayoutParams(Context context) {
        int width = context.getResources().getDimensionPixelSize(R.dimen.overlay_layout_width);
        final WindowManager.LayoutParams params = new WindowManager.LayoutParams(width, ViewGroup.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_SYSTEM_ERROR, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS |
                WindowManager.LayoutParams.FLAG_LAYOUT_INSET_DECOR | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                PixelFormat.TRANSLUCENT);
        params.gravity = Gravity.TOP | Gravity.LEFT;
        return params;
    }

    private OverlayLayout(Context context, Listener listener) {
        super(context);
        this.mListener = listener;

        inflate(context, R.layout.overlay_layout, this);
        initViews(context);
    }

    private void initViews(Context context) {
        mLayoutParams = createLayoutParams(context);
        mMoveView = findViewById(R.id.screencast_overlay_move);;
        mFinishView = findViewById(R.id.screencast_overlay_finish);
        mStartView = findViewById(R.id.screencast_overlay_start);;
        mLoadingView = findViewById(R.id.screencast_overlay_loading);;
        mStreamingTimeView = (TextView) findViewById(R.id.screencast_overlay_elapsed_time);;

        mMoveView.setOnTouchListener(new View.OnTouchListener() {
            private int initX, initY;
            private int initTouchX, initTouchY;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int x = (int) event.getRawX();
                int y = (int) event.getRawY();

                switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    initX = mLayoutParams.x;
                    initY = mLayoutParams.y;
                    initTouchX = x;
                    initTouchY = y;
                    return true;

                case MotionEvent.ACTION_UP:
                    return true;

                case MotionEvent.ACTION_MOVE:
                    mLayoutParams.x = initX + (x - initTouchX);
                    mLayoutParams.y = initY + (y - initTouchY);
                    if (mListener != null) {
                        mListener.onMove();
                    }
                    return true;
                }
                return false;
            }
        });
        mFinishView.setOnClickListener(this);
        mStartView.setOnClickListener(this);
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

    public WindowManager.LayoutParams getParams() {
        return mLayoutParams;
    }

    public void setStartViewEnabled(boolean enabled) {
         mStartView.setEnabled(enabled);
    }

    public void setStartViewSelected(boolean selected) {
         mStartView.setSelected(selected);
    }

    public void updateStreamingStatusOnUiThread(Handler uiHandler, final boolean selected) {
        uiHandler.post(new Runnable() {
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
    }

    public void updateStreamingTimeView(long seconds, boolean isStreaming) {
        mStreamingTimeView.setText(DateUtils.formatElapsedTime(seconds));
        mStreamingTimeView.setVisibility(isStreaming ? View.VISIBLE : View.GONE);
    }
}
