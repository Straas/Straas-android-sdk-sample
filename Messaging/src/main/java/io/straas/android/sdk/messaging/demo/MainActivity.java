package io.straas.android.sdk.messaging.demo;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import io.straas.android.sdk.base.credential.CredentialFailReason;
import io.straas.android.sdk.base.interfaces.OnResultListener;
import io.straas.android.sdk.messaging.ChatMode;
import io.straas.android.sdk.messaging.ChatroomManager;
import io.straas.android.sdk.messaging.Message;
import io.straas.android.sdk.messaging.MessagingError;
import io.straas.android.sdk.messaging.User;
import io.straas.android.sdk.messaging.interfaces.EventListener;
import io.straas.android.sdk.messaging.ui.ChatroomInputView;
import io.straas.android.sdk.messaging.ui.ChatroomOutputView;
import io.straas.android.sdk.messaging.ui.interfaces.CredentialAuthorizeListener;
import io.straas.android.sdk.messaging.ui.interfaces.SignInListener;
import io.straas.sdk.demo.MemberIdentity;

public class MainActivity extends AppCompatActivity {

    private static final String CHATROOM_NAME = "test_chatroom";
    private ChatroomOutputView mChatroomOutputView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mChatroomOutputView = (ChatroomOutputView) findViewById(R.id.chat_room);
        mChatroomOutputView.setChatroomInputView((ChatroomInputView) findViewById(android.R.id.inputArea));
        mChatroomOutputView.setCredentialAuthorizeListener(mCredentialAuthorizeListener);
        mChatroomOutputView.setEventListener(mEventListener);
        mChatroomOutputView.setSendMessageListener(mSendMessageListener);
        mChatroomOutputView.setSignInListener(mSignInListener);

        mChatroomOutputView.connect(CHATROOM_NAME, MemberIdentity.ME);
    }

    private CredentialAuthorizeListener mCredentialAuthorizeListener =
            new CredentialAuthorizeListener() {
                @Override
                public void onSuccess(ChatroomManager chatRoomManager) {

                }

                @Override
                public void onFailure(CredentialFailReason credentialFailReason) {

                }
            };

    private EventListener mEventListener = new EventListener() {
        @Override
        public void onConnected() {

        }

        @Override
        public void onConnectFailed(MessagingError messagingError) {

        }

        @Override
        public void onError(MessagingError messagingError) {

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

    private OnResultListener<Void, MessagingError> mSendMessageListener = new OnResultListener<Void, MessagingError>() {
        @Override
        public void onSuccess(Void aVoid) {

        }

        @Override
        public void onFailure(MessagingError messagingError) {

        }
    };

    private SignInListener mSignInListener = new SignInListener() {
        @Override
        public void signIn() {

        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mChatroomOutputView.disconnect();
    }
}

