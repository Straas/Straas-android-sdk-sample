package io.straas.android.sdk.circall.demo;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
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

    private static final int QRCODE_CIRCALL_TOKEN_REQUEST = 1;
    private static final int QRCODE_RTSP_URL_REQUEST = 2;

    private static final String RTSP_PREFIX = "rtsp://";

    private static final String NAME_SHARED_PREFERENCES = "TOKEN_VIEW";
    private static final String KEY_CIRCALL_TOKEN = "KEY_CIRCALL_TOKEN";
    private static final String KEY_PUBLISH_URL = "KEY_PUBLISH_URL";

    protected ActivityTokenViewBinding mBinding;

    abstract protected void enterRoom();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_token_view);
    }

    @Override
    protected void onStart() {
        super.onStart();
        SharedPreferences sharedPreferences = getSharedPreferences(NAME_SHARED_PREFERENCES, MODE_PRIVATE);
        mBinding.circallToken.setText(sharedPreferences.getString(KEY_CIRCALL_TOKEN, ""));
        mBinding.circallPublishUrl.setText(sharedPreferences.getString(KEY_PUBLISH_URL, ""));
    }

    @Override
    protected void onStop() {
        super.onStop();
        getSharedPreferences(NAME_SHARED_PREFERENCES, MODE_PRIVATE).edit()
                .putString(KEY_CIRCALL_TOKEN, mBinding.circallToken.getText().toString())
                .putString(KEY_PUBLISH_URL, mBinding.circallPublishUrl.getText().toString())
                .apply();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == QRCODE_CIRCALL_TOKEN_REQUEST && resultCode == Activity.RESULT_OK) {
            String token = data.getStringExtra(QrcodeActivity.KEY_QR_CODE_VALUE);
            mBinding.circallToken.setText(trim(token));
        } else if (requestCode == QRCODE_RTSP_URL_REQUEST && resultCode == Activity.RESULT_OK) {
            String rtspUrl = data.getStringExtra(QrcodeActivity.KEY_QR_CODE_VALUE);
            mBinding.circallPublishUrl.setText(trim(rtspUrl));
        }
    }

    private String trim(String str) {
        return str.trim().replace("\n", "").replace("\r", "");
    }

    public void onEnterRoom(View view) {
        String token = mBinding.circallToken.getText().toString();
        if (!CircallToken.isValidToken(token)) {
            // show yellow circall token error
            mBinding.setInformationErrorText(getString(TextUtils.isEmpty(token) ?
                    R.string.empty_circall_token : R.string.error_circall_token));
            return;
        }
        if (mBinding.circallPublishUrl.getVisibility() == View.VISIBLE) {
            if (TextUtils.isEmpty(mBinding.circallPublishUrl.getText())) {
                mBinding.setInformationErrorText(getString(R.string.empty_rtsp_url));
                return;
            } else if (!mBinding.circallPublishUrl.getText().toString().startsWith(RTSP_PREFIX)) {
                mBinding.setInformationErrorText(getString(R.string.error_rtsp_url));
                return;
            }
        }

        mBinding.setInformationErrorText("");
        enterRoom();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @AfterPermissionGranted(QRCODE_CIRCALL_TOKEN_REQUEST)
    private synchronized void scanCircallToken() {
        if (EasyPermissions.hasPermissions(this, QR_CODE_PERMISSIONS)) {
            Intent intent = new Intent(this, QrcodeActivity.class);
            startActivityForResult(intent, QRCODE_CIRCALL_TOKEN_REQUEST);
        } else {
            EasyPermissions.requestPermissions(this, getString(R.string.qr_code_need_permission),
                    QRCODE_CIRCALL_TOKEN_REQUEST, QR_CODE_PERMISSIONS);
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
