package io.straas.android.sdk.streaming.demo.screencast;

import android.hardware.Camera;

import java.util.List;

public class CameraHelper {

    public static boolean setCameraPreviewSize(Camera camera, int cameraViewPxArea) {
        if (camera == null || cameraViewPxArea <= 0) {
            return false;
        }

        Camera.Parameters parameters = camera.getParameters();
        Camera.Size size = getClosestPreviewSize(parameters.getSupportedPreviewSizes(), cameraViewPxArea);
        if (size != null && size.width > 0 && size.height > 0) {
            parameters.setPreviewSize(size.width, size.height);
            camera.setParameters(parameters);
            return true;
        }

        return false;
    }

    private static Camera.Size getClosestPreviewSize(List<Camera.Size> previewsSizes, int cameraViewPxArea) {
        Camera.Size closestSize = null;
        int minDiffArea = Integer.MAX_VALUE;
        for (Camera.Size size : previewsSizes) {
            int diffArea = Math.abs(cameraViewPxArea - size.width * size.height);
            if (diffArea < minDiffArea) {
                minDiffArea = diffArea;
                closestSize = size;
            }
        }
        return closestSize;
    }

}
