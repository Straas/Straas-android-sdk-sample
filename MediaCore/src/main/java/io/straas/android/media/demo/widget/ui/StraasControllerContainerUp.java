package io.straas.android.media.demo.widget.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import io.straas.android.sdk.mediacore.demo.R;


public class StraasControllerContainerUp extends FrameLayout {

    public StraasControllerContainerUp(Context context) {
        this(context, null);
    }

    public StraasControllerContainerUp(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public StraasControllerContainerUp(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }


    private void init() {
        ViewGroup upContainer = (ViewGroup) View.inflate(getContext(), R.layout.straas_controller_container_up, null);
        addView(upContainer);
    }
}
