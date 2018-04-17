package io.straas.android.sdk.circall.demo;

import android.Manifest;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;

import io.straas.android.sdk.circall.CircallToken;
import io.straas.android.sdk.demo.R;
import io.straas.android.sdk.demo.databinding.ActivityMainBinding;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

import static io.straas.android.sdk.circall.demo.SingleVideoCallActivity.INTENT_CIRCALL_TOKEN;

public class MainActivity extends AppCompatActivity {

    public static final String[] CIRCALL_PERMISSIONS = {
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO
    };
    public static final int CIRCALL_PERMISSION_REQUEST = 1;

    private ActivityMainBinding mBinding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);

       // binding.addTextChangedListener(this);
       // binding.memberPassword.addTextChangedListener(this);
       // binding.memberId.addTextChangedListener(this);
     //   binding.memberPassword.addTextChangedListener(this);
    }

    public void onScan(View view) {
    }

    public void onEnterRoom(View view) {
        String token = mBinding.circallStreamKey.getText().toString();
        android.util.Log.d("jason", "onEnterRoom token:" + token);
     /*   if (!CircallToken.isValidToken(token)) {
            // show yellow stream key error
            mBinding.setCircallStreamKeyErrorText(getString(TextUtils.isEmpty(token) ?
                    R.string.empty_stream_key : R.string.error_stream_key));
            return;
        }
*/
        mBinding.setCircallStreamKeyErrorText("");
        checkPermissions();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @AfterPermissionGranted(CIRCALL_PERMISSION_REQUEST)
    private void checkPermissions() {
        if (EasyPermissions.hasPermissions(this, CIRCALL_PERMISSIONS)) {
            Intent intent = new Intent(this, SingleVideoCallActivity.class);
            intent.putExtra(INTENT_CIRCALL_TOKEN, "eyJ0b2tlbklkIjoiZXlKaGJHY2lPaUpJVXpJMU5pSXNJblI1Y0NJNklrcFhWQ0o5LmV5SmhJam9pZEhKcFlXd3VjM1J5WVdGekxtbHZMWFJsYzNRaUxDSnlJam9pTkhBMGMxOWlNMlUzWWpjMFpXSTNNV00wWW1NeE9XWTJaak14WlRnd1pqZzRNbU15TVdJeVpEZ2lMQ0oxSWpvaWRYTmxjakVpTENKd0lqb2lOSEEwY3lJc0ltOGlPaUp0WVc1aFoyVnlJaXdpWlNJNk1UVXlOREV5TXpZME9IMC5JVXFqakZFTGJmUUlUVXEwNG5yZXduaXk0eVhpTXVQX3V0aklTZFgza0EwIiwiaG9zdCI6ImNpcmNhbGwtZWMtcmMuc3RyYWFzLm5ldCIsInNlY3VyZSI6dHJ1ZSwic2lnbmF0dXJlIjoiWkRVeE1qVTJNRE0wTm1Jd05ERTVOelUxWlRRNVlUQXdOakpqTldabVpUSTBZakF5TmpFelpnPT0ifQ");
            startActivity(intent);
        } else {
            EasyPermissions.requestPermissions(this, getString(R.string.circall_need_permission),
                    CIRCALL_PERMISSION_REQUEST, CIRCALL_PERMISSIONS);
        }
    }
}
