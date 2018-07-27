package com.rokid.glass.camera.cameramodule.rokidcamerabuilder;

import com.rokid.glass.camera.cameramodule.RokidCameraBuilder;
import com.rokid.glass.camera.cameramodule.callbacks.RokidCameraIOListener;
import com.rokid.glass.camera.cameramodule.callbacks.RokidCameraStateListener;
import com.rokid.glass.camera.cameramodule.callbacks.RokidCameraVideoRecordingListener;

/**
 * Created by yihan on 7/26/18.
 */

public interface RokidCameraBuilderPlan {

    RokidCameraBuilder setRokidCameraStateListener(RokidCameraStateListener rokidCameraStateListener);

    RokidCameraBuilder setRokidCameraIOListener(RokidCameraIOListener rokidCameraIOListener);

    RokidCameraBuilder setRokidCameraRecordingListener(RokidCameraVideoRecordingListener rokidCameraVideoRecordingListener);

    RokidCameraBuilder setPreviewEnabled(boolean previewEnabled);
}