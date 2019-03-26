package io.straas.android.media.demo.widget.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import io.straas.android.media.demo.R;

public class StraasControllerContainerBottom extends FrameLayout {

    public StraasControllerContainerBottom(Context context) {
        this(context, null);
    }

    public StraasControllerContainerBottom(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public StraasControllerContainerBottom(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }


    private void init() {
        ViewGroup bottomContainer = (ViewGroup) View.inflate(getContext(), R.layout.straas_controller_container_bottom, null);
        addView(bottomContainer);
    }
}
