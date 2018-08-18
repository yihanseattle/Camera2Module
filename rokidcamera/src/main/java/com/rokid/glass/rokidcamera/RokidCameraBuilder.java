package com.rokid.glass.rokidcamera;

import android.app.Activity;
import android.graphics.ImageFormat;
import android.support.annotation.NonNull;
import android.view.TextureView;

import com.rokid.glass.rokidcamera.callbacks.RokidCameraIOListener;
import com.rokid.glass.rokidcamera.callbacks.RokidCameraOnImageAvailableListener;
import com.rokid.glass.rokidcamera.callbacks.RokidCameraStateListener;
import com.rokid.glass.rokidcamera.callbacks.RokidCameraVideoRecordingListener;
import com.rokid.glass.rokidcamera.rokidcamerabuilder.RokidCameraBuilderPlan;
import com.rokid.glass.rokidcamera.utils.RokidCameraParameters;
import com.rokid.glass.rokidcamera.utils.RokidCameraSize;

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
    // resolution sizes
    private RokidCameraSize mRokidCameraSizePreview;
    private RokidCameraSize mRokidCameraSizeImageReader;
    private RokidCameraSize mRokidCameraSizeVideoRecorder;
    // camera parameters
    private RokidCameraParameters mRokidCameraParamAEMode;
    private RokidCameraParameters mRokidCameraParamAFMode;
    private RokidCameraParameters mRokidCameraParamAWBMode;

    // initialize default configurations
    {
        this.previewEnabled = false;
        this.mImageFormat = ImageFormat.JPEG;
        this.mMaxImages = 2;
        this.mImageReaderCallbackMode = RokidCamera.STILL_PHOTO_MODE_SINGLE_NO_CALLBACK;
        this.mRokidCameraStateListener = null;
        this.mRokidCameraIOListener = null;
        this.mRokidCameraRecordingListener = null;
        this.mRokidCameraOnImageAvailableListener = null;
        this.mRokidCameraSizePreview = RokidCameraSize.SIZE_PREVIEW;
        this.mRokidCameraSizeImageReader = RokidCameraSize.SIZE_IMAGE_READER_STILL_PHOTO;
        this.mRokidCameraSizeImageReader = RokidCameraSize.SIZE_VIDEO_RECORDING;
        this.mRokidCameraParamAEMode = RokidCameraParameters.ROKID_CAMERA_PARAM_AE_MODE_ON;
        this.mRokidCameraParamAFMode = RokidCameraParameters.ROKID_CAMERA_PARAM_AF_MODE_PICTURE;
        this.mRokidCameraParamAWBMode = RokidCameraParameters.ROKID_CAMERA_PARAM_AWB_MODE_AUTO;
    }

    public RokidCameraBuilder (Activity activity, TextureView textureView) {
        this.mActivity = activity;
        this.mTextureView = textureView;
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

    @Override
    public RokidCameraBuilder setSizePreview(RokidCameraSize rokidCameraSize) {
        this.mRokidCameraSizePreview = rokidCameraSize;
        return this;
    }

    @Override
    public RokidCameraBuilder setSizeImageReader(RokidCameraSize rokidCameraSize) {
        this.mRokidCameraSizeImageReader = rokidCameraSize;
        return this;
    }

    @Override
    public RokidCameraBuilder setSizeVideoRecorder(RokidCameraSize rokidCameraSize) {
        this.mRokidCameraSizeVideoRecorder = rokidCameraSize;
        return this;
    }

    @Override
    public RokidCameraBuilder setRokidCameraParamAEMode(RokidCameraParameters rokidCameraParameters) {
        this.mRokidCameraParamAEMode = rokidCameraParameters;
        return this;
    }

    @Override
    public RokidCameraBuilder setRokidCameraParamAFMode(RokidCameraParameters rokidCameraParameters) {
        this.mRokidCameraParamAFMode = rokidCameraParameters;
        return this;
    }

    @Override
    public RokidCameraBuilder setRokidCameraParamAWBMode(RokidCameraParameters rokidCameraParameters) {
        this.mRokidCameraParamAWBMode = rokidCameraParameters;
        return this;
    }

    int getMaxImages() {
        return mMaxImages;
    }

    int getImageFormat() {
        return mImageFormat;
    }

    boolean isPreviewEnabled() {
        return previewEnabled;
    }

    int getImageReaderCallbackMode() {
        return mImageReaderCallbackMode;
    }

    Activity getActivity() {
        return mActivity;
    }

    TextureView getTextureView() {
        return mTextureView;
    }

    RokidCameraStateListener getRokidCameraStateListener() {
        return mRokidCameraStateListener;
    }

    RokidCameraIOListener getRokidCameraIOListener() {
        return mRokidCameraIOListener;
    }

    RokidCameraVideoRecordingListener getRokidCameraRecordingListener() {
        return mRokidCameraRecordingListener;
    }

    RokidCameraOnImageAvailableListener getRokidCameraOnImageAvailableListener() {
        return mRokidCameraOnImageAvailableListener;
    }

    RokidCameraSize getRokidCameraSizePreview() {
        return mRokidCameraSizePreview;
    }

    RokidCameraSize getRokidCameraSizeImageReader() {
        return mRokidCameraSizeImageReader;
    }

    RokidCameraSize getRokidCameraSizeVideoRecorder() {
        return mRokidCameraSizeVideoRecorder;
    }

    RokidCameraParameters getRokidCameraParamAEMode() {
        return mRokidCameraParamAEMode;
    }

    RokidCameraParameters getRokidCameraParamAFMode() {
        return mRokidCameraParamAFMode;
    }

    public RokidCameraParameters getRokidCameraParamAWBMode() {
        return mRokidCameraParamAWBMode;
    }

    public RokidCamera build() {
        return new RokidCamera(this);
    }
}
