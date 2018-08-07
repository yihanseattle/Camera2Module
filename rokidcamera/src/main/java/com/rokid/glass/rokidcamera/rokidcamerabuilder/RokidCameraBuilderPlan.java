package com.rokid.glass.rokidcamera.rokidcamerabuilder;

import android.media.Image;
import android.media.ImageReader;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.rokid.glass.rokidcamera.RokidCameraBuilder;
import com.rokid.glass.rokidcamera.RokidCamera;
import com.rokid.glass.rokidcamera.callbacks.RokidCameraIOListener;
import com.rokid.glass.rokidcamera.callbacks.RokidCameraOnImageAvailableListener;
import com.rokid.glass.rokidcamera.callbacks.RokidCameraStateListener;
import com.rokid.glass.rokidcamera.callbacks.RokidCameraVideoRecordingListener;

/**
 * All methods for building RokidCamera.
 *
 * Created by yihan on 7/26/18.
 */

public interface RokidCameraBuilderPlan {

    /**
     * Callback {@link RokidCameraStateListener#onRokidCameraOpened()} is called when RokidCamera is opened.
     *
     * @param rokidCameraStateListener : listener from Activity
     * @return : RokidCameraBuilder
     */
    RokidCameraBuilder setRokidCameraStateListener(@NonNull RokidCameraStateListener rokidCameraStateListener);

    /**
     * Callbacks below will be called when Video Recording state changes:
     *      - Recording starts {@link RokidCameraVideoRecordingListener#onRokidCameraRecordingStarted()}
     *      - Recording ends {@link RokidCameraVideoRecordingListener#onRokidCameraRocordingFinished()}
     *
     * @param rokidCameraVideoRecordingListener : listener from Activity
     * @return : RokidCameraBuilder
     */
    RokidCameraBuilder setRokidCameraRecordingListener(@NonNull RokidCameraVideoRecordingListener rokidCameraVideoRecordingListener);

    /**
     * Set Image related mode and callback.
     *  - Mode {@link RokidCamera#STILL_PHOTO_MODE_SINGLE_NO_CALLBACK} :
     *      - Uses {@link ImageReader#acquireLatestImage()} and will use default path (/sdcard/DCIM/Camera/).
     *      - Callback {@link RokidCameraIOListener#onRokidCameraFileSaved()} is called when saving is done.
     *      - {@link RokidCameraOnImageAvailableListener} will NOT be called and can be null.
     *  - Mode {@link RokidCamera#STILL_PHOTO_MODE_SINGLE_IMAGE_CALLBACK} :
     *      - Uses {@link ImageReader#acquireLatestImage()} and will call {@link RokidCameraOnImageAvailableListener#onRokidCameraImageAvailable(Image)}
     *      to send Image back.
     *      - {@link RokidCameraIOListener} will NOT be called and can be null.
     *  - Mode {@link RokidCamera#STILL_PHOTO_MODE_CONTINUOUS_IMAGE_CALLBACK} :
     *      - Uses {@link ImageReader#acquireNextImage()} and will call {@link RokidCameraOnImageAvailableListener#onRokidCameraImageAvailable(Image)}
     *      to send every Image frame back.
     *      - {@link RokidCameraIOListener} will NOT be called and can be null.
     *
     * @param imageReaderCallbackMode : three modes:    {@link RokidCamera#STILL_PHOTO_MODE_SINGLE_NO_CALLBACK}
     *                                                  {@link RokidCamera#STILL_PHOTO_MODE_SINGLE_IMAGE_CALLBACK}
     *                                                  {@link RokidCamera#STILL_PHOTO_MODE_CONTINUOUS_IMAGE_CALLBACK}
     * @param rokidCameraOnImageAvailableListener : when there is an Image to be used for callback
     * @param rokidCameraIOListener : callback when default saving is done
     * @return : RokidCameraBuilder
     */
    RokidCameraBuilder setRokidCameraOnImageAvailableListener(int imageReaderCallbackMode,
                                                              @Nullable RokidCameraOnImageAvailableListener rokidCameraOnImageAvailableListener,
                                                              @Nullable RokidCameraIOListener rokidCameraIOListener);

    /**
     * Flag to enable the Camera Preview.
     * Camera Preview is disabled by default.
     *
     * @param previewEnabled : true if set to enabled
     * @return : RokidCameraBuilder
     */
    RokidCameraBuilder setPreviewEnabled(boolean previewEnabled);

    /**
     * Set ImageFormat to user specified. Default is set to {@link android.graphics.ImageFormat#JPEG}
     *  - Common types are :
     *      - {@link android.graphics.ImageFormat#JPEG}
     *      - {@link android.graphics.ImageFormat#YUV_420_888}
     *
     * @param imageFormat : input format
     * @return : RokidCameraBuilder
     */
    RokidCameraBuilder setImageFormat(int imageFormat);

    /**
     * MaxImageBuffer size for ImageReader.
     * Refer to `maxImages` in {@link ImageReader#newInstance(int, int, int, int)}
     *
     * @param maxImages : number of Maximum Images
     * @return : RokidCameraBuilder
     */
    RokidCameraBuilder setMaximumImages(int maxImages);
}