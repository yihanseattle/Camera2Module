package com.rokid.glass.camera.cameramodule;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Point;
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
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.media.MediaRecorder;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.Toast;

import com.rokid.glass.camera.DeviceConfig;
import com.rokid.glass.camera.cameramodule.callbacks.RokidCameraIOListener;
import com.rokid.glass.camera.cameramodule.callbacks.RokidCameraStateListener;
import com.rokid.glass.camera.cameramodule.callbacks.RokidCameraVideoRecordingListener;
import com.rokid.glass.camera.constant.Constants;
import com.rokid.glass.camera.preview.AutoFitTextureView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;

/**
 * Camera Module that can be use for projects with needs for Camera features.
 *
 * This class can initialize, set up, and connect to CameraDevice. Set different parameters according to specific applications for Rokid Glass.
 *
 *
 * Created by yihan on 7/23/18.
 */

public class RokidCamera {

    private Activity mActivity;
    private RokidCameraStateListener mRokidCameraStateListener;
    private RokidCameraIOListener mRokidCameraIOListener;
    private RokidCameraVideoRecordingListener mRokidCameraRecordingListener;

    // TODO: default constructor?
    private RokidCamera() {
        // don't allow to use the default constructor
    }

    public RokidCamera(Activity activity, TextureView textureView) {
        this.mActivity = activity;
        this.mTextureView = textureView;
    }

    public RokidCamera(Activity activity, TextureView textureView, RokidCameraStateListener rokidCameraStateListener) {
        this.mActivity = activity;
        this.mTextureView = textureView;
        this.mRokidCameraStateListener = rokidCameraStateListener;
    }

    public RokidCamera(Activity mActivity, TextureView textureView, RokidCameraStateListener mRokidCameraStateListener, RokidCameraIOListener mRokidCameraIOListener) {
        this.mActivity = mActivity;
        this.mTextureView = textureView;
        this.mRokidCameraStateListener = mRokidCameraStateListener;
        this.mRokidCameraIOListener = mRokidCameraIOListener;
    }

    public RokidCamera(Activity mActivity, TextureView textureView, RokidCameraStateListener mRokidCameraStateListener, RokidCameraIOListener mRokidCameraIOListener, RokidCameraVideoRecordingListener mRokidCameraRecordingListener) {
        this.mActivity = mActivity;
        this.mTextureView = textureView;
        this.mRokidCameraStateListener = mRokidCameraStateListener;
        this.mRokidCameraIOListener = mRokidCameraIOListener;
        this.mRokidCameraRecordingListener = mRokidCameraRecordingListener;
    }

    public void setmRokidCameraStateListener(RokidCameraStateListener mRokidCameraStateListener) {
        this.mRokidCameraStateListener = mRokidCameraStateListener;
    }

    public void setmRokidCameraIOListener(RokidCameraIOListener mRokidCameraIOListener) {
        this.mRokidCameraIOListener = mRokidCameraIOListener;
    }

    public void setmRokidCameraRecordingListener(RokidCameraVideoRecordingListener mRokidCameraRecordingListener) {
        this.mRokidCameraRecordingListener = mRokidCameraRecordingListener;
    }

    // auto-focus lock
    private static final int STATE_PREVIEW = 0;
    private static final int STATE_WAIT_LOCK = 1;
    private int mCaptureState = STATE_PREVIEW;

    // preview texture
    private TextureView mTextureView;
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
//            if (mIsRecording) {
//                try {
//                    mVideoFileTest = createVidelFileName();
//                    startRecord();
//                    mMediaRecorder.start();
//
//                    new Handler(getMainLooper()).post(new Runnable() {
//                        @Override
//                        public void run() {
//                            mChronometer.setBase(SystemClock.elapsedRealtime());
//                            mChronometer.setVisibility(View.VISIBLE);
//                            mChronometer.start();
//                        }
//                    });
//
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            } else {
                // Toast.makeText(getApplicationContext(), "Camera connection made!", Toast.LENGTH_SHORT).show();
                startPreview();
//            }


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
                    if (afState == CaptureResult.CONTROL_AF_STATE_FOCUSED_LOCKED ||
                            afState == CaptureResult.CONTROL_AF_STATE_NOT_FOCUSED_LOCKED) {
                        if (mActivity != null) {
                            Toast.makeText(mActivity, "AF Locked!", Toast.LENGTH_SHORT).show();
                        }
                        startStillCaptureRequest();
                    }
                    break;

                // TODO: check for : case STATE_WAITING_PRECAPTURE: {

            }
        }

        @Override
        public void onCaptureCompleted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull TotalCaptureResult result) {
            super.onCaptureCompleted(session, request, result);

            process(result);
        }
    };
    private CaptureRequest.Builder mCaptureRequestBuilder;

    // camera parameter
    private String mCameraId;
    private int mTotalRotation;
    // background thread for camera API actions and saving image to SD card
    private HandlerThread mBackgroundHandlerThread;
    private Handler mBackgroundHandler;

    // destination folder
    private File mVideoFolder;
    private String mVideoFileName;
    private File mImageFolder;
    private String mImageFileName;

    // orientation calculate
    private static SparseIntArray ORIENTATIONS = new SparseIntArray();
    static {
        ORIENTATIONS.append(Surface.ROTATION_0, 90);
        ORIENTATIONS.append(Surface.ROTATION_90, 0);
        ORIENTATIONS.append(Surface.ROTATION_180, 270);
        ORIENTATIONS.append(Surface.ROTATION_270, 180);
    }

    // resolution size
    private Size mPreviewSize;
    private Size mVideoSize;
    private ImageReader mImageReader;
    private final ImageReader.OnImageAvailableListener mOnImageAvailableListener = new ImageReader.OnImageAvailableListener() {
        @Override
        public void onImageAvailable(ImageReader imageReader) {
            // use background thread to save the image
            Image image = imageReader.acquireLatestImage();
            if (image != null) {
                mBackgroundHandler.post(new ImageSaver(image));
            }
        }
    };

    // media recorder for video recorder
    private MediaRecorder mMediaRecorder;
    private boolean mAutoFocusSupported;
    private File imageFileTest;
    private File mVideoFileTest;

    private CameraMode mCameraMode;

    public void onStop() {
        if (mIsRecording) {
            stopRecording();
        }
        closeCamera();

        // TODO: look for background thread finish
        stopBackgroundThread();
    }

    // Different camera modes for button control
    public enum CameraMode {
        PHOTO_STOPPED,
        PHOTO_TAKING,
        VIDEO_STOPPED,
        VIDEO_RECORDING
    }

    /**
     * Background thread for saving images to SD card
     */
    private class ImageSaver implements Runnable {

        private final Image mImage;

        ImageSaver(Image image) {
            mImage = image;
        }

        @Override
        public void run() {

            FileOutputStream fileOutputStream = null;
            try {
                if (mImageFileName != null) {
                    ByteBuffer byteBuffer = mImage.getPlanes()[0].getBuffer();
                    byte[] bytes = new byte[byteBuffer.remaining()];
                    byteBuffer.get(bytes);

                    fileOutputStream = new FileOutputStream(mImageFileName);
                    fileOutputStream.write(bytes);
                    Log.i("testtest", "thread finished ");
                    mImageFileName = null;


                    // callback to user
                    if (mRokidCameraIOListener != null) {
                        mRokidCameraIOListener.onRokidCameraFileSaved();
                    }


                    // send global notification for new photo taken
                    // so that the gallery app can view new photo
                    final Intent scanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                    final Uri contentUri = Uri.fromFile(imageFileTest.getAbsoluteFile());
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

    private boolean mIsRecording = false;


    private static class CompareSizeByArea implements Comparator<Size> {

        @Override
        public int compare(Size lhs, Size rhs) {
            return Long.signum((long) lhs.getWidth() * lhs.getHeight() -
                    (long) rhs.getWidth() * rhs.getHeight());
        }
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


        try {
            for (String cameraId : cameraManager.getCameraIdList()) {
                // get camera characteristics
                CameraCharacteristics cameraCharacteristics = cameraManager.getCameraCharacteristics(cameraId);
                if (cameraCharacteristics.get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_FRONT) {
                    continue;
                }
                StreamConfigurationMap map = cameraCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
                int deviceOrientation = mActivity.getWindowManager().getDefaultDisplay().getRotation();
                mTotalRotation = sensorToDeviceRotation(cameraCharacteristics, deviceOrientation);
                boolean swapRotation = mTotalRotation == 90 || mTotalRotation == 270;

                Point displaySize = new Point();
                mActivity.getWindowManager().getDefaultDisplay().getSize(displaySize);
                int rotatedPreviewWidth = width;
                int rotatedPreviewHeight = height;
                int maxPreviewWidth = displaySize.x;
                int maxPreviewHeight = displaySize.y;

                if (swapRotation) {
                    rotatedPreviewWidth = height;
                    rotatedPreviewHeight = width;
                    maxPreviewWidth = displaySize.y;
                    maxPreviewHeight = displaySize.x;
                }

                if (maxPreviewWidth > Constants.PREVIEW_SIZE.getWidth()) {
                    maxPreviewWidth = Constants.PREVIEW_SIZE.getWidth();
                }

                if (maxPreviewHeight > Constants.PREVIEW_SIZE.getHeight()) {
                    maxPreviewHeight = Constants.PREVIEW_SIZE.getHeight();
                }

                // Try to get the actual width and height of your phone.
                DisplayMetrics displayMetrics = new DisplayMetrics();
                mActivity.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
                int screenHeight = displayMetrics.heightPixels;
                int screenWidth = displayMetrics.widthPixels;
                Size largest = new Size(screenWidth, screenHeight);

                mPreviewSize = chooseOptimalSize(map.getOutputSizes(SurfaceTexture.class), rotatedPreviewWidth, rotatedPreviewHeight, maxPreviewWidth, maxPreviewHeight, largest);
                mVideoSize = chooseOptimalSize(map.getOutputSizes(SurfaceTexture.class), rotatedPreviewWidth, rotatedPreviewHeight, maxPreviewWidth, maxPreviewHeight, largest);
                Size mImageSize = chooseOptimalSize(map.getOutputSizes(SurfaceTexture.class), rotatedPreviewWidth, rotatedPreviewHeight, maxPreviewWidth, maxPreviewHeight, largest);
                mImageReader = ImageReader.newInstance(mImageSize.getWidth(), mImageSize.getHeight(), ImageFormat.JPEG, 10);
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

    /**
     * Help to calculate Camera Parameters
     * @param cameraCharacteristics : current characteristics
     * @param deviceOrientation     : device(screen) orientation
     * @return : orientation for (sensor + device)
     */
    private static int sensorToDeviceRotation(CameraCharacteristics cameraCharacteristics, int deviceOrientation) {
        int sensorOrientation = cameraCharacteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);
        deviceOrientation = ORIENTATIONS.get(deviceOrientation);
        return (sensorOrientation + deviceOrientation + 270) % 360;
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

            // old code. Since we are on Rokid Glass, below is no need.
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//                // device is Marshmallow or later
//                if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) ==
//                        PackageManager.PERMISSION_GRANTED) {
//                    // connect the camera
//                    // TODO: add comments
//                    cameraManager.openCamera(mCameraId, mCameraDevicesStateCallback, mBackgroundHandler);
//                } else {
//                    if (shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
//                        Toast.makeText(this, "Video app required access to camera", Toast.LENGTH_SHORT).show();
//                    }
//                    requestPermissions(new String[]{Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CAMERA_PERMISSION_RESULT);
//                }
//            } else {
//                cameraManager.openCamera(mCameraId, mCameraDevicesStateCallback, mBackgroundHandler);
//            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    /**
     * Start video recording
     */
    private void startRecord() {
        try {
            setupMediaRecorder();
            SurfaceTexture surfaceTexture = mTextureView.getSurfaceTexture();
            assert surfaceTexture != null;
            surfaceTexture.setDefaultBufferSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());
            Surface previewSurface = new Surface(surfaceTexture);
            Surface recordSurface = mMediaRecorder.getSurface();
            mCaptureRequestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_RECORD);
            mCaptureRequestBuilder.addTarget(previewSurface);
//            mCaptureRequestBuilder.addTarget(mImageReader.getSurface());

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
     * Start preview
     */
    private void startPreview() {
        SurfaceTexture surfaceTexture = mTextureView.getSurfaceTexture();
        assert surfaceTexture != null;
        surfaceTexture.setDefaultBufferSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());
        Surface previewSurface = new Surface(surfaceTexture);

        try {
            mCaptureRequestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
//            mCaptureRequestBuilder.addTarget(previewSurface);
            mCaptureRequestBuilder.addTarget(mImageReader.getSurface());

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

    /**
     * Capture still photo
     */
    private void startStillCaptureRequest() {
        try {
            mCaptureRequestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
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
                        imageFileTest = createImageFileName();
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
     * Close Camera resource
     */
    private void closeCamera() {
        if (mCameraDevice != null) {
            mCameraDevice.close();
            mCameraDevice = null;
        }
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
     * Given {@code choices} of {@code Size}s supported by a camera, choose the smallest one that
     * is at least as large as the respective texture view size, and that is at most as large as the
     * respective max size, and whose aspect ratio matches with the specified value. If such size
     * doesn't exist, choose the largest one that is at most as large as the respective max size,
     * and whose aspect ratio matches with the specified value.
     *
     * @param choices           The list of sizes that the camera supports for the intended output
     *                          class
     * @param textureViewWidth  The width of the texture view relative to sensor coordinate
     * @param textureViewHeight The height of the texture view relative to sensor coordinate
     * @param maxWidth          The maximum width that can be chosen
     * @param maxHeight         The maximum height that can be chosen
     * @param aspectRatio       The aspect ratio
     * @return The optimal {@code Size}, or an arbitrary one if none were big enough
     */
    private static Size chooseOptimalSize(Size[] choices, int textureViewWidth,
                                          int textureViewHeight, int maxWidth, int maxHeight, Size aspectRatio) {

//        // Collect the supported resolutions that are at least as big as the preview Surface
//        List<Size> bigEnough = new ArrayList<>();
//        // Collect the supported resolutions that are smaller than the preview Surface
//        List<Size> notBigEnough = new ArrayList<>();
//
//        List<Double> ratio = new LinkedList<>();
//
//        int w = aspectRatio.getWidth();
//        int h = aspectRatio.getHeight();
//        for (Size option : choices) {
//            if (option.getWidth() > option.getHeight()) {
//                ratio.add((double)option.getWidth() / option.getHeight());
//            } else {
//                ratio.add((double)option.getHeight() / option.getWidth());
//            }
//            if (option.getWidth() <= maxWidth && option.getHeight() <= maxHeight &&
//                    option.getHeight() == option.getWidth() * h / w) {
//                if (option.getWidth() >= textureViewWidth &&
//                        option.getHeight() >= textureViewHeight) {
//                    bigEnough.add(option);
//                } else {
//                    notBigEnough.add(option);
//                }
//            }
//        }
//
//        // Pick the smallest of those big enough. If there is no one big enough, pick the
//        // largest of those not big enough.
//        if (bigEnough.size() > 0) {
//            return Collections.min(bigEnough, new CompareSizeByArea());
//        } else if (notBigEnough.size() > 0) {
//            return Collections.max(notBigEnough, new CompareSizeByArea());
//        } else {
//            Log.e(TAG, "Couldn't find any suitable preview size");
//            if (DeviceConfig.isInRokidGlass) {
//                return choices[5];
//            } else {
//                return choices[choices.length - 1];
//            }
//        }

        // TODO: temporary fix because the preview size in xml has been set to [1dp x 1dp]

        if (DeviceConfig.isInRokidGlass) {
            return choices[5];
        } else {
            return choices[2];
        }
    }

    // TODO: put to a Util class
    private void createVideoFolder() {
//        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
//                Environment.DIRECTORY_MOVIES + File.separator), "RokidCameraVideo");
//        mVideoFolder = mediaStorageDir;

        File movieFile = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
        mVideoFolder = new File(movieFile, "Camera");

        if (!mVideoFolder.exists()) {
            mVideoFolder.mkdirs();
        }
    }

    private File createVidelFileName() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyHHdd_HHmmss").format(new Date());
        String prepend = "ROKIDVIDEO_" + timeStamp;
        File videoFile = File.createTempFile(prepend, ".mp4", mVideoFolder);
        mVideoFileName = videoFile.getAbsolutePath();
        return videoFile;
    }

    private void createImageFolder() {
//        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
//                Environment.DIRECTORY_PICTURES + File.separator), "RokidCameraCamera");
//        mImageFolder = mediaStorageDir;

        File imageFile = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
        mImageFolder = new File(imageFile, "Camera");

        if (!mImageFolder.exists()) {
            mImageFolder.mkdirs();
        }
    }

    private File createImageFileName() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyHHdd_HHmmss").format(new Date());
        String prepend = "ROKIDIMAGE_" + timeStamp;
//        File imageFile = File.createTempFile("ROKIDTEST", ".jpg", mImageFolder);
        File imageFile = new File(mImageFolder, prepend + ".jpg");
        mImageFileName = imageFile.getAbsolutePath();
        return imageFile;
    }

    /**
     * Check for permissions and start video recording.
     */
    private void checkWriteStoragePermission() {
        mIsRecording = true;
        try {
            mVideoFileTest = createVidelFileName();
        } catch (IOException e) {
            e.printStackTrace();
        }

        startRecord();
        mMediaRecorder.start();

        if (mRokidCameraRecordingListener != null) {
            mRokidCameraRecordingListener.onRokidCameraRecordingStarted();
        }

        mChronometer.setBase(SystemClock.elapsedRealtime());
        mChronometer.setVisibility(View.VISIBLE);
        mChronometer.start();
    }

    /**
     * Stop recording
     */
    private void stopRecording() {

        if (mRokidCameraRecordingListener != null) {
            mRokidCameraRecordingListener.onRokidCameraRocordingFinished();
        }

        mChronometer.stop();

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
        // app state and UI
        mIsRecording = false;
        mCameraMode = CameraMode.VIDEO_STOPPED;
        updateButtonText(mCameraMode);
        disableProgressTextView();



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
        mMediaRecorder.setVideoFrameRate(60);
        mMediaRecorder.setVideoSize(mVideoSize.getWidth(), mVideoSize.getHeight());
        mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
        mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        mMediaRecorder.setOrientationHint(mTotalRotation);
        mMediaRecorder.prepare();
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
     * Configures the necessary {@link android.graphics.Matrix} transformation to `mTextureView`.
     * This method should be called after the camera preview size is determined in
     * setUpCameraOutputs and also the size of `mTextureView` is fixed.
     *
     * @param viewWidth  The width of `mTextureView`
     * @param viewHeight The height of `mTextureView`
     */
    private void configureTransform(int viewWidth, int viewHeight) {
        if (null == mTextureView || null == mPreviewSize) {
            return;
        }
        int rotation = mActivity.getWindowManager().getDefaultDisplay().getRotation();
        Matrix matrix = new Matrix();
        RectF viewRect = new RectF(0, 0, viewWidth, viewHeight);
        RectF bufferRect = new RectF(0, 0, mPreviewSize.getHeight(), mPreviewSize.getWidth());
        float centerX = viewRect.centerX();
        float centerY = viewRect.centerY();
        if (Surface.ROTATION_90 == rotation || Surface.ROTATION_270 == rotation) {
            bufferRect.offset(centerX - bufferRect.centerX(), centerY - bufferRect.centerY());
            matrix.setRectToRect(viewRect, bufferRect, Matrix.ScaleToFit.FILL);
            float scale = Math.max(
                    (float) viewHeight / mPreviewSize.getHeight(),
                    (float) viewWidth / mPreviewSize.getWidth());
            matrix.postScale(scale, scale, centerX, centerY);
            matrix.postRotate(90 * (rotation - 2), centerX, centerY);
        } else if (Surface.ROTATION_180 == rotation) {
            matrix.postRotate(180, centerX, centerY);
        }
        mTextureView.setTransform(matrix);
    }

    private void initCamera() {

        createVideoFolder();
        createImageFolder();
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


}
