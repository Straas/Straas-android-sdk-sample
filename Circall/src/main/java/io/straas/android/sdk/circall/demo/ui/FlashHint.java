package io.straas.android.sdk.circall.demo.ui;

import android.content.Context;
import android.databinding.BindingAdapter;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

import java.lang.ref.WeakReference;

import static io.straas.android.sdk.circall.demo.ui.FlashHint.FlashHandler.FLASH_INTERVAL;
import static io.straas.android.sdk.circall.demo.ui.FlashHint.FlashHandler.MESSAGE_FLASH;

public class FlashHint extends android.support.v7.widget.AppCompatImageView {

    private FlashHandler mHandler;
    public boolean mFlashEnabled;

    static class FlashHandler extends Handler {
        static final long FLASH_INTERVAL = 1000;

        static final int MESSAGE_FLASH = 0;

        private final WeakReference<FlashHint> mRecordingFlashHint;

        FlashHandler(FlashHint recordingFlashHint) {
            super();
            mRecordingFlashHint = new WeakReference<>(recordingFlashHint);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            FlashHint flashHint = mRecordingFlashHint.get();
            if (flashHint == null) {
                return;
            }
            switch (msg.what) {
                case MESSAGE_FLASH:
                    flashHint.setVisibility(
                            flashHint.getVisibility() == VISIBLE
                            ? INVISIBLE
                            : VISIBLE);
                    removeMessages(MESSAGE_FLASH);
                    sendEmptyMessageDelayed(MESSAGE_FLASH, FLASH_INTERVAL);
                    break;
                default:
                    break;
            }
        }
    }

    public FlashHint(Context context) {
        this(context, null);
    }

    public FlashHint(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FlashHint(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setVisibility(View.GONE);
        mHandler = new FlashHandler(this);
    }

    public void setFlash(boolean enabled) {
        mFlashEnabled = enabled;
        if (mFlashEnabled) {
            bringToFront();
            mHandler.removeMessages(MESSAGE_FLASH);
            mHandler.sendEmptyMessageDelayed(MESSAGE_FLASH, FLASH_INTERVAL);
        } else {
            setVisibility(View.INVISIBLE);
            mHandler.removeMessages(MESSAGE_FLASH);
        }
    }

    @BindingAdapter("app:flash")
    public static void setFlash(FlashHint flashHint, boolean enabled) {
        flashHint.setFlash(enabled);
    }
}
