package io.straas.android.media.demo.widget.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.v7.widget.AppCompatSeekBar;
import android.util.AttributeSet;

import io.straas.android.sdk.demo.R;


public class DottedSeekBar extends AppCompatSeekBar {

    private int[] mDotsPositions = null;
    private int mDotHeight;
    private Paint mPaint = new Paint();
    private Rect[] mDotsCanvasRects = null;

    public DottedSeekBar(final Context context) {
        super(context);
        init(null);
    }

    public DottedSeekBar(final Context context, final AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public DottedSeekBar(final Context context, final AttributeSet attrs, final int defStyle) {
        super(context, attrs, defStyle);
        init(attrs);
    }

    /**
     * Initializes Seek bar extended attributes from xml
     *
     * @param attributeSet {@link AttributeSet}
     */
    private void init(final AttributeSet attributeSet) {
        mDotHeight = getContext().getResources().getDimensionPixelOffset(R.dimen.abc_seekbar_track_progress_height_material);
        // TODO: set color via attr
        mPaint.setColor(Color.YELLOW);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        calculateDotsRectOnCanvas(w);
    }

    private void calculateDotsRectOnCanvas(int w) {
        if (null != mDotsPositions && 0 != mDotsPositions.length) {
            int width = w - getPaddingLeft() - getPaddingRight();
            float step = width / (float)getMax();
            int progressCenterY = (getProgressDrawable().getBounds().top + getProgressDrawable().getBounds().bottom) >> 1;
            int halfDotHeight = mDotHeight >> 1;
            int i = 0;
            for (int position : mDotsPositions) {
                int origin = (int) (position * step + 0.5f) + (getThumb().getIntrinsicWidth() >> 1);
                mDotsCanvasRects[i].left = origin;
                mDotsCanvasRects[i].top = progressCenterY - halfDotHeight;
                mDotsCanvasRects[i].right = origin + mDotHeight;
                mDotsCanvasRects[i].bottom = progressCenterY + halfDotHeight;
                i++;
            }
        }
    }

    /**
     * @param dots to be displayed on this SeekBar
     */
    public void setDots(final int[] dots) {
        mDotsPositions = dots;
        mDotsCanvasRects = new Rect[mDotsPositions.length];
        for (int i = 0; i < mDotsCanvasRects.length; i++) {
            mDotsCanvasRects[i] = new Rect();
        }
        calculateDotsRectOnCanvas(getWidth());
        invalidate();
    }

    @Override
    protected synchronized void onDraw(final Canvas canvas) {
        super.onDraw(canvas);
        if (null != mDotsCanvasRects && 0 != mDotsCanvasRects.length) {
            // draw dots if we have ones
            for (Rect position : mDotsCanvasRects) {
                canvas.drawRect(position, mPaint);
            }
            drawThumb(canvas);
        }

    }

    void drawThumb(Canvas canvas) {
        if (getThumb() != null) {
            canvas.save();
            // Translate the padding. For the x, we need to allow the thumb to
            // draw in its extra space
            canvas.translate(getPaddingLeft() - getThumbOffset(), getPaddingTop());
            getThumb().draw(canvas);
            canvas.restore();
        }
    }


}
