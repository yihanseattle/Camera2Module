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
import com.rokid.glass.rokidcamera.utils.RokidCameraBuilderValidator;
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
    // camera id
    private RokidCameraParameters mRokidCameraParamCameraId;

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
        this.mRokidCameraSizeVideoRecorder = RokidCameraSize.SIZE_VIDEO_RECORDING;
        this.mRokidCameraParamAEMode = RokidCameraParameters.ROKID_CAMERA_PARAM_AE_MODE_ON;
        this.mRokidCameraParamAFMode = RokidCameraParameters.ROKID_CAMERA_PARAM_AF_MODE_PICTURE;
        this.mRokidCameraParamAWBMode = RokidCameraParameters.ROKID_CAMERA_PARAM_AWB_MODE_AUTO;
        this.mRokidCameraParamCameraId = RokidCameraParameters.ROKID_CAMERA_PARAM_CAMERA_ID_ROKID_GLASS;
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

    @Override
    public RokidCameraBuilder setRokidCameraParamCameraID(RokidCameraParameters rokidCameraParameters) {
        this.mRokidCameraParamCameraId = rokidCameraParameters;
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

    public RokidCameraSize getRokidCameraSizePreview() {
        return mRokidCameraSizePreview;
    }

    public RokidCameraSize getRokidCameraSizeImageReader() {
        return mRokidCameraSizeImageReader;
    }

    public RokidCameraSize getRokidCameraSizeVideoRecorder() {
        return mRokidCameraSizeVideoRecorder;
    }

    public RokidCameraParameters getRokidCameraParamAEMode() {
        return mRokidCameraParamAEMode;
    }

    public RokidCameraParameters getRokidCameraParamAFMode() {
        return mRokidCameraParamAFMode;
    }

    public RokidCameraParameters getRokidCameraParamAWBMode() {
        return mRokidCameraParamAWBMode;
    }

    public RokidCameraParameters getRokidCameraParamCameraId() {
        return mRokidCameraParamCameraId;
    }

    public RokidCamera build() {
        validateBuilder(this);
        return new RokidCamera(this);
    }

    private void validateBuilder(RokidCameraBuilder rokidCameraBuilder) {
        RokidCameraBuilderValidator.validateImageFormat(this);
        RokidCameraBuilderValidator.validateMaxImageBuffer(this);
        RokidCameraBuilderValidator.validateImageReaderCallbackMode(this);
        RokidCameraBuilderValidator.validateSizePreview(this);
        RokidCameraBuilderValidator.validateSizeImageReader(this);
        RokidCameraBuilderValidator.validateSizeVideoRecorder(this);
        RokidCameraBuilderValidator.validateParamAEMode(this);
        RokidCameraBuilderValidator.validateParamAFMode(this);
        RokidCameraBuilderValidator.validateParamAWBMode(this);
        RokidCameraBuilderValidator.validateParamCameraId(this);
    }
}
