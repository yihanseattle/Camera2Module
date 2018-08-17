package com.rokid.glass.rokidcamera.utils;

import android.util.Size;

/**
 * All pre-defined Size for different Camera configurations.
 *
 * <ul>
 *  <li>Sizes start with IMAGE_READER is for ImageReader when reading Image from Camera
 *  <li>Sizes start with PREVIEW is for camera preview
 *  <li>Sizes start with VIDEO_RECORDING is for MediaRecorder when recording a video
 * </ul>
 */

public enum RokidCameraSize {

    /** Size for ImageReader for FaceID Algorithm */
    SIZE_IMAGE_READER_ALGORITHM_FACEID  (new Size(1280, 720)),
    /** Size for ImageReader for SLAM Algorithm */
    SIZE_IMAGE_READER_ALGORITHM_SLAM    (new Size(640, 480)),
    /** Size for ImageReader for Landmark Algorithm */
    SIZE_IMAGE_READER_ALGORITHM_LANDMARK(new Size(320, 480)),
    /** Size for ImageReader for ARSDK Algorithm */
    SIZE_IMAGE_READER_ALGORITHM_ARSDK   (new Size(640, 480)),
    /** Size for ImageReader for Still Photo */
    SIZE_IMAGE_READER_STILL_PHOTO       (new Size(4000, 3000)),
    /** Size for ImageReader for Camera Preview */
    SIZE_PREVIEW                        (new Size(1600, 1200)),
    /** Size for ImageReader for Video Recording */
    SIZE_VIDEO_RECORDING                (new Size(2592, 1944));

    private Size mSize;

    RokidCameraSize(Size size) {
        this.mSize = size;
    }

    public Size getSize() {
        return mSize;
    }

}
