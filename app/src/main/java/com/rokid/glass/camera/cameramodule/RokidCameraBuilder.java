package com.rokid.glass.camera.cameramodule;

import android.app.Activity;
import android.view.TextureView;

import com.rokid.glass.camera.cameramodule.callbacks.RokidCameraIOListener;
import com.rokid.glass.camera.cameramodule.callbacks.RokidCameraStateListener;
import com.rokid.glass.camera.cameramodule.callbacks.RokidCameraVideoRecordingListener;

/**
 * Created by yihan on 7/26/18.
 */

public class RokidCameraBuilder {

    // flag to enable the preview
    private boolean previewEnabled;

    private Activity mActivity;

    // preview texture
    private TextureView mTextureView;

    private RokidCameraStateListener mRokidCameraStateListener;
    private RokidCameraIOListener mRokidCameraIOListener;
    private RokidCameraVideoRecordingListener mRokidCameraRecordingListener;

    public RokidCameraBuilder (Activity activity, TextureView textureView) {
        this.mActivity = activity;
        this.mTextureView = textureView;
    }

    public RokidCameraBuilder setRokidCameraStateListener(RokidCameraStateListener rokidCameraStateListener) {
        mRokidCameraStateListener = rokidCameraStateListener;
        return this;
    }

    public RokidCameraBuilder setRokidCameraIOListener(RokidCameraIOListener rokidCameraIOListener) {
        mRokidCameraIOListener = rokidCameraIOListener;
        return this;
    }

    public RokidCameraBuilder setRokidCameraRecordingListener(RokidCameraVideoRecordingListener rokidCameraRecordingListener) {
        mRokidCameraRecordingListener = rokidCameraRecordingListener;
        return this;
    }

    public RokidCamera build() {
        RokidCamera rokidCamera = new RokidCamera(mActivity, mTextureView);
        rokidCamera.setRokidCameraStateListener(mRokidCameraStateListener);
        rokidCamera.setRokidCameraIOListener(mRokidCameraIOListener);
        rokidCamera.setRokidCameraRecordingListener(mRokidCameraRecordingListener);
        return rokidCamera;
    }
}
