package io.straas.android.sdk.demo.qrcode;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import androidx.annotation.ColorInt;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import android.util.AttributeSet;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;

public class ViewFinderView extends View {
    private final Paint mPaint;
    @ColorInt
    private final int mMaskColor;
    private final int mScanHintPadding;
    private final int mScanMaskSize;
    private final String mScanHint;
    private Rect mFramingRect;

    public ViewFinderView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mMaskColor = ContextCompat.getColor(context, R.color.viewfinder_mask);
        mScanHint = getResources().getString(R.string.hint_scan);
        mScanMaskSize = getResources().getDimensionPixelSize(R.dimen.scan_qrcode_mask_size);
        mScanHintPadding = getResources().getDimensionPixelSize(R.dimen.scan_qrcode_text_padding);
        int textSize = getResources().getDimensionPixelSize(R.dimen.scan_qrcode_text_size);
        mPaint.setTextAlign(Paint.Align.CENTER);
        mPaint.setTextSize(textSize);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        Rect frame = getFramingRect();
        if (frame == null) {
            return;
        }
        int width = canvas.getWidth();
        int height = canvas.getHeight();

        // Draw the exterior (i.e. outside the framing rect) darkened
        // copy from https://github.com/zxing/zxing/blob/master/android/src/com/google/zxing/client/android/ViewfinderView.java#L96
        mPaint.setColor(mMaskColor);
        canvas.drawRect(0, 0, width, frame.top, mPaint);
        canvas.drawRect(0, frame.top, frame.left, frame.bottom + 1, mPaint);
        canvas.drawRect(frame.right + 1, frame.top, width, frame.bottom + 1, mPaint);
        canvas.drawRect(0, frame.bottom + 1, width, height, mPaint);

        mPaint.setColor(Color.WHITE);
        canvas.drawText(mScanHint, canvas.getWidth() / 2, frame.bottom + mScanHintPadding, mPaint);
    }

    /**
     * Modify from https://github.com/zxing/zxing/blob/master/android/src/com/google/zxing/client/android/camera/CameraManager.java#L215
     */
    private synchronized Rect getFramingRect() {
        if (mFramingRect == null) {
            Point screenResolution = getAppUsableScreenSize(getContext());
            if (screenResolution == null) {
                // Called early, before init even finished
                return null;
            }

            int leftOffset = (screenResolution.x - mScanMaskSize) >> 1;
            int topOffset = (screenResolution.y - mScanMaskSize) >> 1;
            mFramingRect = new Rect(leftOffset, topOffset, leftOffset + mScanMaskSize, topOffset + mScanMaskSize);
        }
        return mFramingRect;
    }

    private static Point getAppUsableScreenSize(Context context) {
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = windowManager.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        return size;
    }
}
