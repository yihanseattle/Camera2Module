package com.rokid.glass.camera.cameramodule.callbacks;

import android.media.Image;

/**
 * Created by yihan on 7/31/18.
 */

public interface RokidCameraOnImageAvailableListener {

    /**
     * Callback when Image available to send back to Activity.
     *
     * @param image : Image in a Camera Frame
     */
    void onRokidCameraImageAvailable(Image image);

}
