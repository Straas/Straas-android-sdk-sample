package io.straas.android.media.demo.widget.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;

final class VideoRelativeLayout extends RelativeLayout {
    private VideoSubtitleView mVideoSubtitleView;

    public VideoRelativeLayout(Context context) {
        this(context, null);
    }

    public VideoRelativeLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public VideoRelativeLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        float viewAspectRatio = (height != 0) ? (float) width / height : 1;

        // currently we only allow it keep 16:9, no fullscreen control
        float aspectRatio = 1.778f;
        float maxAspectRatioDeformationFraction = 0.01f;
        float aspectDeformation = aspectRatio / viewAspectRatio - 1;
        if (Math.abs(aspectDeformation) <= maxAspectRatioDeformationFraction) {
            // We're within the allowed tolerance.
            return;
        }

        if (aspectDeformation > 0) {
            height = (int) (width / aspectRatio);
        } else {
            width = (int) (height * aspectRatio);
        }
        super.onMeasure(MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY));
    }

    /* package */ VideoSubtitleView getVideoSubtitleView() {
        return mVideoSubtitleView;
    }

    private void initView() {
        mVideoSubtitleView = new VideoSubtitleView(getContext());

        LayoutParams params = new LayoutParams(MATCH_PARENT, MATCH_PARENT);
        params.addRule(RelativeLayout.CENTER_IN_PARENT, TRUE);

        addView(mVideoSubtitleView, params);
    }

}
