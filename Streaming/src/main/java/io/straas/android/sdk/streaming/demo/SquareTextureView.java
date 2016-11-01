package io.straas.android.sdk.streaming.demo;

import android.content.Context;
import android.util.AttributeSet;
import android.view.TextureView;

public class SquareTextureView extends TextureView {

    public SquareTextureView(Context context) {
        super(context);
    }

    public SquareTextureView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SquareTextureView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);

        if (width > height) {

        } else {
            height = width;
        }
        width = MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY);
        height = MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY);

        super.onMeasure(width, height);
    }
}
