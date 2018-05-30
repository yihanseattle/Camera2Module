package com.rokid.camera.camera2videoimage;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
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
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.Chronometer;
import android.widget.ImageButton;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

public class Camera2VideoImageActivity extends AppCompatActivity {

    private static final int REQUEST_CAMERA_PERMISSION_RESULT = 0;
    private static final int REQUEST_WRITE_EXTERNAL_STORAGE_PERMISSION_RESULT = 1;
    private static final int STATE_PREVIEW = 0;
    private static final int STATE_WAIT_LOCK = 1;
    private int mCaptureState = STATE_PREVIEW;

    private TextureView mTextureView;
    private TextureView.SurfaceTextureListener mSurfaceTextureListener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int width, int height) {
            setupCamera(width, height);
            connectCamera();
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int width, int height) {

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
        ORIENTATIONS.append(Surface.ROTATION_0, 0);
        ORIENTATIONS.append(Surface.ROTATION_90, 90);
        ORIENTATIONS.append(Surface.ROTATION_180, 180);
        ORIENTATIONS.append(Surface.ROTATION_270, 270);
    }

    private Size mPreviewSize;
    private Size mVideoSize;
    private Size mImageSize;
    private ImageReader mImageReader;
    private final ImageReader.OnImageAvailableListener mOnImageAvailableListener = new ImageReader.OnImageAvailableListener() {
        @Override
        public void onImageAvailable(ImageReader imageReader) {
            mBackgroundHandler.post(new ImageSaver(imageReader.acquireLatestImage()));
        }
    };
    private MediaRecorder mMediaRecorder;
    private class ImageSaver implements Runnable {

        private final Image mImage;

        public ImageSaver(Image image) {
            mImage = image;
        }

        @Override
        public void run() {
            ByteBuffer byteBuffer = mImage.getPlanes()[0].getBuffer();
            byte[] bytes = new byte[byteBuffer.remaining()];
            byteBuffer.get(bytes);

            FileOutputStream fileOutputStream = null;
            try {
                fileOutputStream = new FileOutputStream(mImageFileName);
                fileOutputStream.write(bytes);
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

    private static class CompareSizeByArea implements Comparator<Size> {

        @Override
        public int compare(Size lhs, Size rhs) {
            return Long.signum((long) lhs.getWidth() * lhs.getHeight() /
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
        mChronometer = findViewById(R.id.chronometer2);
        mChronometer.setOnChronometerTickListener(new Chronometer.OnChronometerTickListener() {
            @Override
            public void onChronometerTick(Chronometer chronometer) {
                long time = SystemClock.elapsedRealtime() - mChronometer.getBase();
                int h   = (int)(time /3600000);
                int m = (int)(time - h*3600000)/60000;
                int s= (int)(time - h*3600000- m*60000)/1000 ;
                String hh = h < 10 ? "0"+h: h+"";
                String mm = m < 10 ? "0"+m: m+"";
                String ss = s < 10 ? "0"+s: s+"";
                mChronometer.setText(hh+":"+mm+":"+ss);
            }
        });
        mTextureView = (TextureView) findViewById(R.id.textureView);
        mRecordImageButton = findViewById(R.id.ibVideoOnline);
        mRecordImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mIsRecording) {
                    mChronometer.stop();
                    mChronometer.setVisibility(View.INVISIBLE);
                    mIsRecording = false;
                    mRecordImageButton.setImageResource(R.mipmap.btn_video_online);
                    mMediaRecorder.stop();
                    mMediaRecorder.reset();
                    startPreview();
                } else {
                    checkWriteStoragePermission();
                }
            }
        });
        mStillImageButton = findViewById(R.id.ibCamera);
        mStillImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                lockFocus();
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();

        startBackgroundThread();

        if (mTextureView.isAvailable()) {
            setupCamera(mTextureView.getWidth(), mTextureView.getHeight());
            connectCamera();
        } else {
            mTextureView.setSurfaceTextureListener(mSurfaceTextureListener);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
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
                int rotatedWidth = width;
                int rotatedHeight = height;
                if (swapRotation) {
                    // in portrait mode, flip the orientation
                    rotatedWidth = height;
                    rotatedHeight = width;
                }
                mPreviewSize = chooseOptimalSize(map.getOutputSizes(SurfaceTexture.class), rotatedWidth, rotatedHeight);
                mVideoSize = chooseOptimalSize(map.getOutputSizes(MediaRecorder.class), rotatedWidth, rotatedHeight);
                mImageSize = chooseOptimalSize(map.getOutputSizes(ImageFormat.JPEG), rotatedWidth, rotatedHeight);
                mImageReader = ImageReader.newInstance(mImageSize.getWidth(), mImageSize.getHeight(), ImageFormat.JPEG, 1);
                mImageReader.setOnImageAvailableListener(mOnImageAvailableListener, mBackgroundHandler);
                mCameraId = cameraId;
                return;
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
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
                    requestPermissions(new String[]{Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO}, REQUEST_CAMERA_PERMISSION_RESULT);
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
            surfaceTexture.setDefaultBufferSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());
            Surface previewSurface = new Surface(surfaceTexture);
            Surface recordSurface = mMediaRecorder.getSurface();
            mCaptureRequestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_RECORD);
            mCaptureRequestBuilder.addTarget(previewSurface);
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
        surfaceTexture.setDefaultBufferSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());
        Surface previewSurface = new Surface(surfaceTexture);

        try {
            mCaptureRequestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            mCaptureRequestBuilder.addTarget(previewSurface);

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
        if (requestCode == REQUEST_CAMERA_PERMISSION_RESULT) {
            if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(getApplicationContext(), "Application will not run without camera services", Toast.LENGTH_SHORT).show();
            }
            if (grantResults[1] != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(getApplicationContext(), "Application will not run without audio", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == REQUEST_WRITE_EXTERNAL_STORAGE_PERMISSION_RESULT) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                mIsRecording = true;
                mRecordImageButton.setImageResource(R.mipmap.ic_launcher);
                try {
                    createVidelFileName();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Toast.makeText(getApplicationContext(), "Permission successfully granted! :)", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getApplicationContext(), "App needs to save video to run", Toast.LENGTH_SHORT).show();
            }
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
        return (sensorOrientation + deviceOrientation + 360) % 360;
    }

    private static Size chooseOptimalSize(Size[] choices, int width, int height) {
        List<Size> bigEnough = new ArrayList<>();
        for (Size option : choices) {
            if (option.getHeight() == option.getWidth() * height / width &&
                    option.getWidth() >= width && option.getHeight() >= height) {
                bigEnough.add(option);
            }
        }

        if (bigEnough.size() > 0) {
            return Collections.min(bigEnough, new CompareSizeByArea());
        } else {
            return choices[2];
        }
    }

    private void createVideoFolder() {
        File movieFile = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES);
        mVideoFolder = new File(movieFile, "camera2VideoImage");
        if (!mVideoFolder.exists()) {
            mVideoFolder.mkdirs();
        }
    }

    private File createVidelFileName() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyHHdd_HHmmss").format(new Date());
        String prepend = "VIDEO_" + timeStamp + "_";
        File videoFile = File.createTempFile(prepend, ".mp4", mVideoFolder);
        mVideoFileName = videoFile.getAbsolutePath();
        return videoFile;
    }

    private void createImageFolder() {
        File imageFile = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        mImageFolder = new File(imageFile, "camera2VideoImage");
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
                mRecordImageButton.setImageResource(R.mipmap.ic_launcher);
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
            mRecordImageButton.setImageResource(R.mipmap.ic_launcher);
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
        mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);
        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mMediaRecorder.setOutputFile(mVideoFileName);
        mMediaRecorder.setVideoEncodingBitRate(1000000);
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
}
