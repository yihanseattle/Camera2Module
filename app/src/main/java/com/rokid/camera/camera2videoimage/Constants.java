package com.rokid.camera.camera2videoimage;

import android.util.Size;

/**
 * Created by yihan on 5/20/18.
 */

public class Constants {

    public static final int VIDEO_PROGRESS_TIMER_RATE = 1000;
    public static final float CAMERA_MODE_TEXT_SIZE_SELECTED = 22;
    public static final float CAMERA_MODE_TEXT_SIZE_DESELECTED = 20;
    public static final String CAMERA_MODE_TEXT_COLOR_SELECTED = "#D8D8D8";
    public static final String CAMERA_MODE_TEXT_COLOR_DESELECTED = "#A5A5A5";
    public static final int CAMERA_MODE_TEXT_PADDING_TOP = 3;
    public static final int CAMERA_MODE_TEXT_PADDING_LEFT = 9;
    public static final int CAMERA_MODE_TEXT_PADDING_RIGHT = 9;

    public static final int REQUEST_CAMERA_PERMISSION_RESULT = 0;
    public static final int REQUEST_WRITE_EXTERNAL_STORAGE_PERMISSION_RESULT = 1;
    public static final int STATE_PREVIEW = 0;
    public static final int STATE_WAIT_LOCK = 1;
    public static int mCaptureState = STATE_PREVIEW;

    /** ============== Customized constants for background image processing ============== **/
    /**
     * The {@link Size} of background image processing.
     *
     * New (12/22/2017): They change to 640 480.
     * */
    public static Size IMAGE_SIZE = new Size(640, 480);

    public static Size PREVIEW_SIZE = new Size(1920, 1080);
}
