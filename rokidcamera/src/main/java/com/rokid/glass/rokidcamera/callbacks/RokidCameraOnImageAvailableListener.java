package com.rokid.glass.rokidcamera.callbacks;

import android.media.Image;

/**
 * Image callback for Image from ImageReader. RokidCamera will send the image back to user Activity.
 */
public interface RokidCameraOnImageAvailableListener {
    /**
     * Callback when Image available to send back to Activity.
     *
     * @param image : Image in a Camera Frame
     */
    void onRokidCameraImageAvailable(Image image);
}
