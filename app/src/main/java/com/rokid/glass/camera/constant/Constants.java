package com.rokid.glass.camera.constant;

/**
 *
 * Constant class
 *
 * Created by yihan on 5/20/18.
 */

public class Constants {

    public static final int VIDEO_PROGRESS_TIMER_RATE = 1000;
    public static final float CAMERA_MODE_TEXT_SIZE_SELECTED = 34;
    public static final float CAMERA_MODE_TEXT_SIZE_DESELECTED = 29;
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

}
