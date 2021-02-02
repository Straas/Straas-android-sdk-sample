package io.straas.android.sdk.messaging.demo;

import android.content.*;
import android.os.*;
import android.view.*;

import androidx.annotation.*;
import androidx.appcompat.app.*;
import io.straas.android.sdk.demo.common.widget.*;

public class MessagingHostActivity extends AppCompatActivity {

    public static final String EXTRA_REST_HOST = "EXTRA_REST_HOST";
    public static final String EXTRA_SOCKET_HOST = "EXTRA_SOCKET_HOST";
    public static final String EXTRA_CMS_HOST = "EXTRA_CMS_HOST";

    RecordTextInputEditText mEditTextRestHost, mEditTextSocketHost, mEditTextCmsHost;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messaging_host);

        mEditTextRestHost = findViewById(R.id.chatroom_rest_host);
        mEditTextSocketHost = findViewById(R.id.chatroom_socket_host);
        mEditTextCmsHost = findViewById(R.id.chatroom_cms_host);
    }

    public void startActivity(View view) {
        Intent intent = new Intent();
        intent.putExtra(EXTRA_REST_HOST, mEditTextRestHost.getText().toString());
        intent.putExtra(EXTRA_SOCKET_HOST, mEditTextSocketHost.getText().toString());
        intent.putExtra(EXTRA_CMS_HOST, mEditTextCmsHost.getText().toString());

        intent.setClass(MessagingHostActivity.this, MainHostActivity.class);

        startActivity(intent);
    }
}
