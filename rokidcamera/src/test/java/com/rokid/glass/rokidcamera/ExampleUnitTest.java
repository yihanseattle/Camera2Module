package com.rokid.glass.rokidcamera;

import android.app.Activity;
import android.graphics.ImageFormat;
import android.view.TextureView;

import com.rokid.glass.rokidcamera.utils.RokidCameraParameters;
import com.rokid.glass.rokidcamera.utils.RokidCameraSize;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static org.mockito.Mockito.mock;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void default_rokidCameraBuilder() throws Exception {
        Activity activity = mock(Activity.class);
        TextureView textureView = mock(TextureView.class);

        RokidCamera rc = new RokidCameraBuilder(activity, textureView).build();
        assertFalse(rc.isPreviewEnabled());
        assertEquals(rc.getImageFormat(), ImageFormat.JPEG);
        assertEquals(rc.getMaxImages(), 2);
        assertEquals(rc.getImageReaderCallbackMode(), RokidCamera.STILL_PHOTO_MODE_SINGLE_NO_CALLBACK);
        assertNull(rc.getRokidCameraStateListener());
        assertNotNull(rc.getRokidCameraIOListener());
        assertNull(rc.getRokidCameraRecordingListener());
        assertNull(rc.getRokidCameraOnImageAvailableListener());
        assertEquals(rc.getSizePreview(), RokidCameraSize.SIZE_PREVIEW);
        assertEquals(rc.getSizeImageReader(), RokidCameraSize.SIZE_IMAGE_READER_STILL_PHOTO);
        assertEquals(rc.getSizeVideoRecorder(), RokidCameraSize.SIZE_VIDEO_RECORDING);
        assertEquals(rc.getRokidCameraParamAEMode(), RokidCameraParameters.ROKID_CAMERA_PARAM_AE_MODE_ON);
        assertEquals(rc.getRokidCameraParamAFMode(), RokidCameraParameters.ROKID_CAMERA_PARAM_AF_MODE_PICTURE);
        assertEquals(rc.getRokidCameraParamAWBMode(), RokidCameraParameters.ROKID_CAMERA_PARAM_AWB_MODE_AUTO);
        assertEquals(rc.getRokidCameraParamCameraId(), RokidCameraParameters.ROKID_CAMERA_PARAM_CAMERA_ID_ROKID_GLASS);
    }

}