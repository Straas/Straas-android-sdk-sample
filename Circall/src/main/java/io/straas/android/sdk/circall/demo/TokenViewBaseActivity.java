package io.straas.android.sdk.circall.demo;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;

import io.straas.android.sdk.circall.CircallToken;
import io.straas.android.sdk.demo.R;
import io.straas.android.sdk.demo.databinding.ActivityTokenViewBinding;
import io.straas.android.sdk.demo.qrcode.QrcodeActivity;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public abstract class TokenViewBaseActivity extends AppCompatActivity {

    public static final String[] QR_CODE_PERMISSIONS = {
            Manifest.permission.CAMERA
    };

    private static final int QRCODE_STREAM_KEY_REQUEST = 1;
    private static final int QRCODE_RTSP_URL_REQUEST = 2;

    protected ActivityTokenViewBinding mBinding;

    abstract protected void enterRoom();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_token_view);
    }

    public void scanStreamKey(View view) {
        scanStreamKey();
    }

    public void scanRtspUrl(View view) {
        scanRtspUrl();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == QRCODE_STREAM_KEY_REQUEST && resultCode == Activity.RESULT_OK) {
            String streamKey = data.getStringExtra(QrcodeActivity.KEY_QR_CODE_VALUE);
            mBinding.circallStreamKey.setText(trim(streamKey));
        } else if (requestCode == QRCODE_RTSP_URL_REQUEST && resultCode == Activity.RESULT_OK) {
            String rtspUrl = data.getStringExtra(QrcodeActivity.KEY_QR_CODE_VALUE);
            mBinding.circallPublishUrl.setText(trim(rtspUrl));
        }
    }

    private String trim(String streamKey) {
        return streamKey.trim().replace("\n", "").replace("\r", "");
    }

    public void onEnterRoom(View view) {
        String token = mBinding.circallStreamKey.getText().toString();
        if (!CircallToken.isValidToken(token)) {
            // show yellow stream key error
            mBinding.setCircallStreamKeyErrorText(getString(TextUtils.isEmpty(token) ?
                    R.string.empty_stream_key : R.string.error_stream_key));
            return;
        }

        mBinding.setCircallStreamKeyErrorText("");
        enterRoom();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @AfterPermissionGranted(QRCODE_STREAM_KEY_REQUEST)
    private synchronized void scanStreamKey() {
        if (EasyPermissions.hasPermissions(this, QR_CODE_PERMISSIONS)) {
            Intent intent = new Intent(this, QrcodeActivity.class);
            startActivityForResult(intent, QRCODE_STREAM_KEY_REQUEST);
        } else {
            EasyPermissions.requestPermissions(this, getString(R.string.qr_code_need_permission),
                    QRCODE_STREAM_KEY_REQUEST, QR_CODE_PERMISSIONS);
        }
    }

    @AfterPermissionGranted(QRCODE_RTSP_URL_REQUEST)
    private synchronized void scanRtspUrl() {
        if (EasyPermissions.hasPermissions(this, QR_CODE_PERMISSIONS)) {
            Intent intent = new Intent(this, QrcodeActivity.class);
            startActivityForResult(intent, QRCODE_RTSP_URL_REQUEST);
        } else {
            EasyPermissions.requestPermissions(this, getString(R.string.qr_code_need_permission),
                    QRCODE_RTSP_URL_REQUEST, QR_CODE_PERMISSIONS);
        }
    }
}
