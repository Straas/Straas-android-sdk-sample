package io.straas.android.sdk.streaming.demo.screencast;

import android.content.Context;
import android.graphics.PixelFormat;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;

public abstract class OverlayLayout extends FrameLayout {

    interface Listener {
        void onMove(OverlayLayout overlayLayout);
        void onStartClick();
        void onDestroyClick();
    }

    private WindowManager.LayoutParams mLayoutParams;
    protected final Listener mListener;

    protected WindowManager.LayoutParams createLayoutParams(Context context) {
        final WindowManager.LayoutParams params = new WindowManager.LayoutParams(getLayoutWidth(context), ViewGroup.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_SYSTEM_ERROR, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS |
                WindowManager.LayoutParams.FLAG_LAYOUT_INSET_DECOR | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                PixelFormat.TRANSLUCENT);
        params.gravity = getLayoutGravity();
        return params;
    }

    protected OverlayLayout(Context context, Listener listener) {
        super(context);
        this.mListener = listener;

        inflate(context, getInflateResource(), this);
        initViews(context);
    }

    public abstract int getInflateResource();

    public abstract int getLayoutWidth(Context context);

    public abstract int getLayoutGravity();

    public abstract void initLayoutViews();

    public abstract View getMoveView();

    private void initViews(Context context) {
        mLayoutParams = createLayoutParams(context);
        initLayoutViews();
        getMoveView().setOnTouchListener(new View.OnTouchListener() {
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
                        mLayoutParams.x = initX + diffX;
                        mLayoutParams.y = initY + diffY;
                        if (mListener != null) {
                            mListener.onMove(OverlayLayout.this);
                        }
                        return true;
                }
                return false;
            }
        });
    }

    public WindowManager.LayoutParams getParams() {
        return mLayoutParams;
    }

}
