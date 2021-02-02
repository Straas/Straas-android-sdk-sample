package io.straas.android.sdk.messaging.demo;


import android.content.*;
import android.os.*;
import android.text.*;
import android.util.*;
import android.view.*;
import android.view.View.*;
import android.view.inputmethod.*;
import android.widget.*;

import com.google.android.gms.tasks.*;

import java.util.*;

import androidx.annotation.*;
import androidx.appcompat.app.*;
import androidx.appcompat.widget.Toolbar;
import androidx.appcompat.widget.*;
import androidx.collection.*;
import androidx.core.content.*;
import io.straas.android.sdk.demo.common.*;
import io.straas.android.sdk.messaging.Message;
import io.straas.android.sdk.messaging.*;
import io.straas.android.sdk.messaging.demo.widget.*;
import io.straas.android.sdk.messaging.demo.widget.BackHandleEditText.*;
import io.straas.android.sdk.messaging.interfaces.EventListener;
import io.straas.android.sdk.messaging.ui.*;
import tyrantgit.widget.*;

public class ECommerceActivity extends AppCompatActivity {

    private static final String CHATROOM_NAME = "test_chatroom";

    private static final String LIKE = "like";
    private static final String LOVE = "love";
    private static final String XD = "XD";
    private static final int MAX_NICKNAME_LENGTH = 15;

    private ChatroomOutputView mChatroomOutputView;
    private HeartLayout mHeartLayout;
    private ViewGroup mInputBar;
    private ViewGroup mControlBar;
    private BackHandleEditText mInput;

    private int mDialogPadding;
    private boolean mEverSetName;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_e_commerce);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        TypedValue outValue = new TypedValue();
        getTheme().resolveAttribute(R.attr.dialogPreferredPadding, outValue, true);
        mDialogPadding = getResources().getDimensionPixelSize(outValue.resourceId);

        getWindow().setBackgroundDrawable(ContextCompat.getDrawable(ECommerceActivity.this,
                R.drawable.e_commerce_background));
        mHeartLayout = findViewById(R.id.heart);
        mInputBar = findViewById(R.id.inputBar);
        mControlBar = findViewById(R.id.controlBar);
        mInput = findViewById(android.R.id.edit);
        mInput.setOnBackPressListener(mOnBackPressListener);
        findViewById(R.id.btnSend).setOnClickListener(mOnSendListener);

        mChatroomOutputView = findViewById(R.id.chat_room);
        mChatroomOutputView.setEventListener(mEventListener);
        mChatroomOutputView.setMessageItemCustomView(R.layout.message_item_e_commerce);
        mChatroomOutputView.setVerticalScrollBarEnabled(false);
        mChatroomOutputView.setMsgDividerColor(ContextCompat.getColor(this, android.R.color.transparent));
        mChatroomOutputView.setPinnedMessageCustomView(null);
        mChatroomOutputView.connect(CHATROOM_NAME, MemberIdentity.ME);
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        mChatroomOutputView.disconnect();
    }

    public void triggerInput(View view) {
        User currentUser = mChatroomOutputView.getChatroomManager().getCurrentUser();
        switch (mChatroomOutputView.getChatroomManager().getChatMode()) {
            case ALL:
                if (currentUser.isGuest() && !mEverSetName) {
                    showSetUserNameDialog();
                    return;
                }
                break;
            case LOGIN:
                if (currentUser.isGuest()) {
                    Toast.makeText(this, R.string.hint_sign_in, Toast.LENGTH_SHORT).show();
                    return;
                }
                break;
            case ANCHOR:
                Role role = currentUser.getRole();
                if (role == Role.NORMAL || role == Role.BLOCKED || role == Role.UNKNOWN) {
                    Toast.makeText(this, R.string.hint_no_permission, Toast.LENGTH_SHORT).show();
                    return;
                }
                break;
        }
        showKeyboard();
    }

    public void showKeyboard() {
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

    public void sendAggregatedData(View view) {
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

    private void showSetUserNameDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(ECommerceActivity.this);
        builder.setTitle(getResources().getString(R.string.enter_nickname));

        final EditText editText = new AppCompatEditText(ECommerceActivity.this);
        InputFilter[] filters = new InputFilter[1];
        filters[0] = new InputFilter.LengthFilter(MAX_NICKNAME_LENGTH);
        editText.setFilters(filters);
        editText.setInputType(InputType.TYPE_TEXT_VARIATION_SHORT_MESSAGE);
        editText.setImeOptions(EditorInfo.IME_ACTION_DONE);

        builder.setPositiveButton(getResources().getString(R.string.confirm), null);
        builder.setNegativeButton(getResources().getString(R.string.cancel), null);

        final AlertDialog alertDialog = builder.create();
        alertDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        alertDialog.setView(editText, mDialogPadding, mDialogPadding, mDialogPadding, 0);
        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                final Button confirmButton = alertDialog.getButton(DialogInterface.BUTTON_POSITIVE);
                final Button cancelButton = alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE);
                confirmButton.setEnabled(false);
                editText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                    @Override
                    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                        if (actionId == v.getImeOptions()) {
                            updateNickname(editText.getText().toString());
                            alertDialog.dismiss();
                        }
                        return false;
                    }
                });
                editText.addTextChangedListener(new AfterTextChangedWatcher() {
                    @Override
                    public void afterTextChanged(Editable s) {
                        confirmButton.setEnabled(s.length() != 0);
                    }
                });
                confirmButton.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        updateNickname(editText.getText().toString());
                        alertDialog.dismiss();
                    }
                });
                cancelButton.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        alertDialog.dismiss();
                    }
                });
            }
        });
        alertDialog.show();
    }

    private void updateNickname(String nickname) {
        mChatroomOutputView.getChatroomManager().updateNickname(nickname)
            .addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    mEverSetName = true;
                }
        });
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
        public void onPinnedMessageUpdated(@Nullable Message pinnedMessage) {

        }

        @Override
        public void onMetadataUpdated(SimpleArrayMap<String, ChatMetadata> simpleArrayMap) {

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

    private static abstract class AfterTextChangedWatcher implements TextWatcher {

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

    }
}

