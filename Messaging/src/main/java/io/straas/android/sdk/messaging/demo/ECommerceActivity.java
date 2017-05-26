package io.straas.android.sdk.messaging.demo;


import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.DrawableRes;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.util.SimpleArrayMap;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatImageView;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;

import java.util.Random;

import io.straas.android.sdk.demo.R;
import io.straas.android.sdk.messaging.ChatMode;
import io.straas.android.sdk.messaging.ChatroomManager;
import io.straas.android.sdk.messaging.ChatroomState;
import io.straas.android.sdk.messaging.Message;
import io.straas.android.sdk.messaging.User;
import io.straas.android.sdk.messaging.demo.widget.BackHandleEditText;
import io.straas.android.sdk.messaging.demo.widget.BackHandleEditText.OnBackPressListener;
import io.straas.android.sdk.messaging.interfaces.EventListener;
import io.straas.android.sdk.messaging.ui.ChatroomOutputView;
import io.straas.sdk.demo.MemberIdentity;
import tyrantgit.widget.HeartLayout;

public class ECommerceActivity extends AppCompatActivity {

    private static final String CHATROOM_NAME = "test_chatroom";

    private static final String LIKE = "like";
    private static final String LOVE = "love";
    private static final String XD = "XD";

    private ChatroomOutputView mChatroomOutputView;
    private HeartLayout mHeartLayout;
    private ViewGroup mInputBar;
    private ViewGroup mControlBar;
    private BackHandleEditText mInput;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_e_commerce);

        getWindow().setBackgroundDrawable(ContextCompat.getDrawable(ECommerceActivity.this,
                R.drawable.e_commerce_background));
        mHeartLayout = (HeartLayout) findViewById(R.id.heart);
        mInputBar = (ViewGroup) findViewById(R.id.inputBar);
        mControlBar = (ViewGroup) findViewById(R.id.controlBar);
        mInput = (BackHandleEditText) findViewById(android.R.id.edit);
        mInput.setOnBackPressListener(mOnBackPressListener);
        findViewById(R.id.btnSend).setOnClickListener(mOnSendListener);

        mChatroomOutputView = (ChatroomOutputView) findViewById(R.id.chat_room);
        mChatroomOutputView.setEventListener(mEventListener);
        mChatroomOutputView.setMessageItemCustomView(R.layout.message_item_e_commerce);
        mChatroomOutputView.setVerticalScrollBarEnabled(false);
        mChatroomOutputView.setMsgDividerColor(ContextCompat.getColor(this, android.R.color.transparent));
        mChatroomOutputView.connect(CHATROOM_NAME, MemberIdentity.ME, false);
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        mChatroomOutputView.disconnect();
    }

    public void showKeyboard(View view) {
        mInputBar.setVisibility(View.VISIBLE);
        mInput.requestFocus();
        InputMethodManager inputManager = (InputMethodManager)ECommerceActivity.this.getSystemService(
                Context.INPUT_METHOD_SERVICE);
        inputManager.showSoftInput(mInput, InputMethodManager.SHOW_FORCED);
    }

    public void hideKeyboard() {
        mInput.clearFocus();
        mInputBar.setVisibility(View.GONE);
        InputMethodManager inputManager = (InputMethodManager)ECommerceActivity.this.getSystemService(
                Context.INPUT_METHOD_SERVICE);
        inputManager.hideSoftInputFromWindow(mInput.getWindowToken(), 0);
    }

    public void love(View view) {
        ChatroomManager manager = mChatroomOutputView.getChatroomManager();
        if (manager.getChatroomState() == ChatroomState.CONNECTED) {
            int i = new Random().nextInt(3);
            switch (i) {
                case 0:
                    startAnimation(R.drawable.ic_emoji_xd, 1);
                    manager.sendAggregatedData(XD);
                    break;
                case 1:
                    startAnimation(R.drawable.ic_emoji_heart, 1);
                    manager.sendAggregatedData(LOVE);
                    break;
                case 2:
                    startAnimation(R.drawable.ic_emoji_like, 1);
                    manager.sendAggregatedData(LIKE);
                    break;
            }
        }
    }

    private void startAnimation(@DrawableRes int id, int count) {
        int i = 0;
        while (i < count) {
            ImageView imageView;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                imageView = new ImageView(this);
            } else {
                imageView = new AppCompatImageView(this);
            }
            imageView.setImageResource(id);
            mHeartLayout.getAnimator().start(imageView, mHeartLayout);
            i++;
        }
    }

    private OnBackPressListener mOnBackPressListener = new OnBackPressListener() {
        @Override
        public void onBackPress() {
            hideKeyboard();
        }
    };

    private OnClickListener mOnSendListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            String msg = mInput.getText().toString();
            if (!TextUtils.isEmpty(msg)) {
                mChatroomOutputView.getChatroomManager().sendMessage(msg);
                mInput.setText("");
                hideKeyboard();
            }
        }
    };

    private EventListener mEventListener = new EventListener() {
        @Override
        public void onConnectFailed(Exception error) {

        }


        @Override
        public void onError(Exception error) {

        }

        @Override
        public void onAggregatedDataAdded(SimpleArrayMap<String, Integer> map) {
            if (map.containsKey(XD)) {
                startAnimation(R.drawable.ic_emoji_xd, map.get(XD));
            }
            if (map.containsKey(LIKE)) {
                startAnimation(R.drawable.ic_emoji_like, map.get(LIKE));
            }
            if (map.containsKey(LOVE)) {
                startAnimation(R.drawable.ic_emoji_heart, map.get(LOVE));
            }
        }

        @Override
        public void onConnected() {
            mControlBar.setVisibility(View.VISIBLE);
        }

        @Override
        public void onRawDataAdded(Message message) {

        }

        @Override
        public void onDisconnected() {

        }

        @Override
        public void onChatWriteModeChanged(ChatMode chatMode) {

        }

        @Override
        public void onInputIntervalChanged(int interval) {

        }

        @Override
        public void onMessageAdded(Message message) {

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
    };
}

