package io.straas.android.sdk.circall.demo;

import android.content.Intent;

import static io.straas.android.sdk.circall.demo.IPCamBroadcastingViewerActivity.INTENT_CIRCALL_TOKEN;

public class IPCamBroadcastingViewerTokenViewActivity extends TokenViewBaseActivity {

    @Override
    protected void enterRoom() {
        Intent intent = new Intent(this, IPCamBroadcastingViewerActivity.class);
        intent.putExtra(INTENT_CIRCALL_TOKEN, mBinding.circallToken.getText().toString());
        startActivity(intent);
    }
}
