package com.rokid.glass.rokidcamera;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.media.Image;
import android.media.ImageReader;
import android.media.MediaRecorder;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.Surface;
import android.view.TextureView;
import android.view.ViewGroup;
import android.widget.Toast;

import com.rokid.glass.rokidcamera.callbacks.RokidCameraIOListener;
import com.rokid.glass.rokidcamera.callbacks.RokidCameraOnImageAvailableListener;
import com.rokid.glass.rokidcamera.callbacks.RokidCameraStateListener;
import com.rokid.glass.rokidcamera.callbacks.RokidCameraVideoRecordingListener;
import com.rokid.glass.rokidcamera.utils.CameraDeviceUtils;
import com.rokid.glass.rokidcamera.utils.RokidCameraParameters;
import com.rokid.glass.rokidcamera.utils.RokidCameraSize;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

import static android.hardware.camera2.CaptureRequest.CONTROL_AE_MODE;
import static android.hardware.camera2.CaptureRequest.CONTROL_AF_MODE;
import static android.hardware.camera2.CaptureRequest.CONTROL_AWB_MODE;

/**
 * Camera Module that can be use for projects with needs for Camera features.
 *
 * This class can initialize, set up, and connect to CameraDevice. Set different parameters according to specific applications for Rokid Glass.
 *
 *
 * Created by yihan on 7/23/18.
 */

public class RokidCamera {

    // SDK variables
    private Activity mActivity;
    private RokidCameraStateListener mRokidCameraStateListener;
    private RokidCameraIOListener mRokidCameraIOListener;
    private RokidCameraVideoRecordingListener mRokidCameraRecordingListener;
    private RokidCameraOnImageAvailableListener mRokidCameraOnImageAvailableListener;
    // flags
    private boolean mPreviewEnabled;
    private int mImageFormat;
    private int mMaxImages;
    private int mImageReaderCallbackMode;

    // resolution size
    private RokidCameraSize mSizePreview;
    private RokidCameraSize mSizeVideoRecorder;
    private RokidCameraSize mSizeImageReader;

    // camera parameters
    private RokidCameraParameters mRokidCameraParamAEMode;
    private RokidCameraParameters mRokidCameraParamAFMode;
    private RokidCameraParameters mRokidCameraParamAWBMode;
    // camera id
    private RokidCameraParameters mRokidCameraParamCameraId;

    // public static variables
    /** Single photo with no callback. Will use default path (/sdcard/DCIM/Camera) for saving. */
    public static int STILL_PHOTO_MODE_SINGLE_NO_CALLBACK = 0;
    /** Single photo with one Image callback. So user can handle the Image on their own. */
    public static int STILL_PHOTO_MODE_SINGLE_IMAGE_CALLBACK = 1;
    /** CONTINUOUS photo with Image callback to Activity. So user can process the Image. */
    public static int STILL_PHOTO_MODE_CONTINUOUS_IMAGE_CALLBACK = 2;

    // preview texture
    private TextureView   mTextureView;
    private TextureView.SurfaceTextureListener mSurfaceTextureListener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int width, int height) {
            setupCamera(width, height);
            connectCamera();
            configureTransform(width, height);
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int width, int height) {
            configureTransform(width, height);
        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
            return false;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {

        }
    };

    // main components
    private CameraDevice mCameraDevice;
    private CameraDevice.StateCallback mCameraDevicesStateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice cameraDevice) {

            mCameraDevice = cameraDevice;


            startPreview();

            // callbacks to user
            if (mRokidCameraStateListener != null) {
                mRokidCameraStateListener.onRokidCameraOpened();
            }
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice cameraDevice) {
            cameraDevice.close();
            mCameraDevice = null;
        }

        @Override
        public void onError(@NonNull CameraDevice cameraDevice, int i) {
            cameraDevice.close();
            mCameraDevice = null;
        }
    };

    // preview callback
    private CameraCaptureSession mPreviewCaptureSession;
    private CameraCaptureSession.CaptureCallback mPreviewCaptureCallback = new CameraCaptureSession.CaptureCallback() {

        private void process(CaptureResult captureResult) {
            switch (mCaptureState) {
                case STATE_PREVIEW:
                    // do nothing
                    break;
                case STATE_WAIT_LOCK:
                    mCaptureState = STATE_PREVIEW;
                    Integer afState = captureResult.get(CaptureResult.CONTROL_AF_STATE);
                    if (afState != null && (afState == CaptureResult.CONTROL_AF_STATE_FOCUSED_LOCKED ||
                            afState == CaptureResult.CONTROL_AF_STATE_NOT_FOCUSED_LOCKED)) {
                        if (mActivity != null) {
                            Toast.makeText(mActivity, "AF Locked!", Toast.LENGTH_SHORT).show();
                        }
                        sendStillCaptureRequest();
                    }
                    break;
            }
        }

        @Override
        public void onCaptureCompleted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull TotalCaptureResult result) {
            super.onCaptureCompleted(session, request, result);

            process(result);
        }
    };

    // camera parameter
    private String mCameraId;
    private int mTotalRotation;

    // auto-focus lock
    private static final int STATE_PREVIEW = 0;
    private static final int STATE_WAIT_LOCK = 1;
    private int mCaptureState = STATE_PREVIEW;
    private boolean mAutoFocusSupported;

    // orientation calculate
    private static SparseIntArray ORIENTATIONS = new SparseIntArray();
    static {
        ORIENTATIONS.append(Surface.ROTATION_0, 90);
        ORIENTATIONS.append(Surface.ROTATION_90, 0);
        ORIENTATIONS.append(Surface.ROTATION_180, 270);
        ORIENTATIONS.append(Surface.ROTATION_270, 180);
    }

    private ImageReader mImageReader;
    private final ImageReader.OnImageAvailableListener mOnImageAvailableListener = new ImageReader.OnImageAvailableListener() {
        @Override
        public void onImageAvailable(ImageReader imageReader) {

            if (mImageReaderCallbackMode == STILL_PHOTO_MODE_CONTINUOUS_IMAGE_CALLBACK) {
                // use case: algorithm
                // use `acquireNextImage()` here because we need continuous image for algorithm

                // TODO: don't use this for now. Use acquireLatestImage() instead.
//                Image image = imageReader.acquireNextImage();


//                long startTime = System.nanoTime();
                Image image = imageReader.acquireLatestImage();
//                long estimatedTime = System.nanoTime() - startTime;
//                Log.i("testtest", "acquireLatestImage time in nano: " + estimatedTime);
                if (image != null) {
                    mBackgroundHandler.post(new ImageCallback(image));
                }

            } else {
                // use background thread to save the image
                // use `acquireLatestImage()` here because we only need one image
                Image image = imageReader.acquireLatestImage();

                if (mImageReaderCallbackMode == STILL_PHOTO_MODE_SINGLE_IMAGE_CALLBACK) {
                    if (image != null) {
                        mBackgroundHandler.post(new ImageCallback(image));
                    }
                } else if (mImageReaderCallbackMode == STILL_PHOTO_MODE_SINGLE_NO_CALLBACK) {
                    // save to SD card
                    if (image != null) {
                        mBackgroundHandler.post(new ImageSaver(image));
                    }
                }
            }
        }
    };

    // request builder for still photo and video
    private CaptureRequest.Builder mCaptureRequestBuilder;
    // media recorder for video recorder
    private MediaRecorder mMediaRecorder;

    private class ImageCallback implements Runnable {

        private final Image mImage;

        ImageCallback(Image image) {
            mImage = image;
        }

        @Override
        public void run() {

            try {
                mRokidCameraOnImageAvailableListener.onRokidCameraImageAvailable(mImage);
            } finally {
                mImage.close();
            }

        }
    }

    private class ImageSaver implements Runnable {

        private final Image mImage;
        ImageSaver(Image image) {
            mImage = image;
        }

        @Override
        public void run() {

            FileOutputStream fileOutputStream = null;
            try {
                // use File to save
                if (mImageFileName != null) {
                    ByteBuffer byteBuffer = mImage.getPlanes()[0].getBuffer();
                    byte[] bytes = new byte[byteBuffer.remaining()];
                    byteBuffer.get(bytes);

                    fileOutputStream = new FileOutputStream(mImageFileName);
                    fileOutputStream.write(bytes);
                    Log.i("testtest", "thread finished ");

                    // reset to null for the next incoming Image
                    mImageFileName = null;

                    // callback to user
                    if (mRokidCameraIOListener != null) {
                        mRokidCameraIOListener.onRokidCameraFileSaved();
                    }


                    // send global notification for new photo taken
                    // so that the gallery app can view new photo
                    final Intent scanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                    final Uri contentUri = Uri.fromFile(mImageFileTest.getAbsoluteFile());
                    scanIntent.setData(contentUri);
                    mActivity.sendBroadcast(scanIntent);

                    MediaScannerConnection.scanFile(
                            mActivity,
                            new String[]{mImageFolder.getAbsolutePath()},
                            null,
                            new MediaScannerConnection.OnScanCompletedListener() {
                                @Override
                                public void onScanCompleted(String path, Uri uri) {
                                    Log.v("testtest",
                                            "file " + path + " was scanned seccessfully: " + uri);
                                }
                            });


                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                mImage.close();
                if (fileOutputStream != null) {
                    try {
                        fileOutputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    // background thread for camera API actions and saving image to SD card
    private HandlerThread mBackgroundHandlerThread;
    private Handler mBackgroundHandler;

    // destination folder
    private File mVideoFolder;
    private String mVideoFileName;
    private File mVideoFileTest;

    private File mImageFolder;
    private String mImageFileName;
    private File mImageFileTest;

    /**
     * User RokidCameraBuilder to create an instance of RokidCamera
     *
     * @param rokidCameraBuilder : user specified RokidCameraBuilder
     */
    RokidCamera(RokidCameraBuilder rokidCameraBuilder) {
        this(rokidCameraBuilder.getActivity(), rokidCameraBuilder.getTextureView());
        this.mRokidCameraStateListener = rokidCameraBuilder.getRokidCameraStateListener();
        this.mRokidCameraIOListener = rokidCameraBuilder.getRokidCameraIOListener();
        this.mRokidCameraRecordingListener = rokidCameraBuilder.getRokidCameraRecordingListener();
        this.mRokidCameraOnImageAvailableListener = rokidCameraBuilder.getRokidCameraOnImageAvailableListener();
        this.mImageReaderCallbackMode = rokidCameraBuilder.getImageReaderCallbackMode();
        this.mPreviewEnabled = rokidCameraBuilder.isPreviewEnabled();
        this.mImageFormat = rokidCameraBuilder.getImageFormat();
        this.mMaxImages = rokidCameraBuilder.getMaxImages();
        this.mSizePreview = rokidCameraBuilder.getRokidCameraSizePreview();
        this.mSizeImageReader = rokidCameraBuilder.getRokidCameraSizeImageReader();
        this.mSizeVideoRecorder = rokidCameraBuilder.getRokidCameraSizeVideoRecorder();
        this.mRokidCameraParamAFMode = rokidCameraBuilder.getRokidCameraParamAFMode();
        this.mRokidCameraParamAEMode = rokidCameraBuilder.getRokidCameraParamAEMode();
        this.mRokidCameraParamAWBMode = rokidCameraBuilder.getRokidCameraParamAWBMode();
        this.mRokidCameraParamCameraId = rokidCameraBuilder.getRokidCameraParamCameraId();
    }

    /**
     * Minimum constructor because RokidCamera will need at least an Activity and a TextureView.
     * User can choose to add callback using the later setter methods.
     *
     * @param activity      : App Activity
     * @param textureView   : App UI TextureView
     */
    private RokidCamera(Activity activity, TextureView textureView) {
        this.mActivity = activity;
        this.mTextureView = textureView;
    }

    /**
     * Background thread for saving images to SD card
     */
    public void onStart() {

        mVideoFolder = createVideoFolder();
        mImageFolder = createImageFolder();
        mMediaRecorder = new MediaRecorder();

        startBackgroundThread();

        if (mTextureView.isAvailable()) {
            // TODO: see Google Example add comments
            // pause and resume
            setupCamera(mTextureView.getWidth(), mTextureView.getHeight());
            connectCamera();

        } else {
            // first time
            mTextureView.setSurfaceTextureListener(mSurfaceTextureListener);
        }
    }

    public void onStop() {
        closeCamera();

        // TODO: look for background thread finish
        stopBackgroundThread();
    }

    /**
     * Start background thread
     */
    private void startBackgroundThread() {
        mBackgroundHandlerThread = new HandlerThread("Camera2VideoImage");
        mBackgroundHandlerThread.start();
        mBackgroundHandler = new Handler(mBackgroundHandlerThread.getLooper());
    }

    /**
     * Setup camera parameters:
     *      - Rotation Degree
     *      - Preview Size
     *      - Video Recording Size
     *      - ImageReader Size
     *      - Auto-Focus Support
     *      - CameraID (If has multiple cameras)
     *
     * @param width     : TexutreView width
     * @param height    : TexutreView height
     */
    private void setupCamera(int width, int height) {
        CameraManager cameraManager = (CameraManager) mActivity.getSystemService(Context.CAMERA_SERVICE);

        if (cameraManager == null) {
            return;
        }

        try {
            for (String cameraId : cameraManager.getCameraIdList()) {
                // get camera characteristics
                CameraCharacteristics cameraCharacteristics = cameraManager.getCameraCharacteristics(cameraId);

                Integer currentCameraId = cameraCharacteristics.get(CameraCharacteristics.LENS_FACING);
                if (currentCameraId == null) {
                    // The return value of that key could be null if the field is not set.
                    return;
                }
                if (currentCameraId != mRokidCameraParamCameraId.getParam()) {
                    // if not desired Camera ID, skip
                    continue;
                }
                int deviceOrientation = mActivity.getWindowManager().getDefaultDisplay().getRotation();
                mTotalRotation = CameraDeviceUtils.sensorToDeviceRotation(cameraCharacteristics, deviceOrientation, ORIENTATIONS);

                mImageReader = ImageReader.newInstance(mSizeImageReader.getSize().getWidth(), mSizeImageReader.getSize().getHeight(), mImageFormat, mMaxImages);
                mImageReader.setOnImageAvailableListener(mOnImageAvailableListener, mBackgroundHandler);

                // Check if auto focus is supported
                int[] afAvailableModes = cameraCharacteristics.get(
                        CameraCharacteristics.CONTROL_AF_AVAILABLE_MODES);
                if (afAvailableModes.length == 0 ||
                        (afAvailableModes.length == 1
                                && afAvailableModes[0] == CameraMetadata.CONTROL_AF_MODE_OFF)) {
                    mAutoFocusSupported = false;
                } else {
                    mAutoFocusSupported = true;
                }

                mCameraId = cameraId;
                return;
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    public int getSensorOrientation() {
        CameraCharacteristics cameraCharacteristics = null;
        try {
            CameraManager cameraManager = (CameraManager) mActivity.getSystemService(Context.CAMERA_SERVICE);
            cameraCharacteristics = cameraManager.getCameraCharacteristics(mCameraId);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }

        if (cameraCharacteristics == null) {
            return -1;
        } else {
            return cameraCharacteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);
        }
    }

    /**
     * Opening Camera via CameraManager
     */
    private void connectCamera() {
        CameraManager cameraManager = (CameraManager) mActivity.getSystemService(Context.CAMERA_SERVICE);
        try {
            if (ContextCompat.checkSelfPermission(mActivity, Manifest.permission.CAMERA) ==
                    PackageManager.PERMISSION_GRANTED) {
                // connect the camera
                // TODO: add comments
                cameraManager.openCamera(mCameraId, mCameraDevicesStateCallback, mBackgroundHandler);
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    /**
     * Start preview
     */
    public void startPreview() {

        // disable preview if necessary
        if (!mPreviewEnabled) {
            final ViewGroup.LayoutParams layoutParams = mTextureView.getLayoutParams();
            layoutParams.height = 1;
            layoutParams.width = 1;
            mActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mTextureView.setLayoutParams(layoutParams);
                }
            });
        }

        SurfaceTexture surfaceTexture = mTextureView.getSurfaceTexture();
        assert surfaceTexture != null;
        surfaceTexture.setDefaultBufferSize(mSizePreview.getSize().getWidth(), mSizePreview.getSize().getHeight());
        Surface previewSurface = new Surface(surfaceTexture);

        try {
            // create Request for Preview template
            /**
             * Create a request suitable for a camera preview window. Specifically, this
             * means that high frame rate is given priority over the highest-quality
             * post-processing. These requests would normally be used with the
             * {@link CameraCaptureSession#setRepeatingRequest} method.
             * This template is guaranteed to be supported on all camera devices.
             *
             * @see #createCaptureRequest
             */
            mCaptureRequestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            configureCameraParameters(mCaptureRequestBuilder, mRokidCameraParamAEMode, mRokidCameraParamAFMode, mRokidCameraParamAWBMode);

//            if (mPreviewEnabled) {
                mCaptureRequestBuilder.addTarget(previewSurface);
//            }

            // only add ImageReader when the mode is STILL_PHOTO_MODE_CONTINUOUS_IMAGE_CALLBACK
            // in other words, only add ImageReader when the app is used by algorithm alike
            if (mImageReaderCallbackMode == STILL_PHOTO_MODE_CONTINUOUS_IMAGE_CALLBACK) {
                mCaptureRequestBuilder.addTarget(mImageReader.getSurface());
            }

            mCameraDevice.createCaptureSession(Arrays.asList(previewSurface, mImageReader.getSurface()), new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {

                    mPreviewCaptureSession = cameraCaptureSession;

                    // preview is a video, so we set a repeating request
                    try {
                        mPreviewCaptureSession.setRepeatingRequest(mCaptureRequestBuilder.build(), null, mBackgroundHandler);
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {
                    Toast.makeText(mActivity, "Unable to setup camera preview", Toast.LENGTH_SHORT).show();
                }
            }, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    public void takeStillPicture() {
        if (mAutoFocusSupported) {
            // try to auto focus
            lockFocus();
        } else {
            // capture right now if auto-focus not supported
            sendStillCaptureRequest();
        }
    }

    /**
     * Auto-focus lock
     */
    private void lockFocus() {
        mCaptureState = STATE_WAIT_LOCK;
        try {
            mCaptureRequestBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER, CaptureRequest.CONTROL_AF_TRIGGER_START);
            mPreviewCaptureSession.capture(mCaptureRequestBuilder.build(), mPreviewCaptureCallback, mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    /**
     * Capture still photo
     */
    private void sendStillCaptureRequest() {
        try {
            // create request for STILL PICTURE type
            /**
             * Create a request suitable for still image capture. Specifically, this
             * means prioritizing image quality over frame rate. These requests would
             * commonly be used with the {@link CameraCaptureSession#capture} method.
             * This template is guaranteed to be supported on all camera devices except
             * {@link CameraMetadata#REQUEST_AVAILABLE_CAPABILITIES_DEPTH_OUTPUT DEPTH_OUTPUT} devices
             * that are not {@link CameraMetadata#REQUEST_AVAILABLE_CAPABILITIES_BACKWARD_COMPATIBLE
             * BACKWARD_COMPATIBLE}.
             * @see #createCaptureRequest
             */
            mCaptureRequestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
            configureCameraParameters(mCaptureRequestBuilder, mRokidCameraParamAEMode, mRokidCameraParamAFMode, mRokidCameraParamAWBMode);
            mCaptureRequestBuilder.addTarget(mImageReader.getSurface());

            // TODO: update diagram

            // not sure why we need to add 180 rotation here
            // the original image was 180 degree off
            mCaptureRequestBuilder.set(CaptureRequest.JPEG_ORIENTATION, mTotalRotation);

            CameraCaptureSession.CaptureCallback stillCaptureCallback = new CameraCaptureSession.CaptureCallback() {
                @Override
                public void onCaptureStarted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, long timestamp, long frameNumber) {
                    super.onCaptureStarted(session, request, timestamp, frameNumber);
                    // create image when it's in focus
                    try {
                        mImageFileTest = createImageFile(mImageFolder);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            };
            mPreviewCaptureSession.capture(mCaptureRequestBuilder.build(), stillCaptureCallback, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    /**
     * Check for permissions and start video recording.
     */
    public void startVideoRecording() {

        try {
            mVideoFileTest = createVideoFile(mVideoFolder);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // set up Recorder
        try {
            setupMediaRecorder();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // prepare for recording
        sendVideoRecordingRequest();

        // start recording
        mMediaRecorder.start();

        if (mRokidCameraRecordingListener != null) {
            mRokidCameraRecordingListener.onRokidCameraRecordingStarted();
        }
    }

    /**
     * Setup video recording
     *
     * @throws IOException : prepare Exception
     */
    private void setupMediaRecorder() throws IOException {
        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);
        mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mMediaRecorder.setOutputFile(mVideoFileName);
        mMediaRecorder.setVideoEncodingBitRate(10000000);
        mMediaRecorder.setVideoFrameRate(30);
        mMediaRecorder.setVideoSize(mSizeVideoRecorder.getSize().getWidth(), mSizeVideoRecorder.getSize().getHeight());
        mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
        mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        mMediaRecorder.setOrientationHint(mTotalRotation);
        mMediaRecorder.prepare();
    }

    /**
     * Set up Preview surface and Recording surface to prepare for recording
     */
    private void sendVideoRecordingRequest() {
        SurfaceTexture surfaceTexture = mTextureView.getSurfaceTexture();
        assert surfaceTexture != null;
        surfaceTexture.setDefaultBufferSize(mSizePreview.getSize().getWidth(), mSizePreview.getSize().getHeight());
        Surface previewSurface = new Surface(surfaceTexture);

        try {

            // create request for RECORDING template
            /**
             * Create a request suitable for video recording. Specifically, this means
             * that a stable frame rate is used, and post-processing is set for
             * recording quality. These requests would commonly be used with the
             * {@link CameraCaptureSession#setRepeatingRequest} method.
             * This template is guaranteed to be supported on all camera devices except
             * {@link CameraMetadata#REQUEST_AVAILABLE_CAPABILITIES_DEPTH_OUTPUT DEPTH_OUTPUT} devices
             * that are not {@link CameraMetadata#REQUEST_AVAILABLE_CAPABILITIES_BACKWARD_COMPATIBLE
             * BACKWARD_COMPATIBLE}.
             *
             * @see #createCaptureRequest
             */
            mCaptureRequestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_RECORD);
            configureCameraParameters(mCaptureRequestBuilder, mRokidCameraParamAEMode, mRokidCameraParamAFMode, mRokidCameraParamAWBMode);

            if (mPreviewEnabled) {
                // add Preview surface to target
                mCaptureRequestBuilder.addTarget(previewSurface);
            }

            // add Record surface to target
            Surface recordSurface = mMediaRecorder.getSurface();
            mCaptureRequestBuilder.addTarget(recordSurface);

            mCameraDevice.createCaptureSession(Arrays.asList(previewSurface, recordSurface), new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
                    try {
                        cameraCaptureSession.setRepeatingRequest(mCaptureRequestBuilder.build(), null, null);
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {

                }
            }, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Stop recording
     */
    public void stopRecording() {

        if (mRokidCameraRecordingListener != null) {
            mRokidCameraRecordingListener.onRokidCameraRocordingFinished();
        }

        try {
            mMediaRecorder.stop();
        } catch(RuntimeException e) {
            // TODO: delete file if recording failed to prevent 0KB file (error file)
//                mFile.delete();
        } finally {
            mMediaRecorder.reset();

//                mRecorder.release();
//                mRecorder = null;
        }

        final Intent scanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        final Uri contentUri = Uri.fromFile(mVideoFileTest.getAbsoluteFile());
        scanIntent.setData(contentUri);
        mActivity.sendBroadcast(scanIntent);

        MediaScannerConnection.scanFile(
                mActivity,
                new String[]{mVideoFolder.getAbsolutePath()},
                null,
                new MediaScannerConnection.OnScanCompletedListener() {
                    @Override
                    public void onScanCompleted(String path, Uri uri) {
                        Log.v("testtest",
                                "file " + path + " was scanned seccessfully: " + uri);
                    }
                });
    }

    /**
     * Stop background thread
     */
    private void stopBackgroundThread() {
        mBackgroundHandlerThread.quitSafely();
        try {
            mBackgroundHandlerThread.join();
            mBackgroundHandlerThread = null;
            mBackgroundHandler = null;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Close Camera resource
     */
    private void closeCamera() {
        if (mCameraDevice != null) {
            mCameraDevice.close();
            mCameraDevice = null;
        }
    }

    /**
     * Configures the necessary {@link android.graphics.Matrix} transformation to `mTextureView`.
     * This method should be called after the camera preview size is determined in
     * setUpCameraOutputs and also the size of `mTextureView` is fixed.
     *
     * @param viewWidth  The width of `mTextureView`
     * @param viewHeight The height of `mTextureView`
     */
    private void configureTransform(int viewWidth, int viewHeight) {
        if (null == mTextureView || null == mSizePreview.getSize()) {
            return;
        }
        int rotation = mActivity.getWindowManager().getDefaultDisplay().getRotation();
        Matrix matrix = new Matrix();
        RectF viewRect = new RectF(0, 0, viewWidth, viewHeight);
        RectF bufferRect = new RectF(0, 0, mSizePreview.getSize().getHeight(), mSizePreview.getSize().getWidth());
        float centerX = viewRect.centerX();
        float centerY = viewRect.centerY();
        if (Surface.ROTATION_90 == rotation || Surface.ROTATION_270 == rotation) {
            bufferRect.offset(centerX - bufferRect.centerX(), centerY - bufferRect.centerY());
            matrix.setRectToRect(viewRect, bufferRect, Matrix.ScaleToFit.FILL);
            float scale = Math.max(
                    (float) viewHeight / mSizePreview.getSize().getHeight(),
                    (float) viewWidth / mSizePreview.getSize().getWidth());
            matrix.postScale(scale, scale, centerX, centerY);
            matrix.postRotate(90 * (rotation - 2), centerX, centerY);
        } else if (Surface.ROTATION_180 == rotation) {
            matrix.postRotate(180, centerX, centerY);
        }
        mTextureView.setTransform(matrix);
    }

    private void configureCameraParameters(CaptureRequest.Builder captureRequestBuilder, RokidCameraParameters aeMode, RokidCameraParameters afMode, RokidCameraParameters awbMode) {
        captureRequestBuilder.set(CONTROL_AE_MODE, aeMode.getParam());
        captureRequestBuilder.set(CONTROL_AF_MODE, afMode.getParam());
        captureRequestBuilder.set(CONTROL_AWB_MODE, awbMode.getParam());
    }

    // TODO: put to a Util class
    public File createVideoFolder() {
//        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
//                Environment.DIRECTORY_MOVIES + File.separator), "RokidCameraVideo");
//        mVideoFolder = mediaStorageDir;

        File movieFile = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
        File mVideoFolder = new File(movieFile, "Camera");

        if (!mVideoFolder.exists()) {
            mVideoFolder.mkdirs();
        }

        return mVideoFolder;
    }

    public File createVideoFile(File mVideoFolder) throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyHHdd_HHmmss").format(new Date());
        String prepend = "ROKIDVIDEO_" + timeStamp;
        File videoFile = File.createTempFile(prepend, ".mp4", mVideoFolder);
        mVideoFileName = videoFile.getAbsolutePath();
        return videoFile;
    }

    public File createImageFolder() {
//        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
//                Environment.DIRECTORY_PICTURES + File.separator), "RokidCameraCamera");
//        mImageFolder = mediaStorageDir;

        File imageFile = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
        File mImageFolder = new File(imageFile, "Camera");

        if (!mImageFolder.exists()) {
            mImageFolder.mkdirs();
        }

        return mImageFolder;
    }

    public File createImageFile(File mImageFolder) throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyHHdd_HHmmss").format(new Date());
        String prepend = "ROKIDIMAGE_" + timeStamp;
//        File imageFile = File.createTempFile("ROKIDTEST", ".jpg", mImageFolder);
        File imageFile = new File(mImageFolder, prepend + ".jpg");
        mImageFileName = imageFile.getAbsolutePath();
        return imageFile;
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

    public boolean isPreviewEnabled() {
        return mPreviewEnabled;
    }

    public int getImageFormat() {
        return mImageFormat;
    }

    public int getMaxImages() {
        return mMaxImages;
    }

    public int getImageReaderCallbackMode() {
        return mImageReaderCallbackMode;
    }

    public RokidCameraSize getSizePreview() {
        return mSizePreview;
    }

    public RokidCameraSize getSizeVideoRecorder() {
        return mSizeVideoRecorder;
    }

    public RokidCameraSize getSizeImageReader() {
        return mSizeImageReader;
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
}
