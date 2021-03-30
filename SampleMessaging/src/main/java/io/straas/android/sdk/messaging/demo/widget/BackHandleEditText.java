package io.straas.android.sdk.messaging.demo.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;

public class BackHandleEditText extends androidx.appcompat.widget.AppCompatEditText {

    private OnBackPressListener mOnBackPressListener;

    public interface OnBackPressListener {
        void onBackPress();
    }

    public BackHandleEditText(Context context) {
        super(context);
    }

    public BackHandleEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public BackHandleEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean onKeyPreIme(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK &&
                event.getAction() == KeyEvent.ACTION_UP) {
            if (mOnBackPressListener != null) {
                mOnBackPressListener.onBackPress();
            }
        }
        return super.dispatchKeyEvent(event);
    }

    public void setOnBackPressListener(OnBackPressListener listener) {
        mOnBackPressListener = listener;
    }
}
