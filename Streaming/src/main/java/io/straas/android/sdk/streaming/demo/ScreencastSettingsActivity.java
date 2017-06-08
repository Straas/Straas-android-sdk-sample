package io.straas.android.sdk.streaming.demo;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.projection.MediaProjectionManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;

import io.straas.android.sdk.demo.R;

import io.straas.android.sdk.streaming.StreamConfig;
import io.straas.android.sdk.streaming.StreamManager;

import io.straas.sdk.demo.MemberIdentity;

public class ScreencastSettingsActivity extends AppCompatActivity {

    private static final String TAG = ScreencastSettingsActivity.class.getSimpleName();

    private static final String[] STREAM_PERMISSIONS = {
            Manifest.permission.RECORD_AUDIO
    };

    private static final int STREAM_PERMISSION_REQUEST = 1;
    private static final int REQUEST_MEDIA_PROJECTION = 2;
    private static final int REQUEST_OVERLAY_PERMISSION = 3;

    private EditText mEditTitle;
    private EditText mEditSynopsis;
    private Spinner mPictureQualitySpinner;

    private MediaProjectionManager mMediaProjectionManager;
    private int mResultCode;
    private Intent mResultData;
    private HashMap<String, Integer> mPictureQualityMap = new HashMap<String, Integer>();

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_screencast_settings);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            Toast.makeText(ScreencastSettingsActivity.this,
                    getResources().getString(R.string.hint_screencast_support_api_level), Toast.LENGTH_SHORT).show();
            finish();
        }

        mEditTitle = (EditText) findViewById(R.id.edit_title);
        mEditSynopsis = (EditText) findViewById(R.id.edit_synopsis);
        mPictureQualitySpinner = (Spinner)findViewById(R.id.quality_spinner);
        final String[] pictureQualityList = getResources().getStringArray(R.array.picture_quality_list);
        ArrayAdapter<String> pictureQualityListAdapter = new ArrayAdapter<>(ScreencastSettingsActivity.this,
                android.R.layout.simple_spinner_dropdown_item, pictureQualityList);
        mPictureQualitySpinner.setAdapter(pictureQualityListAdapter);

        final int[] pictureQualityIntegerList = getResources().getIntArray(R.array.picture_quality_integer_list);
        for (int index = 0; index < pictureQualityList.length; index++) {
            mPictureQualityMap.put(pictureQualityList[index], pictureQualityIntegerList[index]);
        }

        checkPermissions();
        mMediaProjectionManager = (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);
    }

    private void checkPermissions() {
        String[] requestPermissions = getPermissionsRequestArray(STREAM_PERMISSIONS);
        if (requestPermissions.length != 0) {
            ActivityCompat.requestPermissions(ScreencastSettingsActivity.this, requestPermissions,
                    STREAM_PERMISSION_REQUEST);
        }
    }

    private String[] getPermissionsRequestArray(String[] permissions) {
        ArrayList<String> requestArray = new ArrayList<>();
        for (String permission : permissions) {
            if (ActivityCompat.checkSelfPermission(ScreencastSettingsActivity.this, permission) !=
                    PackageManager.PERMISSION_GRANTED) {
                requestArray.add(permission);
            }
        }
        return requestArray.toArray(new String[0]);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == STREAM_PERMISSION_REQUEST) {
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(ScreencastSettingsActivity.this,
                            getResources().getString(R.string.hint_need_permission), Toast.LENGTH_SHORT).show();
                    return;
                }
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public void startScreenCapture(View view) {
        startActivityForResult(mMediaProjectionManager.createScreenCaptureIntent(), REQUEST_MEDIA_PROJECTION);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_MEDIA_PROJECTION && resultCode == Activity.RESULT_OK) {
            mResultCode = resultCode;
            mResultData = data;

            if (Settings.canDrawOverlays(ScreencastSettingsActivity.this)) {
                startScreenStreaming();
            } else {
                requestOverlayPermission();
            }
        } else if (requestCode == REQUEST_OVERLAY_PERMISSION && resultCode == Activity.RESULT_OK) {
            startScreenStreaming();
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void startScreenStreaming() {
        Bundle bundle = new Bundle();
        bundle.putString(StreamConfig.EXTRA_LIVE_EVENT_TITLE, mEditTitle.getText().toString());
        bundle.putString(StreamConfig.EXTRA_LIVE_EVENT_SYNOPSIS, mEditSynopsis.getText().toString());
        bundle.putInt(StreamConfig.EXTRA_LIVE_PICTURE_QUALITY, mPictureQualityMap.get(mPictureQualitySpinner.getSelectedItem().toString()));
        StreamManager.initialize(this, MemberIdentity.ME, mResultCode, mResultData, bundle);

        startActivity(new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME));
        finish();
    }

    @TargetApi(Build.VERSION_CODES.M)
    private void requestOverlayPermission() {
        Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()));
        startActivityForResult(intent, REQUEST_OVERLAY_PERMISSION);
    }
}
