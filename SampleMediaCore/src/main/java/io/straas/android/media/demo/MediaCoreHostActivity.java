package io.straas.android.media.demo;

import android.content.*;
import android.os.*;
import android.view.*;

import androidx.annotation.*;
import androidx.appcompat.app.*;
import io.straas.android.sdk.demo.common.widget.*;

public class MediaCoreHostActivity extends AppCompatActivity {

    public static final String EXTRA_REST_HOST = "EXTRA_REST_HOST";

    RecordTextInputEditText mEditTextRest;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_media_core_host);

        mEditTextRest = findViewById(R.id.media_core_rest);
    }

    public void startActivity(View view) {
        Intent intent = new Intent();
        intent.putExtra(EXTRA_REST_HOST, mEditTextRest.getText().toString());

        int id = view.getId();
        if (id == R.id.btn_start_list) {
            intent.setClass(MediaCoreHostActivity.this, StraasPlayerHostActivity.class);
        } else if (id == R.id.btn_start_operation) {
            intent.setClass(MediaCoreHostActivity.this, OperationHostActivity.class);
        }

        startActivity(intent);
    }

}
