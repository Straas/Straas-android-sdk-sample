package io.straas.android.sdk.streaming.demo.screencast;

import android.content.Context;
import android.graphics.PixelFormat;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;

import io.straas.android.sdk.streaming.demo.Utils;

import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

public abstract class OverlayLayout extends FrameLayout {

    interface Listener {
        void onMove(OverlayLayout overlayLayout);
        void onClick(final View v);
    }

    private WindowManager.LayoutParams mLayoutParams;
    protected final Listener mListener;

    protected WindowManager.LayoutParams createLayoutParams() {
        int alertWindowType = Utils.isAndroidOreoOrAbove() ?
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY :
                WindowManager.LayoutParams.TYPE_SYSTEM_ERROR;
        final WindowManager.LayoutParams params = new WindowManager.LayoutParams(WRAP_CONTENT, WRAP_CONTENT,
                alertWindowType, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS |
                WindowManager.LayoutParams.FLAG_LAYOUT_INSET_DECOR | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                PixelFormat.TRANSLUCENT);
        params.gravity = getLayoutGravity();
        params.y = getVerticalMargin(params.gravity);
        return params;
    }

    private int getVerticalMargin(int gravity) {
        // if top gravity then align to the height of status bar
        if ((gravity & Gravity.TOP) == Gravity.TOP) {
            int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
            if (resourceId > 0) {
                return getResources().getDimensionPixelSize(resourceId);
            }
        }
        return 0;
    }

    protected OverlayLayout(Context context, Listener listener) {
        super(context);
        this.mListener = listener;

        inflate(context, getInflateResource(), this);
        initViews();
    }

    public abstract int getInflateResource();

    public abstract int getLayoutGravity();

    public abstract void initLayoutViews();

    public abstract View getMoveView();

    private void initViews() {
        mLayoutParams = createLayoutParams();
        initLayoutViews();
        getMoveView().setOnTouchListener(new OnTouchListener() {
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
                        int diffX = (mLayoutParams.gravity & Gravity.LEFT) == Gravity.LEFT ? (x - initTouchX) : (initTouchX - x);
                        int diffY = (mLayoutParams.gravity & Gravity.TOP) == Gravity.TOP  ? (y - initTouchY) : (initTouchY - y);
                        if (!isValidYRange(initY + diffY)) {
                            return true;
                        }

                        mLayoutParams.x = initX + diffX;
                        mLayoutParams.y = initY + diffY;
                        if (mListener != null) {
                            mListener.onMove(OverlayLayout.this);
                        }
                        return true;
                }
                return false;
            }

            private boolean isValidYRange(int newY) {
                return newY >= 0;
            }
        });
    }

    public WindowManager.LayoutParams getParams() {
        return mLayoutParams;
    }

}
