package io.straas.android.sdk.messaging.demo;

import android.content.*;

import io.straas.android.sdk.messaging.*;

import static io.straas.android.sdk.messaging.demo.MessagingHostActivity.*;

public class MainHostActivity extends MainActivity {

    @Override
    protected void onCustomizeChatroomConfig(ChatroomManager.ChatroomConfig.Builder builder) {
        Intent intent = getIntent();
        String restHost = intent.getStringExtra(EXTRA_REST_HOST);
        String socketHost = intent.getStringExtra(EXTRA_SOCKET_HOST);
        String cmsHost = intent.getStringExtra(EXTRA_CMS_HOST);

        builder.setRestHost(restHost)
                .setSocketHost(socketHost)
                .setCmsHost(cmsHost);
    }
}
