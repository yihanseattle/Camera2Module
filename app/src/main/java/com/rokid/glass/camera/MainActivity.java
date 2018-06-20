package com.rokid.glass.camera;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.graphics.Typeface;
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
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.KeyEvent;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.widget.Chronometer;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.rokid.glass.camera.constant.Constants;
import com.rokid.glass.camera.enums.CameraMode;
import com.rokid.glass.camera.preview.AutoFitTextureView;
import com.rokid.glass.camera.recyclerviews.RecyclerViewAdapter;
import com.rokid.glass.camera.utils.Utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    public static final String TAG = "Camera2VideoImage";

    private static final int REQUEST_CAMERA_PERMISSION_RESULT = 0;
    private static final int REQUEST_WRITE_EXTERNAL_STORAGE_PERMISSION_RESULT = 1;
    private static final int STATE_PREVIEW = 0;
    private static final int STATE_WAIT_LOCK = 1;
    private int mCaptureState = STATE_PREVIEW;

    private boolean touchpadIsDisabled;
    private final int touchpadAnimationInterval = 300;

    private AutoFitTextureView mTextureView;
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

    private CameraDevice mCameraDevice;
    private CameraDevice.StateCallback mCameraDevicesStateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice cameraDevice) {
            mCameraDevice = cameraDevice;
            if (mIsRecording) {
                try {
                    createVidelFileName();
                    startRecord();
                    mMediaRecorder.start();
                    new Handler(getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            mChronometer.setBase(SystemClock.elapsedRealtime());
                            mChronometer.setVisibility(View.VISIBLE);
                            mChronometer.start();
                        }
                    });

                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                // Toast.makeText(getApplicationContext(), "Camera connection made!", Toast.LENGTH_SHORT).show();
                startPreview();
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

    private String mCameraId;
    private HandlerThread mBackgroundHandlerThread;
    private Handler mBackgroundHandler;

    private File mVideoFolder;
    private String mVideoFileName;
    private File mImageFolder;
    private String mImageFileName;

    private static SparseIntArray ORIENTATIONS = new SparseIntArray();

    static {
        ORIENTATIONS.append(Surface.ROTATION_0, 90);
        ORIENTATIONS.append(Surface.ROTATION_90, 0);
        ORIENTATIONS.append(Surface.ROTATION_180, 270);
        ORIENTATIONS.append(Surface.ROTATION_270, 180);
    }

    private Size mPreviewSize;
    private Size mVideoSize;
    private Size mImageSize;
    private ImageReader mImageReader;
    private final ImageReader.OnImageAvailableListener mOnImageAvailableListener = new ImageReader.OnImageAvailableListener() {
        @Override
        public void onImageAvailable(ImageReader imageReader) {
            Image image = imageReader.acquireLatestImage();
            if (image != null) {
                mBackgroundHandler.post(new ImageSaver(image));
            }
        }
    };
    private MediaRecorder mMediaRecorder;
    private boolean mAutoFocusSupported;
    private boolean permissionsAreGranted;

    private class ImageSaver implements Runnable {

        private final Image mImage;

        public ImageSaver(Image image) {
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

    private Chronometer mChronometer;
    private int mTotalRotation;
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
                        Toast.makeText(getApplicationContext(), "AF Locked!", Toast.LENGTH_SHORT).show();
                        startStillCaptureRequest();
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
    private CaptureRequest.Builder mCaptureRequestBuilder;

    private ImageButton mRecordImageButton;
    private ImageButton mStillImageButton;
    private boolean mIsRecording = false;

    // camera 1
    private CameraMode cameraMode;
    private ImageView ivCameraButton;
    private LinearLayout linearLayoutVideoProgress;
    private ImageView ivRecordingRedDot;
    private RecyclerView recyclerView;

//    public static final int MEDIA_TYPE_IMAGE = 1;
//    public static final int MEDIA_TYPE_VIDEO = 2;

    private ArrayList<String> mCameraModes;

    private static class CompareSizeByArea implements Comparator<Size> {

        @Override
        public int compare(Size lhs, Size rhs) {
            return Long.signum((long) lhs.getWidth() * lhs.getHeight() -
                    (long) rhs.getWidth() * rhs.getHeight());
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera2_video_image);

        createVideoFolder();
        createImageFolder();

        mMediaRecorder = new MediaRecorder();
        mChronometer = findViewById(R.id.chronometer);
        mChronometer.setOnChronometerTickListener(new Chronometer.OnChronometerTickListener() {
            @Override
            public void onChronometerTick(Chronometer chronometer) {
                long time = SystemClock.elapsedRealtime() - mChronometer.getBase();
                int h = (int) (time / 3600000);
                int m = (int) (time - h * 3600000) / 60000;
                int s = (int) (time - h * 3600000 - m * 60000) / 1000;
                String hh = h < 10 ? "0" + h : h + "";
                String mm = m < 10 ? "0" + m : m + "";
                String ss = s < 10 ? "0" + s : s + "";
                mChronometer.setText(hh + ":" + mm + ":" + ss);
            }
        });
        mTextureView = findViewById(R.id.textureView);
//        mRecordImageButton = findViewById(R.id.ibVideoOnline);
//        mRecordImageButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                if (mIsRecording) {
//                    mChronometer.stop();
//                    mChronometer.setVisibility(View.INVISIBLE);
//                    mIsRecording = false;
//                    mRecordImageButton.setImageResource(R.mipmap.btn_video_online);
//                    mMediaRecorder.stop();
//                    mMediaRecorder.reset();
//                    startPreview();
//                } else {
//                    checkWriteStoragePermission();
//                }
//            }
//        });
//        mStillImageButton = findViewById(R.id.ibCamera);
//        mStillImageButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                lockFocus();
//            }
//        });

        linearLayoutVideoProgress = findViewById(R.id.linearlayoutVideoProgress);
        ivRecordingRedDot = findViewById(R.id.ivVideoRecordingRedDot);
        Animation mAnimation = new AlphaAnimation(1, 0);
        mAnimation.setDuration(700);
        mAnimation.setInterpolator(new LinearInterpolator());
        mAnimation.setRepeatCount(Animation.INFINITE);
        mAnimation.setRepeatMode(Animation.REVERSE);
        ivRecordingRedDot.startAnimation(mAnimation);

        // Add a listener to the Capture button
        ivCameraButton = findViewById(R.id.ivCameraButton);
        ivCameraButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        performCameraButtonAction();
                    }
                }
        );
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (arePermissionsGranted()) {
            initApp();
        } else {
            requestAllPermissions();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        stopRecording();
        closeCamera();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        View decorView = getWindow().getDecorView();
        if (hasFocus) {
            decorView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        }
    }

    private void setupCamera(int width, int height) {
        CameraManager cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);


        try {
            for (String cameraId : cameraManager.getCameraIdList()) {
                // get camera characteristics
                CameraCharacteristics cameraCharacteristics = cameraManager.getCameraCharacteristics(cameraId);
                if (cameraCharacteristics.get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_FRONT) {
                    continue;
                }
                StreamConfigurationMap map = cameraCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
                int deviceOrientation = getWindowManager().getDefaultDisplay().getRotation();
                mTotalRotation = sensorToDeviceRotation(cameraCharacteristics, deviceOrientation);
                boolean swapRotation = mTotalRotation == 90 || mTotalRotation == 270;
//                int rotatedWidth = width;
//                int rotatedHeight = height;
//                if (swapRotation) {
//                     in portrait mode, flip the orientation
//                    rotatedWidth = height;
//                    rotatedHeight = width;
//                }

                Point displaySize = new Point();
                getWindowManager().getDefaultDisplay().getSize(displaySize);
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

                /**if (maxPreviewWidth > MAX_PREVIEW_WIDTH) {
                 maxPreviewWidth = MAX_PREVIEW_WIDTH;
                 } */
                if (maxPreviewWidth > Constants.PREVIEW_SIZE.getWidth()) {
                    maxPreviewWidth = Constants.PREVIEW_SIZE.getWidth();
                }

                if (maxPreviewHeight > Constants.PREVIEW_SIZE.getHeight()) {
                    maxPreviewHeight = Constants.PREVIEW_SIZE.getHeight();
                }

                    // Try to get the actual width and height of your phone.
                DisplayMetrics displayMetrics = new DisplayMetrics();
                getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
                int screenHeight = displayMetrics.heightPixels;
                int screenWidth = displayMetrics.widthPixels;
                Size largest = new Size(screenWidth, screenHeight);

                mPreviewSize = chooseOptimalSize(map.getOutputSizes(SurfaceTexture.class), rotatedPreviewWidth, rotatedPreviewHeight, maxPreviewWidth, maxPreviewHeight, largest);
                mVideoSize = chooseOptimalSize(map.getOutputSizes(SurfaceTexture.class), rotatedPreviewWidth, rotatedPreviewHeight, maxPreviewWidth, maxPreviewHeight, largest);
                mImageSize = chooseOptimalSize(map.getOutputSizes(SurfaceTexture.class), rotatedPreviewWidth, rotatedPreviewHeight, maxPreviewWidth, maxPreviewHeight, largest);
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

//        CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
//        try {
//            for (String cameraId: manager.getCameraIdList()) {
//                CameraCharacteristics characteristics
//                        = manager.getCameraCharacteristics(cameraId);
//
//                Integer facing = characteristics.get(CameraCharacteristics.LENS_FACING);
//                if (facing != null && facing == CameraCharacteristics.LENS_FACING_FRONT) {
//                    continue;
//                }
//
//                StreamConfigurationMap map = characteristics.get(
//                        CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
//                if (map == null)
//                    continue;
//
//                /*Size largest = Collections.max(
//                        Arrays.asList(map.getOutputSizes(ImageFormat.YUV_420_888)),
//                        new CompareSizesByArea());*/
//                // Try to get the actual width and height of your phone.
//                DisplayMetrics displayMetrics = new DisplayMetrics();
//                getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
//                int screenHeight = displayMetrics.heightPixels;
//                int screenWidth = displayMetrics.widthPixels;
//                Size largest = new Size(screenWidth, screenHeight);
//
//                /* Another way to get the full screen */
//                /*Point screenSize = new Point();
//                getWindowManager().getDefaultDisplay().getSize(screenSize);
//                Size largest = new Size(screenSize.x, screenSize.y);*/
//
//                // Hard code
//                //Size largest = new Size(640, 480);
//
//                /** Two observations:
//                 *  1. Using JPEG is better than YUV_420_888, as currently in the
//                 *  image to byte conversion, we finally always go from JPEG to byte. So it saves
//                 *  an additional step from YUV to JPEG.
//                 *  2. The pool size of the image reader needs to be large enough.
//                 *  3. However, using YUV_420_888 can guarantee 30 fps, while JPEG can only get
//                 *  roughly 10 fps. */
//                mImageReader = ImageReader.newInstance(Constants.IMAGE_SIZE.getWidth(),
//                        Constants.IMAGE_SIZE.getHeight(),
//                        ImageFormat.JPEG, 40);
//
//                /** A separate thread in hope to speed-up. */
//                mImageReader.setOnImageAvailableListener(mOnImageAvailableListener,
//                        mBackgroundHandler);
//
//                // Find out if we need to swap dimension to get the preview size relative to sensor
//                // coordinate.
//                int displayRotation = getWindowManager().getDefaultDisplay().getRotation();
//                //noinspection ConstantConditions
//                mTotalRotation = characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);
//                boolean swappedDimensions = false;
//                switch (displayRotation) {
//                    case Surface.ROTATION_0:
//                    case Surface.ROTATION_180:
//                        if (mTotalRotation == 90 || mTotalRotation == 270) {
//                            swappedDimensions = true;
//                        }
//                        break;
//                    case Surface.ROTATION_90:
//                    case Surface.ROTATION_270:
//                        if (mTotalRotation == 0 || mTotalRotation == 180) {
//                            swappedDimensions = true;
//                        }
//                        break;
//                    default:
//                        Log.e(TAG, "Display rotation is invalid: " + displayRotation);
//                }
//
//                Point displaySize = new Point();
//                getWindowManager().getDefaultDisplay().getSize(displaySize);
//                int rotatedPreviewWidth = width;
//                int rotatedPreviewHeight = height;
//                int maxPreviewWidth = displaySize.x;
//                int maxPreviewHeight = displaySize.y;
//
//                if (swappedDimensions) {
//                    rotatedPreviewWidth = height;
//                    rotatedPreviewHeight = width;
//                    maxPreviewWidth = displaySize.y;
//                    maxPreviewHeight = displaySize.x;
//                }
//
//                /**if (maxPreviewWidth > MAX_PREVIEW_WIDTH) {
//                 maxPreviewWidth = MAX_PREVIEW_WIDTH;
//                 } */
//                if (maxPreviewWidth > Constants.PREVIEW_SIZE.getWidth()) {
//                    maxPreviewWidth = Constants.PREVIEW_SIZE.getWidth();
//                }
//
//                if (maxPreviewHeight > Constants.PREVIEW_SIZE.getHeight()) {
//                    maxPreviewHeight = Constants.PREVIEW_SIZE.getHeight();
//                }
//
//                // Danger, W.R.! Attempting to use too large a preview size could  exceed the camera
//                // bus' bandwidth limitation, resulting in gorgeous previews but the storage of
//                // garbage capture data.
//                mPreviewSize = chooseOptimalSize(map.getOutputSizes(SurfaceTexture.class),
//                        rotatedPreviewWidth, rotatedPreviewHeight, maxPreviewWidth,
//                        maxPreviewHeight, largest);
//
//                mVideoSize = chooseOptimalSize(map.getOutputSizes(SurfaceTexture.class),
//                        rotatedPreviewWidth, rotatedPreviewHeight, maxPreviewWidth,
//                        maxPreviewHeight, largest);
//                mImageSize = chooseOptimalSize(map.getOutputSizes(SurfaceTexture.class),
//                        rotatedPreviewWidth, rotatedPreviewHeight, maxPreviewWidth,
//                        maxPreviewHeight, largest);
//
//                // Log.d(TAG, "setUpCameraOutput: Final preview size = " + mPreviewSize.getWidth() + " " + mPreviewSize.getHeight());
//
//                // We fit the aspect ratio of TextureView to the size of preview we picked.
//                int orientation = getResources().getConfiguration().orientation;
//                if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
//                    mTextureView.setAspectRatio(
//                            mPreviewSize.getWidth(), mPreviewSize.getHeight());
//                } else {
//                    mTextureView.setAspectRatio(
//                            mPreviewSize.getHeight(), mPreviewSize.getWidth());
//                }
//
//                // Log.d(TAG, "setUpCameraOutput: mTextureView = " + mTextureView.getWidth() + " " + mTextureView.getHeight());
//
//                // Check if auto focus is supported
//                int[] afAvailableModes = characteristics.get(
//                        CameraCharacteristics.CONTROL_AF_AVAILABLE_MODES);
//                if (afAvailableModes.length == 0 ||
//                        (afAvailableModes.length == 1
//                                && afAvailableModes[0] == CameraMetadata.CONTROL_AF_MODE_OFF)) {
////                    mAutoFocusSupported = false;
//                } else {
////                    mAutoFocusSupported = true;
//                }
//
//                mCameraId = cameraId;
//                return;
//
//            }
//        } catch (CameraAccessException e) {
//            e.printStackTrace();
//        } catch (NullPointerException e) {
//            Log.d(TAG, "The device does not support Camera2 API.");
//        }
    }

    private void connectCamera() {
        CameraManager cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                // device is Marshmallow or later
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) ==
                        PackageManager.PERMISSION_GRANTED) {
                    // connect the camera
                    cameraManager.openCamera(mCameraId, mCameraDevicesStateCallback, mBackgroundHandler);
                } else {
                    if (shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
                        Toast.makeText(this, "Video app required access to camera", Toast.LENGTH_SHORT).show();
                    }
                    requestPermissions(new String[]{Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CAMERA_PERMISSION_RESULT);
                }
            } else {
                cameraManager.openCamera(mCameraId, mCameraDevicesStateCallback, mBackgroundHandler);
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

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
                    Toast.makeText(getApplicationContext(), "Unable to setup camera preview", Toast.LENGTH_SHORT).show();
                }
            }, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void startStillCaptureRequest() {
        try {
            mCaptureRequestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
            mCaptureRequestBuilder.addTarget(mImageReader.getSurface());
            // not sure why we need to add 180 rotation here
            // the original image was 180 degree off
            mCaptureRequestBuilder.set(CaptureRequest.JPEG_ORIENTATION, mTotalRotation);

            CameraCaptureSession.CaptureCallback stillCaptureCallback = new CameraCaptureSession.CaptureCallback() {
                @Override
                public void onCaptureStarted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, long timestamp, long frameNumber) {
                    super.onCaptureStarted(session, request, timestamp, frameNumber);
                    // create image when it's in focus
                    try {
                        createImageFileName();
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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//        if (requestCode == REQUEST_CAMERA_PERMISSION_RESULT) {
//            if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
//                Toast.makeText(getApplicationContext(), "Application will not run without camera services", Toast.LENGTH_SHORT).show();
//            }
//            if (grantResults[1] != PackageManager.PERMISSION_GRANTED) {
//                Toast.makeText(getApplicationContext(), "Application will not run without audio", Toast.LENGTH_SHORT).show();
//            }
//        } else if (requestCode == REQUEST_WRITE_EXTERNAL_STORAGE_PERMISSION_RESULT) {
//            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                mIsRecording = true;
////                mRecordImageButton.setImageResource(R.mipmap.ic_launcher);
//                try {
//                    createVidelFileName();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//                Toast.makeText(getApplicationContext(), "Permission successfully granted! :)", Toast.LENGTH_SHORT).show();
//            } else {
//                Toast.makeText(getApplicationContext(), "App needs to save video to run", Toast.LENGTH_SHORT).show();
//            }
//        }

        if (arePermissionsGranted()) {
            initApp();
        } else {
            Toast.makeText(this, "Please grant all permission so the app will work properly.", Toast.LENGTH_SHORT).show();
        }
    }

    private void closeCamera() {
        if (mCameraDevice != null) {
            mCameraDevice.close();
            mCameraDevice = null;
        }
    }

    private void startBackgroundThread() {
        mBackgroundHandlerThread = new HandlerThread("Camera2VideoImage");
        mBackgroundHandlerThread.start();
        mBackgroundHandler = new Handler(mBackgroundHandlerThread.getLooper());
    }

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

    private static int sensorToDeviceRotation(CameraCharacteristics cameraCharacteristics, int deviceOrientation) {
        int sensorOrientation = cameraCharacteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);
        deviceOrientation = ORIENTATIONS.get(deviceOrientation);
        return (sensorOrientation + deviceOrientation + 270) % 360;
    }

//    /**
//     * Given {@code choices} of {@code Size}s supported by a camera, choose the smallest one that
//     * is at least as large as the respective texture view size, and that is at most as large as the
//     * respective max size, and whose aspect ratio matches with the specified value. If such size
//     * doesn't exist, choose the largest one that is at most as large as the respective max size,
//     * and whose aspect ratio matches with the specified value.
//     *
//     * @param choices           The list of sizes that the camera supports for the intended output
//     *                          class
//     * @param textureViewWidth  The width of the texture view relative to sensor coordinate
//     * @param textureViewHeight The height of the texture view relative to sensor coordinate
//     * @param maxWidth          The maximum width that can be chosen
//     * @param maxHeight         The maximum height that can be chosen
//     * @param aspectRatio       The aspect ratio
//     * @return The optimal {@code Size}, or an arbitrary one if none were big enough
//     */
//    private static Size chooseOptimalSize(Size[] choices, int textureViewWidth,
//                                          int textureViewHeight, int maxWidth, int maxHeight, Size aspectRatio) {
//
//        //Log.d(TAG, "chooseOptimalSize: tVW = " + textureViewWidth + " tVH = " + textureViewHeight);
//        //Log.d(TAG, "chooseOptimalSize: maxW = " + maxWidth + " maxH = " + maxHeight);
//        // Collect the supported resolutions that are at least as big as the preview Surface
//        List<Size> bigEnough = new ArrayList<>();
//        // Collect the supported resolutions that are smaller than the preview Surface
//        List<Size> notBigEnough = new ArrayList<>();
//        int w = aspectRatio.getWidth();
//        int h = aspectRatio.getHeight();
//        for (Size option : choices) {
//            //Log.d(TAG, "chooseOptimalSize: option = " + option.getWidth() + " " + option.getHeight());
//            if (option.getWidth() <= maxWidth && option.getHeight() <= maxHeight) {// &&
//                //    option.getHeight() == option.getWidth() * h / w) {
//                if (option.getHeight() != option.getWidth() * h / w) continue;
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
//            //Log.d(TAG, "chooseOptimalSize: Goes to bigEnough");
//            return Collections.min(bigEnough, new CompareSizeByArea());
//        } else if (notBigEnough.size() > 0) {
//            //Log.d(TAG, "chooseOptimalSize: Goes to notBigEnough");
//            return Collections.max(notBigEnough, new CompareSizeByArea());
//        } else {
//            //Log.d(TAG, "chooseOptimalSize: Not find any suitable!");
//            Log.e(TAG, "Couldn't find any suitable preview size");
//            return choices[0];
//        }
//    }

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
            return choices[choices.length - 1];
        }
    }

    private void createVideoFolder() {
        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DCIM + File.separator), "RokidCameraVideo");
        mVideoFolder = mediaStorageDir;

//        File movieFile = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES);
//        mVideoFolder = new File(movieFile, "camera2VideoImage");


        if (!mVideoFolder.exists()) {
            mVideoFolder.mkdirs();
        }
    }

    private File createVidelFileName() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyHHdd_HHmmss").format(new Date());
        String prepend = "VIDEO_" + "111" + "_";
        File videoFile = File.createTempFile(prepend, ".mp4", mVideoFolder);
        mVideoFileName = videoFile.getAbsolutePath();
        return videoFile;
    }

    private void createImageFolder() {
//        File imageFile = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
//        mImageFolder = new File(imageFile, "camera2VideoImage");


        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DCIM + File.separator), "RokidCameraCamera");
        mImageFolder = mediaStorageDir;
        if (!mImageFolder.exists()) {
            mImageFolder.mkdirs();
        }
    }

    private File createImageFileName() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyHHdd_HHmmss").format(new Date());
        String prepend = "IMAGE_" + timeStamp + "_";
        File imageFile = File.createTempFile(prepend, ".jpg", mImageFolder);
        mImageFileName = imageFile.getAbsolutePath();
        return imageFile;
    }

    private void checkWriteStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                mIsRecording = true;
                try {
                    createVidelFileName();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                startRecord();
                mMediaRecorder.start();
                mChronometer.setBase(SystemClock.elapsedRealtime());
                mChronometer.setVisibility(View.VISIBLE);
                mChronometer.start();
            } else {
                if (shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    Toast.makeText(this, "app needs to be able to save videos", Toast.LENGTH_SHORT).show();
                }
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_WRITE_EXTERNAL_STORAGE_PERMISSION_RESULT);
            }
        } else {
            mIsRecording = true;
            try {
                createVidelFileName();
            } catch (IOException e) {
                e.printStackTrace();
            }
            startRecord();
            mMediaRecorder.start();
            mChronometer.setBase(SystemClock.elapsedRealtime());
            mChronometer.setVisibility(View.VISIBLE);
            mChronometer.start();
        }
    }

    private void setupMediaRecorder() throws IOException {
        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);
        mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mMediaRecorder.setOutputFile(mVideoFileName);
        mMediaRecorder.setVideoEncodingBitRate(10000000);
        mMediaRecorder.setVideoFrameRate(30);
        mMediaRecorder.setVideoSize(mVideoSize.getWidth(), mVideoSize.getHeight());
        mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
        mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        mMediaRecorder.setOrientationHint(mTotalRotation);
        mMediaRecorder.prepare();
    }

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
        int rotation = getWindowManager().getDefaultDisplay().getRotation();
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

    // ----------------------
    // ----------------------
    // ----------------------
    // ----------------------
    // ----------------------
    // camera 1

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (!touchpadIsDisabled) {
            if (DeviceConfig.isInRokidGlass) {
                handleGlassAction(keyCode);
            } else {
                handleControllerAction(keyCode);
            }
        }
        return super.onKeyUp(keyCode, event);
    }

    private void handleControllerAction(int keyCode) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BUTTON_A:
                // ENTER
                Log.i("testtest", "KeyUp ->> " + keyCode + " -- ENTER \n");

                performCameraButtonAction();
                break;

            case 20:
                // RIGHT
                Log.i("testtest", "KeyUp ->> " + keyCode + " -- RIGHT \n");
                performSwipeToPhoto();
                break;

            case 19:
                // LEFT
                Log.i("testtest", "KeyUp ->> " + keyCode + " -- LEFT \n");
                performSwipeToVideo();
                break;

            default:
                Log.i("testtest", "KeyUp ->> " + keyCode + " -- Not Defined!! \n");
                break;
        }
    }

    private void handleGlassAction(int keyCode) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_ENTER:
                // ENTER
                Log.i("testtest", "KeyUp ->> " + keyCode + " -- ENTER \n");

                performCameraButtonAction();
                break;

            case KeyEvent.KEYCODE_DPAD_LEFT:
                // RIGHT
                if (DeviceConfig.useKeyboardInGlassForDebuggingWithoutTouchpad) {
                    Log.i("testtest", "KeyUp ->> " + keyCode + " -- RIGHT \n");
                    performSwipeToPhoto();
                }
                break;

            case KeyEvent.KEYCODE_DPAD_RIGHT:
                // LEFT
                if (DeviceConfig.useKeyboardInGlassForDebuggingWithoutTouchpad) {
                    // debugging using keyboard via Vysor
                    Log.i("testtest", "KeyUp ->> " + keyCode + " -- LEFT \n");
                    performSwipeToVideo();
                }
                break;

            case KeyEvent.KEYCODE_DPAD_UP:
                // RIGHT
                if (!DeviceConfig.useKeyboardInGlassForDebuggingWithoutTouchpad) {
                    Log.i("testtest", "KeyUp ->> " + keyCode + " -- RIGHT \n");
                    performSwipeToPhoto();
                }
                break;

            case KeyEvent.KEYCODE_DPAD_DOWN:
                // LEFT
                if (!DeviceConfig.useKeyboardInGlassForDebuggingWithoutTouchpad) {
                    Log.i("testtest", "KeyUp ->> " + keyCode + " -- LEFT \n");
                    performSwipeToVideo();
                }
                break;

            default:
                Log.i("testtest", "KeyUp ->> " + keyCode + " -- Not Defined!! \n");
                break;
        }
    }

    private void initRecyclerView() {
        Log.d(TAG, "initRecyclerView: init recyclerview");
        mCameraModes = new ArrayList<>();
        mCameraModes.add("               ");
        mCameraModes.add(getResources().getString(R.string.CAMERAMODE_PHOTO));
        mCameraModes.add(getResources().getString(R.string.CAMERAMODE_VIDEO));
        mCameraModes.add("               ");

        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(layoutManager);
        RecyclerViewAdapter adapter = new RecyclerViewAdapter(this, mCameraModes);
        recyclerView.setAdapter(adapter);

    }

    private void updateButtonText(final CameraMode cameraMode) {
        if (cameraMode == CameraMode.PHOTO_STOPPED) {
            ivCameraButton.setImageDrawable(getResources().getDrawable(R.drawable.shutterinactive));
        } else if (cameraMode == CameraMode.PHOTO_TAKING) {
            ivCameraButton.setImageDrawable(getResources().getDrawable(R.drawable.shutteractive));
            new Handler(getMainLooper()).postDelayed(new Runnable() {
                @Override
                public void run() {
                    ivCameraButton.setImageDrawable(getResources().getDrawable(R.drawable.shutterinactive));
                    touchpadIsDisabled = false;
                }
            }, touchpadAnimationInterval);
            this.cameraMode = CameraMode.PHOTO_STOPPED;
        } else if (cameraMode == CameraMode.VIDEO_STOPPED) {
            ivCameraButton.setImageDrawable(getResources().getDrawable(R.drawable.videoinactive));
        } else if (cameraMode == CameraMode.VIDEO_RECORDING) {
            ivCameraButton.setImageDrawable(getResources().getDrawable(R.drawable.videoactive));
        }
    }

    private void initCameraModeForPhoto() {
        View view = null;
        TextView textView = null;
        view = recyclerView.findViewHolderForAdapterPosition(1).itemView;
        textView = view.findViewById(R.id.tvCameraMode);
        textView.setTextSize(Constants.CAMERA_MODE_TEXT_SIZE_SELECTED);
        textView.setTextColor(Color.parseColor(Constants.CAMERA_MODE_TEXT_COLOR_SELECTED));
        textView.setPadding(
                Utils.getDPFromPx(this, Constants.CAMERA_MODE_TEXT_PADDING_LEFT),
                0,
                Utils.getDPFromPx(this, Constants.CAMERA_MODE_TEXT_PADDING_RIGHT),
                0);
        textView.setTypeface(null, Typeface.BOLD);
        view = recyclerView.findViewHolderForAdapterPosition(2).itemView;
        textView = view.findViewById(R.id.tvCameraMode);
        textView.setTextSize(Constants.CAMERA_MODE_TEXT_SIZE_DESELECTED);
        textView.setTextColor(Color.parseColor(Constants.CAMERA_MODE_TEXT_COLOR_DESELECTED));
        textView.setPadding(
                Utils.getDPFromPx(this, Constants.CAMERA_MODE_TEXT_PADDING_LEFT),
                Utils.getDPFromPx(this, Constants.CAMERA_MODE_TEXT_PADDING_TOP),
                Utils.getDPFromPx(this, Constants.CAMERA_MODE_TEXT_PADDING_RIGHT),
                0);
        textView.setTypeface(null, Typeface.NORMAL);
    }

    private void initCameraModeForVideo() {
        View view = null;
        TextView textView = null;
        view = recyclerView.findViewHolderForAdapterPosition(1).itemView;
        textView = view.findViewById(R.id.tvCameraMode);
        textView.setTextSize(Constants.CAMERA_MODE_TEXT_SIZE_DESELECTED);
        textView.setTextColor(Color.parseColor(Constants.CAMERA_MODE_TEXT_COLOR_DESELECTED));
        textView.setPadding(
                Utils.getDPFromPx(this, Constants.CAMERA_MODE_TEXT_PADDING_LEFT),
                Utils.getDPFromPx(this, Constants.CAMERA_MODE_TEXT_PADDING_TOP),
                Utils.getDPFromPx(this, Constants.CAMERA_MODE_TEXT_PADDING_RIGHT),
                0);
        textView.setTypeface(null, Typeface.NORMAL);
        view = recyclerView.findViewHolderForAdapterPosition(2).itemView;
        textView = view.findViewById(R.id.tvCameraMode);
        textView.setTextSize(Constants.CAMERA_MODE_TEXT_SIZE_SELECTED);
        textView.setTextColor(Color.parseColor(Constants.CAMERA_MODE_TEXT_COLOR_SELECTED));
        textView.setPadding(
                Utils.getDPFromPx(this, Constants.CAMERA_MODE_TEXT_PADDING_LEFT),
                0,
                Utils.getDPFromPx(this, Constants.CAMERA_MODE_TEXT_PADDING_RIGHT),
                0);
        textView.setTypeface(null, Typeface.BOLD);
    }

    private void disableProgressTextView() {
        recyclerView.setVisibility(View.VISIBLE);
        linearLayoutVideoProgress.setVisibility(View.GONE);
    }

    private void enableProgressTextView() {
        recyclerView.setVisibility(View.GONE);
        linearLayoutVideoProgress.setVisibility(View.VISIBLE);

    }

    private void initCameraButton() {
        cameraMode = CameraMode.PHOTO_STOPPED;
        updateButtonText(cameraMode);
    }

    private void initPreview() {
        // Create our Preview view and set it as the content of our activity.
        cameraMode = CameraMode.PHOTO_STOPPED;
//        mTextureView.setOnTouchListener(new OnSwipeTouchListener(this) {
//            View view = null;
//            TextView textView = null;
//
//            public void onSwipeTop() {
////                Toast.makeText(getApplicationContext(), "top", Toast.LENGTH_SHORT).show();
//            }
//
//            public void onSwipeRight() {
////                Toast.makeText(getApplicationContext(), "right", Toast.LENGTH_SHORT).show();
//
//                performSwipeToPhoto();
//            }
//
//            public void onSwipeLeft() {
////                Toast.makeText(getApplicationContext(), "left", Toast.LENGTH_SHORT).show();
//
//                performSwipeToVideo();
//
//            }
//
//            public void onSwipeBottom() {
////                Toast.makeText(getApplicationContext(), "bottom", Toast.LENGTH_SHORT).show();
//
//            }
//        });
    }

    private void performSwipeToVideo() {
        // only can swipe to video if not currently recording
        if (cameraMode != CameraMode.VIDEO_RECORDING) {
            cameraMode = CameraMode.VIDEO_STOPPED;
            updateButtonText(cameraMode);
            recyclerView.smoothScrollToPosition(3);
            initCameraModeForVideo();
        }
    }

    private void performSwipeToPhoto() {
        // only can swipe to photo if not currently recording
        if (cameraMode != CameraMode.VIDEO_RECORDING) {
            cameraMode = CameraMode.PHOTO_STOPPED;
            updateButtonText(cameraMode);
            recyclerView.smoothScrollToPosition(0);
            initCameraModeForPhoto();
            initPreview();
        }
    }

    private void performCameraButtonAction() {

        if (cameraMode == CameraMode.PHOTO_STOPPED) {
            cameraMode = CameraMode.PHOTO_TAKING;
            touchpadIsDisabled = true;
            // get an image from the camera
            handleStillPictureButton();
            updateButtonText(cameraMode);
        } else {
            handleVideoButton();
        }
    }

    private void handleVideoButton() {
        if (mIsRecording) {
            stopRecording();
            // restart preview
            startPreview();
        } else {
            mIsRecording = true;
            cameraMode = CameraMode.VIDEO_RECORDING;
            updateButtonText(cameraMode);
            enableProgressTextView();
            checkWriteStoragePermission();
        }
    }

    private void stopRecording() {
        mChronometer.stop();


        try {
            mMediaRecorder.stop();
        } catch(RuntimeException e) {
            //you must delete the outputfile when the recorder stop failed.
//                mFile.delete();
        } finally {
//                mRecorder.release();
//                mRecorder = null;

            mMediaRecorder.reset();
        }
        // app state and UI
        mIsRecording = false;
        cameraMode = CameraMode.VIDEO_STOPPED;
        updateButtonText(cameraMode);
        disableProgressTextView();
    }

    private void handleStillPictureButton() {
        if (mAutoFocusSupported) {
            lockFocus();
        } else {
            startStillCaptureRequest();
        }
    }

    private void initApp() {
        initRecyclerView();
        initCameraButton();
//        initTimerTask();
        // Create an instance of Camera
//        mCamera = getCameraInstance();
//        mCamera.setDisplayOrientation(180);
//        startContinuousAutoFocus();
        initPreview();
        initCamera();

        touchpadIsDisabled = false;
    }

    private void initCamera() {
        startBackgroundThread();

        if (mTextureView.isAvailable()) {
            setupCamera(mTextureView.getWidth(), mTextureView.getHeight());
            connectCamera();

        } else {
            mTextureView.setSurfaceTextureListener(mSurfaceTextureListener);
        }
    }

    private boolean arePermissionsGranted() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            permissionsAreGranted = true;
        } else {
            return false;
        }

        return true;
    }

    private void requestAllPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[] {
                    Manifest.permission.CAMERA,
                    Manifest.permission.RECORD_AUDIO,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE }, 8);
        }
    }
}
