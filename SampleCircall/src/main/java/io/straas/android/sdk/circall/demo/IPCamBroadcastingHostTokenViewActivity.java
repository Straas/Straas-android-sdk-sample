package io.straas.android.sdk.circall.demo;

import android.content.Intent;
import android.os.Bundle;

import static io.straas.android.sdk.circall.demo.IPCamBroadcastingHostActivity.INTENT_CIRCALL_TOKEN;
import static io.straas.android.sdk.circall.demo.IPCamBroadcastingHostActivity.INTENT_PUBLISH_URL;

public class IPCamBroadcastingHostTokenViewActivity extends TokenViewBaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding.setNeedPublishUrl(true);
    }

    @Override
    protected void enterRoom() {
        Intent intent = new Intent(this, IPCamBroadcastingHostActivity.class);
        intent.putExtra(INTENT_CIRCALL_TOKEN, mBinding.circallToken.getText().toString());
        intent.putExtra(INTENT_PUBLISH_URL, mBinding.circallPublishUrl.getText().toString());
        startActivity(intent);
    }
}
