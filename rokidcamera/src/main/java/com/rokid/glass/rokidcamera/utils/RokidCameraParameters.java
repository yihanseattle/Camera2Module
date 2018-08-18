package com.rokid.glass.rokidcamera.utils;

import static android.hardware.camera2.CameraMetadata.CONTROL_AE_MODE_OFF;
import static android.hardware.camera2.CameraMetadata.CONTROL_AE_MODE_ON;
import static android.hardware.camera2.CameraMetadata.CONTROL_AF_MODE_AUTO;
import static android.hardware.camera2.CameraMetadata.CONTROL_AF_MODE_CONTINUOUS_PICTURE;
import static android.hardware.camera2.CameraMetadata.CONTROL_AF_MODE_CONTINUOUS_VIDEO;
import static android.hardware.camera2.CameraMetadata.CONTROL_AWB_MODE_AUTO;
import static android.hardware.camera2.CameraMetadata.CONTROL_AWB_MODE_OFF;

/**
 * Created by yihan on 8/17/18.
 */

public enum  RokidCameraParameters {
    ROKID_CAMERA_PARAM_AE_MODE_ON(CONTROL_AE_MODE_ON),
    ROKID_CAMERA_PARAM_AE_MODE_OFF(CONTROL_AE_MODE_OFF),
    ROKID_CAMERA_PARAM_AWB_MODE_AUTO(CONTROL_AWB_MODE_AUTO),
    ROKID_CAMERA_PARAM_AWB_MODE_OFF(CONTROL_AWB_MODE_OFF),
    ROKID_CAMERA_PARAM_AF_MODE_AUTO(CONTROL_AF_MODE_AUTO),
    ROKID_CAMERA_PARAM_AF_MODE_PICTURE(CONTROL_AF_MODE_CONTINUOUS_PICTURE),
    ROKID_CAMERA_PARAM_AF_MODE_VIDEO(CONTROL_AF_MODE_CONTINUOUS_VIDEO);

    private int mode;

    RokidCameraParameters(int mode) {
        this.mode = mode;
    }

    public int getMode() {
        return mode;
    }
}
