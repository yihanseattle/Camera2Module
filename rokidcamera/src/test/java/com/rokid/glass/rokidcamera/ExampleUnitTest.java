package com.rokid.glass.rokidcamera;

import android.app.Activity;
import android.graphics.ImageFormat;
import android.media.Image;
import android.view.TextureView;

import com.rokid.glass.rokidcamera.callbacks.RokidCameraIOListener;
import com.rokid.glass.rokidcamera.callbacks.RokidCameraOnImageAvailableListener;
import com.rokid.glass.rokidcamera.callbacks.RokidCameraStateListener;
import com.rokid.glass.rokidcamera.callbacks.RokidCameraVideoRecordingListener;
import com.rokid.glass.rokidcamera.utils.RokidCameraParameters;
import com.rokid.glass.rokidcamera.utils.RokidCameraSize;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;
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

        System.out.println("RokidCamera Default Config: PASSED");
    }

    @Test
    public void setRokidCameraStateListener_rokidCameraBuilder() throws Exception {
        Activity activity = mock(Activity.class);
        TextureView textureView = mock(TextureView.class);

        RokidCameraStateListener rokidCameraStateListener = new RokidCameraStateListener() {
            @Override
            public void onRokidCameraOpened() {

            }
        };

        RokidCamera rc = new RokidCameraBuilder(activity, textureView)
                .setRokidCameraStateListener(rokidCameraStateListener)
                .build();
        assertFalse(rc.isPreviewEnabled());
        assertEquals(rc.getImageFormat(), ImageFormat.JPEG);
        assertEquals(rc.getMaxImages(), 2);
        assertEquals(rc.getImageReaderCallbackMode(), RokidCamera.STILL_PHOTO_MODE_SINGLE_NO_CALLBACK);
        assertNotNull(rc.getRokidCameraStateListener());
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

        System.out.println("RokidCamera Camera State Listener: PASS");
    }

    @Test
    public void setRokidCameraRecordingListener_rokidCameraBuilder() throws Exception {
        Activity activity = mock(Activity.class);
        TextureView textureView = mock(TextureView.class);
        RokidCameraVideoRecordingListener rokidCameraVideoRecordingListener = new RokidCameraVideoRecordingListener() {
            @Override
            public void onRokidCameraRecordingStarted() {

            }

            @Override
            public void onRokidCameraRocordingFinished() {

            }
        };

        RokidCamera rc = new RokidCameraBuilder(activity, textureView)
                .setRokidCameraRecordingListener(rokidCameraVideoRecordingListener)
                .build();
        assertFalse(rc.isPreviewEnabled());
        assertEquals(rc.getImageFormat(), ImageFormat.JPEG);
        assertEquals(rc.getMaxImages(), 2);
        assertEquals(rc.getImageReaderCallbackMode(), RokidCamera.STILL_PHOTO_MODE_SINGLE_NO_CALLBACK);
        assertNull(rc.getRokidCameraStateListener());
        assertNotNull(rc.getRokidCameraIOListener());
        assertNotNull(rc.getRokidCameraRecordingListener());
        assertNull(rc.getRokidCameraOnImageAvailableListener());
        assertEquals(rc.getSizePreview(), RokidCameraSize.SIZE_PREVIEW);
        assertEquals(rc.getSizeImageReader(), RokidCameraSize.SIZE_IMAGE_READER_STILL_PHOTO);
        assertEquals(rc.getSizeVideoRecorder(), RokidCameraSize.SIZE_VIDEO_RECORDING);
        assertEquals(rc.getRokidCameraParamAEMode(), RokidCameraParameters.ROKID_CAMERA_PARAM_AE_MODE_ON);
        assertEquals(rc.getRokidCameraParamAFMode(), RokidCameraParameters.ROKID_CAMERA_PARAM_AF_MODE_PICTURE);
        assertEquals(rc.getRokidCameraParamAWBMode(), RokidCameraParameters.ROKID_CAMERA_PARAM_AWB_MODE_AUTO);
        assertEquals(rc.getRokidCameraParamCameraId(), RokidCameraParameters.ROKID_CAMERA_PARAM_CAMERA_ID_ROKID_GLASS);

        System.out.println("RokidCamera Recording State Listener: PASS");
    }

    @Test
    public void setRokidCameraOnImageAvailableListener_singlePhotoWithoutCallback_rokidCameraBuilder() throws Exception {
        Activity activity = mock(Activity.class);
        TextureView textureView = mock(TextureView.class);

        RokidCameraIOListener rokidCameraIOListener = new RokidCameraIOListener() {
            @Override
            public void onRokidCameraFileSaved() {

            }
        };

        RokidCamera rc = new RokidCameraBuilder(activity, textureView)
                .setRokidCameraOnImageAvailableListener(RokidCamera.STILL_PHOTO_MODE_SINGLE_NO_CALLBACK,
                        null,
                        rokidCameraIOListener)
                .build();
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

        System.out.println("RokidCamera OnImageAvailable Single Photo No Callback: PASS");
    }

    @Test
    public void setRokidCameraOnImageAvailableListener_singlePhotoWithCallback_rokidCameraBuilder() throws Exception {
        Activity activity = mock(Activity.class);
        TextureView textureView = mock(TextureView.class);

        RokidCameraOnImageAvailableListener rokidCameraOnImageAvailableListener = new RokidCameraOnImageAvailableListener() {
            @Override
            public void onRokidCameraImageAvailable(Image image) {

            }
        };

        RokidCamera rc = new RokidCameraBuilder(activity, textureView)
                .setRokidCameraOnImageAvailableListener(RokidCamera.STILL_PHOTO_MODE_SINGLE_IMAGE_CALLBACK,
                        rokidCameraOnImageAvailableListener,
                        null)
                .build();
        assertFalse(rc.isPreviewEnabled());
        assertEquals(rc.getImageFormat(), ImageFormat.JPEG);
        assertEquals(rc.getMaxImages(), 2);
        assertEquals(rc.getImageReaderCallbackMode(), RokidCamera.STILL_PHOTO_MODE_SINGLE_IMAGE_CALLBACK);
        assertNull(rc.getRokidCameraStateListener());
        assertNull(rc.getRokidCameraIOListener());
        assertNull(rc.getRokidCameraRecordingListener());
        assertNotNull(rc.getRokidCameraOnImageAvailableListener());
        assertEquals(rc.getSizePreview(), RokidCameraSize.SIZE_PREVIEW);
        assertEquals(rc.getSizeImageReader(), RokidCameraSize.SIZE_IMAGE_READER_STILL_PHOTO);
        assertEquals(rc.getSizeVideoRecorder(), RokidCameraSize.SIZE_VIDEO_RECORDING);
        assertEquals(rc.getRokidCameraParamAEMode(), RokidCameraParameters.ROKID_CAMERA_PARAM_AE_MODE_ON);
        assertEquals(rc.getRokidCameraParamAFMode(), RokidCameraParameters.ROKID_CAMERA_PARAM_AF_MODE_PICTURE);
        assertEquals(rc.getRokidCameraParamAWBMode(), RokidCameraParameters.ROKID_CAMERA_PARAM_AWB_MODE_AUTO);
        assertEquals(rc.getRokidCameraParamCameraId(), RokidCameraParameters.ROKID_CAMERA_PARAM_CAMERA_ID_ROKID_GLASS);

        System.out.println("RokidCamera OnImageAvailable Single Photo With Callback: PASS");
    }

    @Test
    public void setRokidCameraOnImageAvailableListener_continuousPhotoWithCallback_rokidCameraBuilder() throws Exception {
        Activity activity = mock(Activity.class);
        TextureView textureView = mock(TextureView.class);

        RokidCameraOnImageAvailableListener rokidCameraOnImageAvailableListener = new RokidCameraOnImageAvailableListener() {
            @Override
            public void onRokidCameraImageAvailable(Image image) {

            }
        };

        RokidCamera rc = new RokidCameraBuilder(activity, textureView)
                .setRokidCameraOnImageAvailableListener(RokidCamera.STILL_PHOTO_MODE_CONTINUOUS_IMAGE_CALLBACK,
                rokidCameraOnImageAvailableListener,
                        null)
                .build();
        assertFalse(rc.isPreviewEnabled());
        assertEquals(rc.getImageFormat(), ImageFormat.JPEG);
        assertEquals(rc.getMaxImages(), 2);
        assertEquals(rc.getImageReaderCallbackMode(), RokidCamera.STILL_PHOTO_MODE_CONTINUOUS_IMAGE_CALLBACK);
        assertNull(rc.getRokidCameraStateListener());
        assertNull(rc.getRokidCameraIOListener());
        assertNull(rc.getRokidCameraRecordingListener());
        assertNotNull(rc.getRokidCameraOnImageAvailableListener());
        assertEquals(rc.getSizePreview(), RokidCameraSize.SIZE_PREVIEW);
        assertEquals(rc.getSizeImageReader(), RokidCameraSize.SIZE_IMAGE_READER_STILL_PHOTO);
        assertEquals(rc.getSizeVideoRecorder(), RokidCameraSize.SIZE_VIDEO_RECORDING);
        assertEquals(rc.getRokidCameraParamAEMode(), RokidCameraParameters.ROKID_CAMERA_PARAM_AE_MODE_ON);
        assertEquals(rc.getRokidCameraParamAFMode(), RokidCameraParameters.ROKID_CAMERA_PARAM_AF_MODE_PICTURE);
        assertEquals(rc.getRokidCameraParamAWBMode(), RokidCameraParameters.ROKID_CAMERA_PARAM_AWB_MODE_AUTO);
        assertEquals(rc.getRokidCameraParamCameraId(), RokidCameraParameters.ROKID_CAMERA_PARAM_CAMERA_ID_ROKID_GLASS);

        System.out.println("RokidCamera OnImageAvailable Continuous Photo With Callback: PASS");
    }

    @Test
    public void setPreviewEnabled_enabled_rokidCameraBuilder() throws Exception {
        Activity activity = mock(Activity.class);
        TextureView textureView = mock(TextureView.class);

        RokidCamera rc = new RokidCameraBuilder(activity, textureView)
                .setPreviewEnabled(true)
                .build();
        assertTrue(rc.isPreviewEnabled());
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

        System.out.println("RokidCamera Preview Enabled: PASS");
    }

    @Test
    public void setPreviewEnabled_disabled_rokidCameraBuilder() throws Exception {
        Activity activity = mock(Activity.class);
        TextureView textureView = mock(TextureView.class);

        RokidCamera rc = new RokidCameraBuilder(activity, textureView)
                .setPreviewEnabled(false)
                .build();
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

        System.out.println("RokidCamera Preview Disabled: PASS");
    }

    @Test
    public void setImageFormat_rokidCameraBuilder() throws Exception {
        Activity activity = mock(Activity.class);
        TextureView textureView = mock(TextureView.class);

        RokidCamera rc = new RokidCameraBuilder(activity, textureView)
                .setImageFormat(ImageFormat.YUV_420_888)
                .build();
        assertFalse(rc.isPreviewEnabled());
        assertEquals(rc.getImageFormat(), ImageFormat.YUV_420_888);
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

        System.out.println("RokidCamera ImageFormat YUV_420_888: PASS");
    }

    @Test
    public void setMaximumImages_rokidCameraBuilder() throws Exception {
        Activity activity = mock(Activity.class);
        TextureView textureView = mock(TextureView.class);

        RokidCamera rc = new RokidCameraBuilder(activity, textureView)
                .setMaximumImages(4)
                .build();
        assertFalse(rc.isPreviewEnabled());
        assertEquals(rc.getImageFormat(), ImageFormat.JPEG);
        assertEquals(rc.getMaxImages(), 4);
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

        System.out.println("RokidCamera Maximum Image Buffer Size = 4 : PASS");
    }

    @Test
    public void setSizePreview_rokidCameraBuilder() throws Exception {
        Activity activity = mock(Activity.class);
        TextureView textureView = mock(TextureView.class);

        RokidCamera rc = new RokidCameraBuilder(activity, textureView)
                .setSizePreview(RokidCameraSize.SIZE_PREVIEW)
                .build();
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

        System.out.println("RokidCamera Preview Size Set to SIZE_PREVIEW: PASS");
    }

    @Test
    public void setSizeImageReader_ARSDK_rokidCameraBuilder() throws Exception {
        Activity activity = mock(Activity.class);
        TextureView textureView = mock(TextureView.class);

        RokidCamera rc = new RokidCameraBuilder(activity, textureView)
                .setSizeImageReader(RokidCameraSize.SIZE_IMAGE_READER_ALGORITHM_ARSDK)
                .build();
        assertFalse(rc.isPreviewEnabled());
        assertEquals(rc.getImageFormat(), ImageFormat.JPEG);
        assertEquals(rc.getMaxImages(), 2);
        assertEquals(rc.getImageReaderCallbackMode(), RokidCamera.STILL_PHOTO_MODE_SINGLE_NO_CALLBACK);
        assertNull(rc.getRokidCameraStateListener());
        assertNotNull(rc.getRokidCameraIOListener());
        assertNull(rc.getRokidCameraRecordingListener());
        assertNull(rc.getRokidCameraOnImageAvailableListener());
        assertEquals(rc.getSizePreview(), RokidCameraSize.SIZE_PREVIEW);
        assertEquals(rc.getSizeImageReader(), RokidCameraSize.SIZE_IMAGE_READER_ALGORITHM_ARSDK);
        assertEquals(rc.getSizeVideoRecorder(), RokidCameraSize.SIZE_VIDEO_RECORDING);
        assertEquals(rc.getRokidCameraParamAEMode(), RokidCameraParameters.ROKID_CAMERA_PARAM_AE_MODE_ON);
        assertEquals(rc.getRokidCameraParamAFMode(), RokidCameraParameters.ROKID_CAMERA_PARAM_AF_MODE_PICTURE);
        assertEquals(rc.getRokidCameraParamAWBMode(), RokidCameraParameters.ROKID_CAMERA_PARAM_AWB_MODE_AUTO);
        assertEquals(rc.getRokidCameraParamCameraId(), RokidCameraParameters.ROKID_CAMERA_PARAM_CAMERA_ID_ROKID_GLASS);

        System.out.println("RokidCamera ImageReader Size Set to SIZE_IMAGE_READER_ALGORITHM_ARSDK: PASS");
    }

    @Test
    public void setSizeImageReader_FaceID_rokidCameraBuilder() throws Exception {
        Activity activity = mock(Activity.class);
        TextureView textureView = mock(TextureView.class);

        RokidCamera rc = new RokidCameraBuilder(activity, textureView)
                .setSizeImageReader(RokidCameraSize.SIZE_IMAGE_READER_ALGORITHM_FACEID)
                .build();
        assertFalse(rc.isPreviewEnabled());
        assertEquals(rc.getImageFormat(), ImageFormat.JPEG);
        assertEquals(rc.getMaxImages(), 2);
        assertEquals(rc.getImageReaderCallbackMode(), RokidCamera.STILL_PHOTO_MODE_SINGLE_NO_CALLBACK);
        assertNull(rc.getRokidCameraStateListener());
        assertNotNull(rc.getRokidCameraIOListener());
        assertNull(rc.getRokidCameraRecordingListener());
        assertNull(rc.getRokidCameraOnImageAvailableListener());
        assertEquals(rc.getSizePreview(), RokidCameraSize.SIZE_PREVIEW);
        assertEquals(rc.getSizeImageReader(), RokidCameraSize.SIZE_IMAGE_READER_ALGORITHM_FACEID);
        assertEquals(rc.getSizeVideoRecorder(), RokidCameraSize.SIZE_VIDEO_RECORDING);
        assertEquals(rc.getRokidCameraParamAEMode(), RokidCameraParameters.ROKID_CAMERA_PARAM_AE_MODE_ON);
        assertEquals(rc.getRokidCameraParamAFMode(), RokidCameraParameters.ROKID_CAMERA_PARAM_AF_MODE_PICTURE);
        assertEquals(rc.getRokidCameraParamAWBMode(), RokidCameraParameters.ROKID_CAMERA_PARAM_AWB_MODE_AUTO);
        assertEquals(rc.getRokidCameraParamCameraId(), RokidCameraParameters.ROKID_CAMERA_PARAM_CAMERA_ID_ROKID_GLASS);

        System.out.println("RokidCamera ImageReader Size Set to SIZE_IMAGE_READER_ALGORITHM_FACEID: PASS");

    }

    @Test
    public void setSizeImageReader_Landmark_rokidCameraBuilder() throws Exception {
        Activity activity = mock(Activity.class);
        TextureView textureView = mock(TextureView.class);

        RokidCamera rc = new RokidCameraBuilder(activity, textureView)
                .setSizeImageReader(RokidCameraSize.SIZE_IMAGE_READER_ALGORITHM_LANDMARK)
                .build();
        assertFalse(rc.isPreviewEnabled());
        assertEquals(rc.getImageFormat(), ImageFormat.JPEG);
        assertEquals(rc.getMaxImages(), 2);
        assertEquals(rc.getImageReaderCallbackMode(), RokidCamera.STILL_PHOTO_MODE_SINGLE_NO_CALLBACK);
        assertNull(rc.getRokidCameraStateListener());
        assertNotNull(rc.getRokidCameraIOListener());
        assertNull(rc.getRokidCameraRecordingListener());
        assertNull(rc.getRokidCameraOnImageAvailableListener());
        assertEquals(rc.getSizePreview(), RokidCameraSize.SIZE_PREVIEW);
        assertEquals(rc.getSizeImageReader(), RokidCameraSize.SIZE_IMAGE_READER_ALGORITHM_LANDMARK);
        assertEquals(rc.getSizeVideoRecorder(), RokidCameraSize.SIZE_VIDEO_RECORDING);
        assertEquals(rc.getRokidCameraParamAEMode(), RokidCameraParameters.ROKID_CAMERA_PARAM_AE_MODE_ON);
        assertEquals(rc.getRokidCameraParamAFMode(), RokidCameraParameters.ROKID_CAMERA_PARAM_AF_MODE_PICTURE);
        assertEquals(rc.getRokidCameraParamAWBMode(), RokidCameraParameters.ROKID_CAMERA_PARAM_AWB_MODE_AUTO);
        assertEquals(rc.getRokidCameraParamCameraId(), RokidCameraParameters.ROKID_CAMERA_PARAM_CAMERA_ID_ROKID_GLASS);

        System.out.println("RokidCamera ImageReader Size Set to SIZE_IMAGE_READER_ALGORITHM_LANDMARK: PASS");

    }

    @Test
    public void setSizeImageReader_SLAM_rokidCameraBuilder() throws Exception {
        Activity activity = mock(Activity.class);
        TextureView textureView = mock(TextureView.class);

        RokidCamera rc = new RokidCameraBuilder(activity, textureView)
                .setSizeImageReader(RokidCameraSize.SIZE_IMAGE_READER_ALGORITHM_SLAM)
                .build();
        assertFalse(rc.isPreviewEnabled());
        assertEquals(rc.getImageFormat(), ImageFormat.JPEG);
        assertEquals(rc.getMaxImages(), 2);
        assertEquals(rc.getImageReaderCallbackMode(), RokidCamera.STILL_PHOTO_MODE_SINGLE_NO_CALLBACK);
        assertNull(rc.getRokidCameraStateListener());
        assertNotNull(rc.getRokidCameraIOListener());
        assertNull(rc.getRokidCameraRecordingListener());
        assertNull(rc.getRokidCameraOnImageAvailableListener());
        assertEquals(rc.getSizePreview(), RokidCameraSize.SIZE_PREVIEW);
        assertEquals(rc.getSizeImageReader(), RokidCameraSize.SIZE_IMAGE_READER_ALGORITHM_SLAM);
        assertEquals(rc.getSizeVideoRecorder(), RokidCameraSize.SIZE_VIDEO_RECORDING);
        assertEquals(rc.getRokidCameraParamAEMode(), RokidCameraParameters.ROKID_CAMERA_PARAM_AE_MODE_ON);
        assertEquals(rc.getRokidCameraParamAFMode(), RokidCameraParameters.ROKID_CAMERA_PARAM_AF_MODE_PICTURE);
        assertEquals(rc.getRokidCameraParamAWBMode(), RokidCameraParameters.ROKID_CAMERA_PARAM_AWB_MODE_AUTO);
        assertEquals(rc.getRokidCameraParamCameraId(), RokidCameraParameters.ROKID_CAMERA_PARAM_CAMERA_ID_ROKID_GLASS);

        System.out.println("RokidCamera ImageReader Size Set to SIZE_IMAGE_READER_ALGORITHM_SLAM: PASS");

    }

    @Test
    public void setSizeImageReader_StillPhoto_rokidCameraBuilder() throws Exception {
        Activity activity = mock(Activity.class);
        TextureView textureView = mock(TextureView.class);

        RokidCamera rc = new RokidCameraBuilder(activity, textureView)
                .setSizeImageReader(RokidCameraSize.SIZE_IMAGE_READER_STILL_PHOTO)
                .build();
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

        System.out.println("RokidCamera ImageReader Size Set to SIZE_IMAGE_READER_STILL_PHOTO: PASS");

    }

    @Test
    public void setSizeVideoRecorder_rokidCameraBuilder() throws Exception {
        Activity activity = mock(Activity.class);
        TextureView textureView = mock(TextureView.class);

        RokidCamera rc = new RokidCameraBuilder(activity, textureView)
                .setSizeVideoRecorder(RokidCameraSize.SIZE_VIDEO_RECORDING)
                .build();
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

        System.out.println("RokidCamera Video Recording Size Set to SIZE_VIDEO_RECORDING: PASS");
    }

    @Test
    public void setRokidCameraParamAEMode_AEOff_rokidCameraBuilder() throws Exception {
        Activity activity = mock(Activity.class);
        TextureView textureView = mock(TextureView.class);

        RokidCamera rc = new RokidCameraBuilder(activity, textureView)
                .setRokidCameraParamAEMode(RokidCameraParameters.ROKID_CAMERA_PARAM_AE_MODE_OFF)
                .build();
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
        assertEquals(rc.getRokidCameraParamAEMode(), RokidCameraParameters.ROKID_CAMERA_PARAM_AE_MODE_OFF);
        assertEquals(rc.getRokidCameraParamAFMode(), RokidCameraParameters.ROKID_CAMERA_PARAM_AF_MODE_PICTURE);
        assertEquals(rc.getRokidCameraParamAWBMode(), RokidCameraParameters.ROKID_CAMERA_PARAM_AWB_MODE_AUTO);
        assertEquals(rc.getRokidCameraParamCameraId(), RokidCameraParameters.ROKID_CAMERA_PARAM_CAMERA_ID_ROKID_GLASS);

        System.out.println("RokidCamera AE Mode Set to OFF: PASS");
    }

    @Test
    public void setRokidCameraParamAEMode_AEOn_rokidCameraBuilder() throws Exception {
        Activity activity = mock(Activity.class);
        TextureView textureView = mock(TextureView.class);

        RokidCamera rc = new RokidCameraBuilder(activity, textureView)
                .setRokidCameraParamAEMode(RokidCameraParameters.ROKID_CAMERA_PARAM_AE_MODE_ON)
                .build();
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

        System.out.println("RokidCamera AE Mode Set to ON: PASS");
    }

    @Test
    public void setRokidCameraParamAFMode_AFAuto_rokidCameraBuilder() throws Exception {
        Activity activity = mock(Activity.class);
        TextureView textureView = mock(TextureView.class);

        RokidCamera rc = new RokidCameraBuilder(activity, textureView)
                .setRokidCameraParamAFMode(RokidCameraParameters.ROKID_CAMERA_PARAM_AF_MODE_AUTO)
                .build();
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
        assertEquals(rc.getRokidCameraParamAFMode(), RokidCameraParameters.ROKID_CAMERA_PARAM_AF_MODE_AUTO);
        assertEquals(rc.getRokidCameraParamAWBMode(), RokidCameraParameters.ROKID_CAMERA_PARAM_AWB_MODE_AUTO);
        assertEquals(rc.getRokidCameraParamCameraId(), RokidCameraParameters.ROKID_CAMERA_PARAM_CAMERA_ID_ROKID_GLASS);

        System.out.println("RokidCamera AF Mode Set to AUTO: PASS");
    }

    @Test
    public void setRokidCameraParamAFMode_AFPicture_rokidCameraBuilder() throws Exception {
        Activity activity = mock(Activity.class);
        TextureView textureView = mock(TextureView.class);

        RokidCamera rc = new RokidCameraBuilder(activity, textureView)
                .setRokidCameraParamAFMode(RokidCameraParameters.ROKID_CAMERA_PARAM_AF_MODE_PICTURE)
                .build();
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

        System.out.println("RokidCamera AF Mode Set to PICTURE: PASS");
    }

    @Test
    public void setRokidCameraParamAFMode_AFVideo_rokidCameraBuilder() throws Exception {
        Activity activity = mock(Activity.class);
        TextureView textureView = mock(TextureView.class);

        RokidCamera rc = new RokidCameraBuilder(activity, textureView)
                .setRokidCameraParamAFMode(RokidCameraParameters.ROKID_CAMERA_PARAM_AF_MODE_VIDEO)
                .build();
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
        assertEquals(rc.getRokidCameraParamAFMode(), RokidCameraParameters.ROKID_CAMERA_PARAM_AF_MODE_VIDEO);
        assertEquals(rc.getRokidCameraParamAWBMode(), RokidCameraParameters.ROKID_CAMERA_PARAM_AWB_MODE_AUTO);
        assertEquals(rc.getRokidCameraParamCameraId(), RokidCameraParameters.ROKID_CAMERA_PARAM_CAMERA_ID_ROKID_GLASS);

        System.out.println("RokidCamera AF Mode Set to VIDEO: PASS");
    }

    @Test
    public void setRokidCameraParamAWBMode_AWBAuto_rokidCameraBuilder() throws Exception {
        Activity activity = mock(Activity.class);
        TextureView textureView = mock(TextureView.class);

        RokidCamera rc = new RokidCameraBuilder(activity, textureView)
                .setRokidCameraParamAWBMode(RokidCameraParameters.ROKID_CAMERA_PARAM_AWB_MODE_AUTO)
                .build();
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

        System.out.println("RokidCamera AWB Mode Set to AUTO: PASS");
    }

    @Test
    public void setRokidCameraParamAWBMode_AWBOff_rokidCameraBuilder() throws Exception {
        Activity activity = mock(Activity.class);
        TextureView textureView = mock(TextureView.class);

        RokidCamera rc = new RokidCameraBuilder(activity, textureView)
                .setRokidCameraParamAWBMode(RokidCameraParameters.ROKID_CAMERA_PARAM_AWB_MODE_OFF)
                .build();
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
        assertEquals(rc.getRokidCameraParamAWBMode(), RokidCameraParameters.ROKID_CAMERA_PARAM_AWB_MODE_OFF);
        assertEquals(rc.getRokidCameraParamCameraId(), RokidCameraParameters.ROKID_CAMERA_PARAM_CAMERA_ID_ROKID_GLASS);

        System.out.println("RokidCamera AWB Mode Set to OFF: PASS");
    }

    @Test
    public void setRokidCameraParamCameraID_RokidGlass_rokidCameraBuilder() throws Exception {
        Activity activity = mock(Activity.class);
        TextureView textureView = mock(TextureView.class);

        RokidCamera rc = new RokidCameraBuilder(activity, textureView)
                .setRokidCameraParamCameraID(RokidCameraParameters.ROKID_CAMERA_PARAM_CAMERA_ID_ROKID_GLASS)
                .build();
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

        System.out.println("RokidCamera Camera ID Set to ROKID_GLASS: PASS");
    }

    @Test
    public void setRokidCameraParamCameraID_FrontCamera_rokidCameraBuilder() throws Exception {
        Activity activity = mock(Activity.class);
        TextureView textureView = mock(TextureView.class);

        RokidCamera rc = new RokidCameraBuilder(activity, textureView)
                .setRokidCameraParamCameraID(RokidCameraParameters.ROKID_CAMERA_PARAM_CAMERA_ID_FRONT)
                .build();
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
        assertEquals(rc.getRokidCameraParamCameraId(), RokidCameraParameters.ROKID_CAMERA_PARAM_CAMERA_ID_FRONT);

        System.out.println("RokidCamera Camera ID Set to FRONT_CAMERA: PASS");
    }

    @Test
    public void setRokidCameraParamCameraID_BackCamera_rokidCameraBuilder() throws Exception {
        Activity activity = mock(Activity.class);
        TextureView textureView = mock(TextureView.class);

        RokidCamera rc = new RokidCameraBuilder(activity, textureView)
                .setRokidCameraParamCameraID(RokidCameraParameters.ROKID_CAMERA_PARAM_CAMERA_ID_BACK)
                .build();
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
        assertEquals(rc.getRokidCameraParamCameraId(), RokidCameraParameters.ROKID_CAMERA_PARAM_CAMERA_ID_BACK);

        System.out.println("RokidCamera Camera ID Set to BACK_CAMERA: PASS");
    }
}