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
import com.rokid.glass.rokidcamera.utils.RokidCameraSize;

/**
 * All methods for building RokidCamera.
 *
 */

public interface RokidCameraBuilderPlan {

    /**
     * Assign callback for State change listener. Callback {@link RokidCameraStateListener#onRokidCameraOpened()} is called when RokidCamera is opened
     *
     * @param rokidCameraStateListener : listener from Activity
     * @return : RokidCameraBuilder object
     */
    RokidCameraBuilder setRokidCameraStateListener(@NonNull RokidCameraStateListener rokidCameraStateListener);

    /**
     * Assign callback for Recording state change listener
     * <p>Callbacks below will be called when Video Recording state changes:
     * <ul>
     * <li>When Recording starts, the {@link RokidCameraVideoRecordingListener#onRokidCameraRecordingStarted()} will be called
     * <li>When Recording ends, the {@link RokidCameraVideoRecordingListener#onRokidCameraRocordingFinished()} will be called
     * </ul>
     *
     * @param rokidCameraVideoRecordingListener : listener from Activity
     * @return : RokidCameraBuilder object
     */
    RokidCameraBuilder setRokidCameraRecordingListener(@NonNull RokidCameraVideoRecordingListener rokidCameraVideoRecordingListener);

    /**
     * Set Image retrieval mode and callback. There are three Image modes:
     * <p>Mode {@link RokidCamera#STILL_PHOTO_MODE_SINGLE_NO_CALLBACK}
     * <ul>
     *     <li>Uses {@link ImageReader#acquireLatestImage()} and will use default path (/sdcard/DCIM/Camera/)
     *     <li>Callback {@link RokidCameraIOListener#onRokidCameraFileSaved()} is called when saving is done
     *     <li>{@link RokidCameraOnImageAvailableListener} will NOT be called and can be null
     * </ul>
     * <p>Mode {@link RokidCamera#STILL_PHOTO_MODE_SINGLE_IMAGE_CALLBACK}
     * <ul>
     *      <li>Uses {@link ImageReader#acquireLatestImage()} and will call {@link RokidCameraOnImageAvailableListener#onRokidCameraImageAvailable(Image)}
     *      to send Image back.
     *      <li>{@link RokidCameraIOListener} will NOT be called and can be null.
     * </ul>
     * <p>Mode {@link RokidCamera#STILL_PHOTO_MODE_CONTINUOUS_IMAGE_CALLBACK} :
     * <ul>
     *     <li>Uses {@link ImageReader#acquireNextImage()} and will call {@link RokidCameraOnImageAvailableListener#onRokidCameraImageAvailable(Image)}
     *      to send every Image frame back.
     *     <li>{@link RokidCameraIOListener} will NOT be called and can be null.
     * </ul>
     *
     * @param imageReaderCallbackMode : the different Image mode that user wants the Camera to function. Below are three modes supported
     *                                <ul>
     *                                <li>{@link RokidCamera#STILL_PHOTO_MODE_SINGLE_NO_CALLBACK}
     *                                <li>{@link RokidCamera#STILL_PHOTO_MODE_SINGLE_IMAGE_CALLBACK}
     *                                <li>{@link RokidCamera#STILL_PHOTO_MODE_CONTINUOUS_IMAGE_CALLBACK}
     *                                </ul>
     * @param rokidCameraOnImageAvailableListener : the callback used in Image modes that will use callback
     * @param rokidCameraIOListener : callback when Image saving is done and Image mode is {@link RokidCamera#STILL_PHOTO_MODE_SINGLE_NO_CALLBACK}
     * @return : RokidCameraBuilder object
     */
    RokidCameraBuilder setRokidCameraOnImageAvailableListener(int imageReaderCallbackMode,
                                                              @Nullable RokidCameraOnImageAvailableListener rokidCameraOnImageAvailableListener,
                                                              @Nullable RokidCameraIOListener rokidCameraIOListener);

    /**
     * Change visibility of Camera Preview.
     * Camera Preview is disabled by default.
     *
     * @param previewEnabled : enabled if set to TRUE
     * @return : RokidCameraBuilder object
     */
    RokidCameraBuilder setPreviewEnabled(boolean previewEnabled);

    /**
     * Change ImageFormat to user specified output format. Default format is set to {@link android.graphics.ImageFormat#JPEG}
     * <p>Common ImageFormat are :
     * <ul>
     *     <li>{@link android.graphics.ImageFormat#JPEG}
     *     <li>{@link android.graphics.ImageFormat#YUV_420_888}
     * </ul>
     *
     * @param imageFormat : the input format
     * @return : RokidCameraBuilder object
     */
    RokidCameraBuilder setImageFormat(int imageFormat);

    /**
     * Set MaxImageBuffer size for ImageReader. Let the ImageReader create buffer automatically.
     * Refer to `maxImages` in {@link ImageReader#newInstance(int, int, int, int)}
     *
     * @param maxImages : number of Maximum Images buffer
     * @return : RokidCameraBuilder object
     */
    RokidCameraBuilder setMaximumImages(int maxImages);

    RokidCameraBuilder setSizePreview(RokidCameraSize rokidCameraSize);

    RokidCameraBuilder setSizeImageReader(RokidCameraSize rokidCameraSize);

    RokidCameraBuilder setSizeVideoRecorder(RokidCameraSize rokidCameraSize);
}