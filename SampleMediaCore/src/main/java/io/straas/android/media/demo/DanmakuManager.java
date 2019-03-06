package io.straas.android.media.demo;

import android.app.Activity;
import android.app.Application;
import android.content.ComponentCallbacks;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.util.ArrayMap;
import android.support.v4.util.SimpleArrayMap;
import android.text.TextPaint;
import android.util.Log;
import android.view.View;

import io.straas.android.sdk.messaging.ChatMetadata;
import io.straas.android.sdk.messaging.ChatMode;
import io.straas.android.sdk.messaging.Message;
import io.straas.android.sdk.messaging.User;
import io.straas.android.sdk.messaging.interfaces.EventListener;
import master.flame.danmaku.controller.DrawHandler;
import master.flame.danmaku.controller.IDanmakuView;
import master.flame.danmaku.danmaku.model.BaseDanmaku;
import master.flame.danmaku.danmaku.model.DanmakuTimer;
import master.flame.danmaku.danmaku.model.IDanmakus;
import master.flame.danmaku.danmaku.model.IDisplayer;
import master.flame.danmaku.danmaku.model.android.DanmakuContext;
import master.flame.danmaku.danmaku.model.android.Danmakus;
import master.flame.danmaku.danmaku.model.android.SpannedCacheStuffer;
import master.flame.danmaku.danmaku.parser.BaseDanmakuParser;

class DanmakuManager implements Application.ActivityLifecycleCallbacks, EventListener, ComponentCallbacks {
    private final Activity mActivity;
    private final IDanmakuView mIDanmakuView;
    private int mDanmakuTextSize;

    public DanmakuManager(IDanmakuView iDanmakuView) {
        mIDanmakuView = iDanmakuView;
        mActivity = (Activity) mIDanmakuView.getView().getContext();
        mActivity.getApplication().registerComponentCallbacks(this);
        mActivity.getApplication().registerActivityLifecycleCallbacks(this);
        mDanmakuTextSize = mActivity.getResources().getDimensionPixelSize(R.dimen.danmaku_text_size);
        float shadowRadius = mActivity.getResources().getDimensionPixelSize(R.dimen.danmaku_shadow_radius);
        mIDanmakuView.setCallback(new DrawHandler.Callback() {
            @Override
            public void prepared() {
                mIDanmakuView.start();
                handleConfiguration(mActivity.getResources().getConfiguration());
            }

            @Override
            public void updateTimer(DanmakuTimer timer) {

            }

            @Override
            public void danmakuShown(BaseDanmaku danmaku) {

            }

            @Override
            public void drawingFinished() {

            }
        });

        ArrayMap<Integer, Integer> maxLinesPair = new ArrayMap<>();
        maxLinesPair.put(BaseDanmaku.TYPE_SCROLL_RL, 4);

        ArrayMap<Integer, Boolean> overlappingEnablePair = new ArrayMap<>();
        overlappingEnablePair.put(BaseDanmaku.TYPE_SCROLL_RL, true);
        overlappingEnablePair.put(BaseDanmaku.TYPE_FIX_TOP, true);
        mIDanmakuView.prepare(new BaseDanmakuParser() {
                                  @Override
                                  protected IDanmakus parse() {
                                      return new Danmakus();
                                  }
                              },
                DanmakuContext.create()
                        .setDanmakuStyle(IDisplayer.DANMAKU_STYLE_SHADOW, shadowRadius)
                        .setScrollSpeedFactor(1.5f)
                        .setDuplicateMergingEnabled(false)
                        .setMaximumVisibleSizeInScreen(-1)
                        .setCacheStuffer(new BackgroundCacheStuffer(), null)
                        .setMaximumLines(maxLinesPair)
                        .preventOverlapping(overlappingEnablePair));
    }

    public View getDanmakuView() {
        return mIDanmakuView.getView();
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {

    }

    @Override
    public void onActivityStarted(Activity activity) {
    }

    @Override
    public void onActivityResumed(Activity activity) {
        if (activity != mActivity) {
            return;
        }
        if (mIDanmakuView.isPrepared() && mIDanmakuView.isPaused()) {
            mIDanmakuView.resume();
        }
    }

    @Override
    public void onActivityPaused(Activity activity) {
        if (activity != mActivity) {
            return;
        }
        mIDanmakuView.pause();
    }

    @Override
    public void onActivityStopped(Activity activity) {
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
    }

    @Override
    public void onActivityDestroyed(Activity activity) {
        if (activity != mActivity) {
            return;
        }
        mIDanmakuView.release();
        activity.getApplication().unregisterActivityLifecycleCallbacks(this);
    }

    @Override
    public void onConnected() {

    }

    @Override
    public void onConnectFailed(Exception error) {
        Log.e("chatroom", error.toString());
    }

    @Override
    public void onError(Exception error) {
        Log.e("chatroom", error.toString());
    }

    @Override
    public void onAggregatedDataAdded(SimpleArrayMap<String, Integer> map) {
        Log.d("onAggregatedData", map.toString());
    }

    @Override
    public void onRawDataAdded(Message message) {
        Log.d("onRawData", message.getRawData().getJsonText());
    }

    @Override
    public void onDisconnected() {

    }

    @Override
    public void onMessageAdded(Message message) {
        if (!mIDanmakuView.isPrepared() || mIDanmakuView.isPaused()) {
            return;
        }
        final BaseDanmaku danmaku = mIDanmakuView.getConfig().mDanmakuFactory
                .createDanmaku(BaseDanmaku.TYPE_SCROLL_RL);
        danmaku.text = message.getText();
        danmaku.isLive = true;
        danmaku.setTime(mIDanmakuView.getCurrentTime());
        danmaku.textSize = mDanmakuTextSize;
        danmaku.textColor = Color.WHITE;
        mIDanmakuView.addDanmaku(danmaku);
    }

    @Override
    public void onMessageRemoved(String messageId) {

    }

    @Override
    public void onMessageFlushed() {

    }

    @Override
    public void onUserJoined(User[] users) {

    }

    @Override
    public void onUserUpdated(User[] users) {

    }

    @Override
    public void onUserLeft(Integer[] userLabels) {

    }

    @Override
    public void userCount(int userCount) {

    }

    @Override
    public void onChatWriteModeChanged(ChatMode chatMode) {

    }

    @Override
    public void onInputIntervalChanged(int inputInterval) {

    }

    @Override
    public void onPinnedMessageUpdated(@Nullable Message pinnedMessage) {
        
    }

    @Override
    public void onMetadataUpdated(SimpleArrayMap<String, ChatMetadata> simpleArrayMap) {

    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        handleConfiguration(newConfig);
    }

    private void handleConfiguration(Configuration config) {
        if (config.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            onActivityResumed(mActivity);
        } else {
            onActivityPaused(mActivity);
            mIDanmakuView.removeAllLiveDanmakus();
        }
    }

    @Override
    public void onLowMemory() {

    }

    private class BackgroundCacheStuffer extends SpannedCacheStuffer {
        @Override
        public void measure(BaseDanmaku danmaku, TextPaint paint, boolean fromWorkerThread) {
            String measureText = danmaku.text.toString();
            float measureTextWidth = paint.measureText(measureText);

            if (measureTextWidth > mIDanmakuView.getWidth()) {
                int charLength = (int) (mIDanmakuView.getWidth() /
                        (measureTextWidth / measureText.length()));
                danmaku.text = measureText.substring(0, charLength) + '…';
            }

            super.measure(danmaku, paint, fromWorkerThread);
        }
    }
}
