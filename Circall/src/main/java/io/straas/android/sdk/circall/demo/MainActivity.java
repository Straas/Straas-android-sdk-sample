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
import io.straas.android.sdk.demo.databinding.ActivityMainBinding;
import io.straas.android.sdk.demo.qrcode.QrcodeActivity;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

import static io.straas.android.sdk.circall.demo.SingleVideoCallActivity.INTENT_CIRCALL_TOKEN;

public class MainActivity extends AppCompatActivity {

    public static final String[] CIRCALL_PERMISSIONS = {
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO
    };

    private static final int CIRCALL_PERMISSION_REQUEST = 1;
    private static final int SCAN_QRCODE_REQUEST = 2;

    private ActivityMainBinding mBinding;
    private boolean mIsRequestFromStartVideoCall = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);
    }

    public void onScanQrcode(View view) {
        mIsRequestFromStartVideoCall = false;
        checkPermissions();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SCAN_QRCODE_REQUEST && resultCode == Activity.RESULT_OK) {
            String streamKey = data.getStringExtra(QrcodeActivity.KEY_QR_CODE_VALUE);
            mBinding.circallStreamKey.setText(trim(streamKey));
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
        mIsRequestFromStartVideoCall = true;
        checkPermissions();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @AfterPermissionGranted(CIRCALL_PERMISSION_REQUEST)
    private synchronized void checkPermissions() {
        android.util.Log.d("jason", "checkPermissions mIsRequestFromStartVideoCall:" + mIsRequestFromStartVideoCall);
        if (EasyPermissions.hasPermissions(this, CIRCALL_PERMISSIONS)) {
            if (mIsRequestFromStartVideoCall) {
                Intent intent = new Intent(this, SingleVideoCallActivity.class);
                intent.putExtra(INTENT_CIRCALL_TOKEN, mBinding.circallStreamKey.getText().toString());
                startActivity(intent);
            } else {
                Intent intent = new Intent(this, QrcodeActivity.class);
                startActivityForResult(intent, SCAN_QRCODE_REQUEST);
            }
        } else {
            android.util.Log.d("jason", "checkPermissions EasyPermissions.requestPermissions:" + mIsRequestFromStartVideoCall);

            EasyPermissions.requestPermissions(this, getString(R.string.circall_need_permission),
                    CIRCALL_PERMISSION_REQUEST, CIRCALL_PERMISSIONS);
        }
    }
}
