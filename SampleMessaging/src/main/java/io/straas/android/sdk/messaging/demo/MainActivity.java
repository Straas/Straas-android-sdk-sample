package io.straas.android.sdk.messaging.demo;

import android.os.*;
import android.widget.*;

import com.google.android.gms.tasks.*;

import androidx.annotation.*;
import androidx.appcompat.app.*;
import androidx.collection.*;
import io.straas.android.sdk.authentication.identity.*;
import io.straas.android.sdk.demo.common.*;
import io.straas.android.sdk.messaging.Message;
import io.straas.android.sdk.messaging.*;
import io.straas.android.sdk.messaging.interfaces.*;
import io.straas.android.sdk.messaging.ui.*;
import io.straas.android.sdk.messaging.ui.interfaces.*;

public class MainActivity extends AppCompatActivity {

    private static final String CHATROOM_NAME = "test_chatroom";
    private ChatroomOutputView mChatroomOutputView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_messaging);

        mChatroomOutputView = findViewById(R.id.chat_room);
        mChatroomOutputView.setCredentialAuthorizeListener(mCredentialAuthorizeListener);
        mChatroomOutputView.setEventListener(mEventListener);

        ChatroomManager.ChatroomConfig.Builder configBuilder = new ChatroomManager.ChatroomConfig.Builder();
        onCustomizeChatroomConfig(configBuilder);
        // If you don't need the function of custom domain, you could create an empty ChatroomConfig
        // or use the method which doesn't need a ChatroomConfig: ChatroomManager#connect(String, Identity)
        mChatroomOutputView.connect(CHATROOM_NAME, MemberIdentity.ME, configBuilder.build());
    }

    protected void onCustomizeChatroomConfig(ChatroomManager.ChatroomConfig.Builder builder) {

    }

    protected void connect(ChatroomOutputView chatroomOutputView, String chatroomName, Identity identity) {
        chatroomOutputView.connect(chatroomName, identity);
    }

    private CredentialAuthorizeListener mCredentialAuthorizeListener =
            new CredentialAuthorizeListener() {
                @Override
                public void onSuccess(ChatroomManager chatRoomManager) {
                    final ChatroomInputView chatroomInputView = findViewById(android.R.id.inputArea);
                    chatroomInputView.setChatroomManager(chatRoomManager);
                    chatroomInputView.setSendMessageListener(mSendMessageListener);
                    chatroomInputView.setSignInListener(mSignInListener);
                }

                @Override
                public void onFailure(Exception error) {
                    Toast.makeText(MainActivity.this, "Validation fails, error: " + error,
                            Toast.LENGTH_LONG).show();
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

        }

        @Override
        public void onConnected() {
            // if you enable ChatroomManager.WITH_DATA_CHANNEL flag
            //Ppap[] PPAP = new Ppap[2];
            //PPAP[0] = new Ppap();
            //PPAP[0].mIhaveA = new String[]{"pen", "apple"};
            //PPAP[0].mUh = "Apple Pen!";
            //
            //PPAP[1] = new Ppap();
            //PPAP[1].mIhaveA = new String[]{"pen", "pineapple"};
            //PPAP[1].mUh = "Pineapple Pen!";
            //
            //mChatroomOutputView.getChatroomManager().sendRawData(
            //        new RawData.Builder().setObject(PPAP).build());
        }

        @Override
        public void onRawDataAdded(Message message) {
            // if you enable ChatroomManager.WITH_DATA_CHANNEL flag
            //try {
            //    Ppap[] PPAP = message.getRawData().getJsonTextAsData(Ppap[].class);
            //    Log.d("raw data", String.format("I have a %s, I have an %s, Uh! %s",
            //            PPAP[0].mIhaveA[0], PPAP[0].mIhaveA[1], PPAP[0].mUh));
            //    Log.d("raw data", String.format("I have a %s, I have an %s, Uh! %s",
            //            PPAP[1].mIhaveA[0], PPAP[1].mIhaveA[1], PPAP[1].mUh));
            //} catch (IOException e) {
            //    e.printStackTrace();
            //} catch (JSONException e) {
            //    e.printStackTrace();
            //}
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

    private OnCompleteListener<Void> mSendMessageListener = new OnCompleteListener<Void>() {
        @Override
        public void onComplete(@NonNull Task<Void> task) {

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

    /**
     * Class for demo {@link RawData}
     */
    private static class Ppap {
        public String[] mIhaveA;
        public String mUh;
    }

}

