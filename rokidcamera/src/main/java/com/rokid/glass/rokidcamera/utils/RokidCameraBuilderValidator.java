package com.rokid.glass.rokidcamera.utils;

import android.graphics.ImageFormat;

import com.rokid.glass.rokidcamera.RokidCamera;
import com.rokid.glass.rokidcamera.RokidCameraBuilder;

/**
 * Created by yihan on 8/23/18.
 */

public class RokidCameraBuilderValidator {


    public static void validateImageFormat(RokidCameraBuilder rokidCameraBuilder) {
        int imageFormat = rokidCameraBuilder.getImageFormat();
        if (imageFormat == ImageFormat.JPEG ||
                imageFormat == ImageFormat.YUV_420_888) {
            return;
        }

        throw new IllegalStateException("Rokid Camera Only Support JPEG or YUV_420_888 formats!");
    }

    public static void validateMaxImageBuffer(RokidCameraBuilder rokidCameraBuilder) {
        int imageBufferSize = rokidCameraBuilder.getMaxImages();
        if (imageBufferSize > 2 && imageBufferSize < 20) {
            return;
        }

        throw new IllegalStateException("Rokid Camera Only Support between 2 and 20 for Max Image Buffer Size!");
    }

    public static void validateImageReaderCallbackMode(RokidCameraBuilder rokidCameraBuilder) {
        if (rokidCameraBuilder.getImageReaderCallbackMode() == RokidCamera.STILL_PHOTO_MODE_SINGLE_NO_CALLBACK) {
            if (rokidCameraBuilder.getRokidCameraIOListener() == null) {
                throw new IllegalStateException("Must implements RokidCameraIOListener when using RokidCamera.STILL_PHOTO_MODE_SINGLE_NO_CALLBACK");
            }
        }

        if (rokidCameraBuilder.getImageReaderCallbackMode() == RokidCamera.STILL_PHOTO_MODE_CONTINUOUS_IMAGE_CALLBACK ||
                rokidCameraBuilder.getImageReaderCallbackMode() == RokidCamera.STILL_PHOTO_MODE_SINGLE_IMAGE_CALLBACK) {
            if (rokidCameraBuilder.getRokidCameraOnImageAvailableListener() == null) {
                throw new IllegalStateException("Must implements RokidCameraOnImageAvailableListener when " +
                        "using RokidCamera.STILL_PHOTO_MODE_CONTINUOUS_IMAGE_CALLBACK or RokidCamera.STILL_PHOTO_MODE_SINGLE_IMAGE_CALLBACK");
            }
        }
    }

    public static void validateSizePreview(RokidCameraBuilder rokidCameraBuilder) {
        RokidCameraSize sizePreview = rokidCameraBuilder.getRokidCameraSizePreview();
        if (sizePreview == RokidCameraSize.SIZE_PREVIEW) {
            return;
        }

        throw new IllegalStateException("Must use Preview Size!");
    }

    public static void validateSizeImageReader(RokidCameraBuilder rokidCameraBuilder) {
        RokidCameraSize sizeImageReader = rokidCameraBuilder.getRokidCameraSizeImageReader();
        if (sizeImageReader == RokidCameraSize.SIZE_IMAGE_READER_ALGORITHM_ARSDK ||
                sizeImageReader == RokidCameraSize.SIZE_IMAGE_READER_ALGORITHM_FACEID ||
                sizeImageReader == RokidCameraSize.SIZE_IMAGE_READER_ALGORITHM_LANDMARK ||
                sizeImageReader == RokidCameraSize.SIZE_IMAGE_READER_ALGORITHM_SLAM ||
                sizeImageReader == RokidCameraSize.SIZE_IMAGE_READER_STILL_PHOTO) {
            return;
        }

        throw new IllegalStateException("Must use ImageReader Size!");
    }

    public static void validateSizeVideoRecorder(RokidCameraBuilder rokidCameraBuilder) {
        RokidCameraSize sizeVideoRecorder = rokidCameraBuilder.getRokidCameraSizeVideoRecorder();
        if (sizeVideoRecorder == RokidCameraSize.SIZE_VIDEO_RECORDING) {
            return;
        }

        throw new IllegalStateException("Must use Recording Size!");
    }

    public static void validateParamAEMode(RokidCameraBuilder rokidCameraBuilder) {
        RokidCameraParameters aeMode = rokidCameraBuilder.getRokidCameraParamAEMode();
        if (aeMode == RokidCameraParameters.ROKID_CAMERA_PARAM_AE_MODE_OFF ||
                aeMode == RokidCameraParameters.ROKID_CAMERA_PARAM_AE_MODE_ON) {
            return;
        }

        throw new IllegalStateException("Please use AE Parameters ONLY!");
    }

    public static void validateParamAFMode(RokidCameraBuilder rokidCameraBuilder) {
        RokidCameraParameters afMode = rokidCameraBuilder.getRokidCameraParamAFMode();
        if (afMode == RokidCameraParameters.ROKID_CAMERA_PARAM_AF_MODE_AUTO ||
                afMode == RokidCameraParameters.ROKID_CAMERA_PARAM_AF_MODE_PICTURE ||
                afMode == RokidCameraParameters.ROKID_CAMERA_PARAM_AF_MODE_VIDEO) {
            return;
        }

        throw new IllegalStateException("Please use AF Parameters ONLY!");
    }

    public static void validateParamAWBMode(RokidCameraBuilder rokidCameraBuilder) {
        RokidCameraParameters awbMode = rokidCameraBuilder.getRokidCameraParamAWBMode();
        if (awbMode == RokidCameraParameters.ROKID_CAMERA_PARAM_AWB_MODE_AUTO ||
                awbMode == RokidCameraParameters.ROKID_CAMERA_PARAM_AWB_MODE_OFF) {
            return;
        }

        throw new IllegalStateException("Please use AWB Parameters ONLY!");
    }

    public static void validateParamCameraId(RokidCameraBuilder rokidCameraBuilder) {
        RokidCameraParameters cameraId = rokidCameraBuilder.getRokidCameraParamCameraId();
        if (cameraId == RokidCameraParameters.ROKID_CAMERA_PARAM_CAMERA_ID_BACK ||
                cameraId == RokidCameraParameters.ROKID_CAMERA_PARAM_CAMERA_ID_FRONT ||
                cameraId == RokidCameraParameters.ROKID_CAMERA_PARAM_CAMERA_ID_ROKID_GLASS) {
            return;
        }

        throw new IllegalStateException("Please use CAMERA_ID in RokidCameraParameters!");
    }
}
