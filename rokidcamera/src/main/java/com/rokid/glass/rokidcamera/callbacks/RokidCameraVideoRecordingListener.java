package com.rokid.glass.rokidcamera.callbacks;

/**
 * Callback for Video Recording status.
 * One for recording started and one for Recording ended.
 *
 * Created by yihan on 7/23/18.
 */

public interface RokidCameraVideoRecordingListener {

    /**
     * Callback when video recording started.
     */
    void onRokidCameraRecordingStarted();

    /**
     * Callback when video recording ended.
     */
    void onRokidCameraRocordingFinished();

}
