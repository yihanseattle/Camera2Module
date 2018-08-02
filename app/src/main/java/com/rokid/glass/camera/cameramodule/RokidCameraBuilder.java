package com.rokid.glass.camera.cameramodule;

import android.app.Activity;
import android.graphics.ImageFormat;
import android.support.annotation.NonNull;
import android.view.TextureView;

import com.rokid.glass.camera.cameramodule.callbacks.RokidCameraIOListener;
import com.rokid.glass.camera.cameramodule.callbacks.RokidCameraOnImageAvailableListener;
import com.rokid.glass.camera.cameramodule.callbacks.RokidCameraStateListener;
import com.rokid.glass.camera.cameramodule.callbacks.RokidCameraVideoRecordingListener;
import com.rokid.glass.camera.cameramodule.rokidcamerabuilder.RokidCameraBuilderPlan;

/**
 *
 * Created by yihan on 7/26/18.
 */

public class RokidCameraBuilder implements RokidCameraBuilderPlan {

    // flag to enable the preview
    private boolean previewEnabled;
    private int mImageFormat;
    private int mMaxImages;
    private int mImageReaderCallbackMode;

    // activity and activity callbacks
    private Activity mActivity;
    // preview texture
    private TextureView mTextureView;
    // callbacks
    private RokidCameraStateListener mRokidCameraStateListener;
    private RokidCameraIOListener mRokidCameraIOListener;
    private RokidCameraVideoRecordingListener mRokidCameraRecordingListener;
    private RokidCameraOnImageAvailableListener mRokidCameraOnImageAvailableListener;

    public RokidCameraBuilder (Activity activity, TextureView textureView) {
        this.mActivity = activity;
        this.mTextureView = textureView;
        setDefaultConfig();
    }

    /**
     * Set default value for RokidCamera configurations.
     *
     */
    private void setDefaultConfig() {
         previewEnabled = false;
         mImageFormat = ImageFormat.JPEG;
         mMaxImages = 2;
         mImageReaderCallbackMode = RokidCamera.STILL_PHOTO_MODE_SINGLE_NO_CALLBACK;
    }

    @Override
    public RokidCameraBuilder setRokidCameraStateListener(@NonNull RokidCameraStateListener rokidCameraStateListener) {
        this.mRokidCameraStateListener = rokidCameraStateListener;
        return this;
    }

    @Override
    public RokidCameraBuilder setRokidCameraRecordingListener(@NonNull RokidCameraVideoRecordingListener rokidCameraRecordingListener) {
        this.mRokidCameraRecordingListener = rokidCameraRecordingListener;
        return this;
    }

    @Override
    public RokidCameraBuilder setRokidCameraOnImageAvailableListener(int imageReaderCallbackMode,
                                                                     @NonNull RokidCameraOnImageAvailableListener rokidCameraOnImageAvailableListener,
                                                                     @NonNull RokidCameraIOListener rokidCameraIOListener) {
        this.mImageReaderCallbackMode = imageReaderCallbackMode;
        this.mRokidCameraOnImageAvailableListener = rokidCameraOnImageAvailableListener;
        this.mRokidCameraIOListener = rokidCameraIOListener;
        return this;
    }

    @Override
    public RokidCameraBuilder setPreviewEnabled(boolean previewEnabled) {
        this.previewEnabled = previewEnabled;
        return this;
    }

    @Override
    public RokidCameraBuilder setImageFormat(int imageFormat) {
        this.mImageFormat = imageFormat;
        return this;
    }

    @Override
    public RokidCameraBuilder setMaximumImages(int maxImages) {
        this.mMaxImages = maxImages;
        return this;
    }

    public int getMaxImages() {
        return mMaxImages;
    }

    public int getImageFormat() {
        return mImageFormat;
    }

    public boolean isPreviewEnabled() {
        return previewEnabled;
    }

    public int getImageReaderCallbackMode() {
        return mImageReaderCallbackMode;
    }

    public Activity getActivity() {
        return mActivity;
    }

    public TextureView getTextureView() {
        return mTextureView;
    }

    public RokidCameraStateListener getRokidCameraStateListener() {
        return mRokidCameraStateListener;
    }

    public RokidCameraIOListener getRokidCameraIOListener() {
        return mRokidCameraIOListener;
    }

    public RokidCameraVideoRecordingListener getRokidCameraRecordingListener() {
        return mRokidCameraRecordingListener;
    }

    public RokidCameraOnImageAvailableListener getRokidCameraOnImageAvailableListener() {
        return mRokidCameraOnImageAvailableListener;
    }

    public RokidCamera build() {
        return new RokidCamera(this);
    }
}
