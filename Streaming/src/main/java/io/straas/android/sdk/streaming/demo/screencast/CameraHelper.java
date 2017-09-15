package io.straas.android.sdk.streaming.demo.screencast;

import android.hardware.Camera;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class CameraHelper {

    public static boolean setCameraPreviewSize(Camera camera, int cameraViewPxArea) {
        if (camera == null || cameraViewPxArea <= 0) {
            return false;
        }

        Camera.Parameters parameters = camera.getParameters();
        Camera.Size size = getCameraPreviewSize(parameters.getSupportedPreviewSizes(), cameraViewPxArea);
        if (size != null && size.width > 0 && size.height > 0) {
            parameters.setPreviewSize(size.width, size.height);
            camera.setParameters(parameters);
            return true;
        }

        return false;
    }

    private static Camera.Size getCameraPreviewSize(List<Camera.Size> previewsSizes, int cameraViewPxArea) {
        if (cameraViewPxArea <= 0) {
            return null;
        }

        Collections.sort(previewsSizes, new Comparator<Camera.Size>() {
            @Override
            public int compare(Camera.Size lhs, Camera.Size rhs) {
                if ((lhs.width * lhs.height) > (rhs.width * rhs.height)) {
                    return 1;
                } else {
                    return -1;
                }
            }
        });
        Camera.Size cameraSize = null;
        for (Camera.Size size : previewsSizes) {
            if (cameraViewPxArea >= size.width * size.height) {
                return size;
            }
        }
        return cameraSize;
    }
}
