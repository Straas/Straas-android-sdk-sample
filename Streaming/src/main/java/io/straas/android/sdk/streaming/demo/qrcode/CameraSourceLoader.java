package io.straas.android.sdk.streaming.demo.qrcode;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;
import android.util.Log;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.MultiProcessor;
import com.google.android.gms.vision.Tracker;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;

final class CameraSourceLoader extends AsyncTaskLoader<CameraSource> {
    private CameraSource mCameraSource;
    private TaskCompletionSource<Barcode> mTask = new TaskCompletionSource<>();

    CameraSourceLoader(Context context) {
        super(context);
    }

    Task<Barcode> getBarcodeScanTask() {
        mTask = new TaskCompletionSource<>();
        return mTask.getTask();
    }

    @Override
    public CameraSource loadInBackground() {
        BarcodeDetector barcodeDetector = new BarcodeDetector.Builder(getContext())
                .setBarcodeFormats(Barcode.QR_CODE)
                .build();
        barcodeDetector.setProcessor(new MultiProcessor.Builder<>(new MultiProcessor.Factory<Barcode>() {
            @Override
            public Tracker<Barcode> create(Barcode barcode) {
                mTask.trySetResult(barcode);
                return new Tracker<>();
            }
        }).build());

        if (!barcodeDetector.isOperational()) {
            Log.w(QrcodeActivity.TAG, "Detector dependencies are not yet available.");
        }

        mCameraSource = new CameraSource.Builder(getContext(), barcodeDetector)
                .setFacing(CameraSource.CAMERA_FACING_BACK)
                .setRequestedPreviewSize(1600, 1024)
                .setRequestedFps(15.0f)
                .setAutoFocusEnabled(true)
                .build();
        return mCameraSource;
    }

    @Override
    protected void onStartLoading() {
        forceLoad();
    }

    @Override
    protected void onStopLoading() {
        if (mCameraSource != null) {
            mCameraSource.stop();
        }
    }

    @Override
    protected void onReset() {
        if (mCameraSource != null) {
            mCameraSource.release();
        }
    }
}
