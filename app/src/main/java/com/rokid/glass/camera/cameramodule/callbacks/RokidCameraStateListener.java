package com.rokid.glass.camera.cameramodule.callbacks;

/**
 * Created by yihan on 7/23/18.
 *
 * Callbacks from CameraDevice inside RokidCamera class. The purpose is to let the user know
 * if camera states has changed.
 *
 * Currently only send callback when:
 *      - RokidCamera is opened
 *
 */

public interface RokidCameraStateListener {

    /**
     * Callback when RokidCamera opened successfully.
     */
    void onRokidCameraOpened();

}
