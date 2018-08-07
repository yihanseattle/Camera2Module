package com.rokid.glass.rokidcamera.utils;

import android.hardware.camera2.CameraCharacteristics;
import android.util.Size;
import android.util.SparseIntArray;

import java.util.Comparator;

/**
 * Created by yihan on 7/25/18.
 */

public class CameraDeviceUtils {

    private static class CompareSizeByArea implements Comparator<Size> {

        @Override
        public int compare(Size lhs, Size rhs) {
            return Long.signum((long) lhs.getWidth() * lhs.getHeight() -
                    (long) rhs.getWidth() * rhs.getHeight());
        }

    }

    /**
     * Help to calculate Camera Parameters
     * @param cameraCharacteristics : current characteristics
     * @param deviceOrientation     : device(screen) orientation
     * @return : orientation for (sensor + device)
     */
    public static int sensorToDeviceRotation(CameraCharacteristics cameraCharacteristics, int deviceOrientation, SparseIntArray orientationMap) {
        int sensorOrientation = cameraCharacteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);
        deviceOrientation = orientationMap.get(deviceOrientation);
        return (sensorOrientation + deviceOrientation + 270) % 360;
    }

    /**
     * Given {@code choices} of {@code Size}s supported by a camera, choose the smallest one that
     * is at least as large as the respective texture view size, and that is at most as large as the
     * respective max size, and whose aspect ratio matches with the specified value. If such size
     * doesn't exist, choose the largest one that is at most as large as the respective max size,
     * and whose aspect ratio matches with the specified value.
     *
     * @param choices           The list of sizes that the camera supports for the intended output
     *                          class
     * @param textureViewWidth  The width of the texture view relative to sensor coordinate
     * @param textureViewHeight The height of the texture view relative to sensor coordinate
     * @param maxWidth          The maximum width that can be chosen
     * @param maxHeight         The maximum height that can be chosen
     * @param aspectRatio       The aspect ratio
     * @return The optimal {@code Size}, or an arbitrary one if none were big enough
     */
    public static Size chooseOptimalSize(Size[] choices, int textureViewWidth,
                                          int textureViewHeight, int maxWidth, int maxHeight, Size aspectRatio) {

//        // Collect the supported resolutions that are at least as big as the preview Surface
//        List<Size> bigEnough = new ArrayList<>();
//        // Collect the supported resolutions that are smaller than the preview Surface
//        List<Size> notBigEnough = new ArrayList<>();
//
//        List<Double> ratio = new LinkedList<>();
//
//        int w = aspectRatio.getWidth();
//        int h = aspectRatio.getHeight();
//        for (Size option : choices) {
//            if (option.getWidth() > option.getHeight()) {
//                ratio.add((double)option.getWidth() / option.getHeight());
//            } else {
//                ratio.add((double)option.getHeight() / option.getWidth());
//            }
//            if (option.getWidth() <= maxWidth && option.getHeight() <= maxHeight &&
//                    option.getHeight() == option.getWidth() * h / w) {
//                if (option.getWidth() >= textureViewWidth &&
//                        option.getHeight() >= textureViewHeight) {
//                    bigEnough.add(option);
//                } else {
//                    notBigEnough.add(option);
//                }
//            }
//        }
//
//        // Pick the smallest of those big enough. If there is no one big enough, pick the
//        // largest of those not big enough.
//        if (bigEnough.size() > 0) {
//            return Collections.min(bigEnough, new CompareSizeByArea());
//        } else if (notBigEnough.size() > 0) {
//            return Collections.max(notBigEnough, new CompareSizeByArea());
//        } else {
//            Log.e(TAG, "Couldn't find any suitable preview size");
//            if (DeviceConfig.isInRokidGlass) {
//                return choices[5];
//            } else {
//                return choices[choices.length - 1];
//            }
//        }

        // TODO: temporary fix because the preview size in xml has been set to [1dp x 1dp]

//        if (isInRokidGlass) {
            return choices[5];
//        } else {
//            return choices[2];
//        }
    }
}
