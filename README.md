Rokid Camera

The camera app for Rokid Glass can take pictures and videos. Saves the file to `sdcard\DCIM\` folder. The user can swipe between picture and video mode by swiping on the touch bar.

## Basic App Features:

1. Take photo
- Click shutter button to take single photo
{F2188, layout=left, size=full, alt="a duckling"}

2. Record video 

- Click shutter button to start recording. It should be time duration at the shutter button when recording is in progress. Click again to stop recording. 
{F2189, layout=left, size=full, alt="a duckling"}
{F2190, layout=left, size=full, alt="a duckling"}

---
## Default File Path for Photo and Video

`sdcard\DCIM\`

---
## Android API:

Photo:
```
CameraDevice
```
{F291, layout=left, size=full, alt="a duckling"}

=== Note:
	
- CameraDevice.CaptureListener: 
	1. used for auto-focus in `Preview` 
	
		lang=java
        private CameraCaptureSession.CaptureCallback mPreviewCaptureCallback = new CameraCaptureSession.CaptureCallback()   
            private void process(CaptureResult captureResult) {
              switch (mCaptureState) {
                  case STATE_PREVIEW:
                      // do nothing
                      break;
                  case STATE_WAIT_LOCK:
                      mCaptureState = STATE_PREVIEW
                      Integer afState = captureResult.ge  CaptureResult.CONTROL_AF_STATE);
                      if (afState == CaptureResult.CONTROL_AF_STATE_FOCUSED_LOCKED ||
                              afState =  CaptureResult.CONTROL_AF_STATE_NOT_FOCUSED_LOCKED) {
                          Toast.makeText(getApplicationContext(), "AF Locked!"  Toast.LENGTH_SHORT).show();
                          startStillCaptureRequest();
                      }
                      break;
              }

            @Override
            public void onCaptureCompleted(@NonNull CameraCaptureSession session  @NonNull CaptureRequest request, @NonNull TotalCaptureResult result) {
              super.onCaptureCompleted(session, request, result)  
              process(result);
            }
        };
        
	2. and create image file in `still photo capture`
	
		lang=java
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
    
- CameraDevice.StateListener: used for opening camera and check if opening was successful or not.
		lang=java
        private CameraDevice.StateCallback mCameraDevicesStateCallback = new CameraDevice.StateCallback() {
            @Override
            public void onOpened(@NonNull CameraDevice cameraDevice) {
                mCameraDevice = cameraDevice;
                if (mIsRecording) {
                    try {
                        mVideoFileTest = createVidelFileName();
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

Video:
```
MediaRecorder
```
---
## Output Resolution:

Photo: (Format : JPEG)

```
4000 × 3000
```

Video: (Format : MP4)

```
2592 × 1944
```

---
## Basic App Initialization Workflow

1. Make sure the file destination folders are available or create new one if does not exists.
2. Init all UI, RecyclerView(used for button swiping), background thread, preview, and CameraDevice. 
3. Dynamic permissions check and request permissions if needed.
4. Steps for setting up CameraDevice: (`setupCamera()` method in `MainActivity`)

	- Get device rotation and camera sensor rotation.

		lang=java
        int deviceOrientation = getWindowManager().getDefaultDisplay().getRotation();
        mTotalRotation = sensorToDeviceRotation(cameraCharacteristics, deviceOrientation);
        private static int sensorToDeviceRotation(CameraCharacteristics cameraCharacteristics, int deviceOrientation) {
            int sensorOrientation = cameraCharacteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);
            deviceOrientation = ORIENTATIONS.get(deviceOrientation);
            return (sensorOrientation + deviceOrientation + 270) % 360;
        }
	- Get screen resolution.

		lang=java
        Point displaySize = new Point();
        getWindowManager().getDefaultDisplay().getSize(displaySize);

	- Get current hardware auto-focus support.

		lang=java
        int[] afAvailableModes = cameraCharacteristics.get(CameraCharacteristics.CONTROL_AF_AVAILABLE_MODES);

	- Set correct rotation value for CameraDevice.

	- Set optimal size for PreviewSize (Camera Preview for user to see what the camera sees).

	- Set optimal size for VideoSize (Video Recording resolution).

	- Set optimal size for ImageReaderSize (Still photo resolution).

		lang=java
        mPreviewSize = chooseOptimalSize(map.getOutputSizes(SurfaceTexture.class), rotatedPreviewWidth, rotatedPreviewHeight, maxPreviewWidth, maxPreviewHeight, largest);
        mVideoSize = chooseOptimalSize(map.getOutputSizes(SurfaceTexture.class), rotatedPreviewWidth, rotatedPreviewHeight, maxPreviewWidth, maxPreviewHeight, largest);
        Size mImageSize = chooseOptimalSize(map.getOutputSizes(SurfaceTexture.class), rotatedPreviewWidth, rotatedPreviewHeight, maxPreviewWidth, maxPreviewHeight, largest);
        mImageReader = ImageReader.newInstance(mImageSize.getWidth(), mImageSize.getHeight(), ImageFormat.JPEG, 10);
        mImageReader.setOnImageAvailableListener(mOnImageAvailableListener, mBackgroundHandler);


5. Steps for initialize CameraDevice (`initCamera()` method in `MainActivity`)
	- Will wait for TextureView to be ready before initialize CameraDevice.
	- If TextureView is ready, init now. Or will init camera in TextureView ready callback.
	- Get CameraManager from system service.
	- Use CameraManager to open camera with state callback passed on.
	- In state callback, the default is still photo because the default mode is photo. So as soon as Camera opens successfully, the preview will be shown. 

		lang=java
        if (mTextureView.isAvailable()) {
            setupCamera(mTextureView.getWidth(), mTextureView.getHeight());
            connectCamera();
        } else {
            mTextureView.setSurfaceTextureListener(mSurfaceTextureListener);
        }
---
## Start Preview

{F290, layout=left, size=full, alt="a duckling"}

*************

- Steps to start the preview:
	1. Add `Preview` target so that the preview will be shown
		lang=java
        mCaptureRequestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
        mCaptureRequestBuilder.addTarget(mImageReader.getSurface());

	2. Request a `RepeatingRequest` for starting preview 
		lang=java
        try {
            mPreviewCaptureSession.setRepeatingRequest(mCaptureRequestBuilder.build(), null, mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }

---
## Taking Still Photo Workflow

{F1092, layout=left, size=full, alt="a duckling"}

1. This action is triggered by clicking on the touch pad
2. Will try to auto-focus if auto-focus is available and request capture inside auto-focus callback. Or will request still photo capture right away if auto-focus is not available.

		lang=java
        private void handleStillPictureButton() {
            if (mAutoFocusSupported) {
                // try to auto focus
                lockFocus();
            } else {
                // capture right now if auto-focus not supported
                startStillCaptureRequest();
            }
        }

3. Use `CaptureRequestBuilder` to build the capture request. Builder setup includes setting request to `CameraDevice.TEMPLATE_STILL_CAPTURE`, adding `ImageReader` to target, setting same orientation as preview so that the captured image will be the correct orientation. (`startStillCaptureRequest()` method in `MainActivity`)

		lang=java
        mCaptureRequestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
        mCaptureRequestBuilder.addTarget(mImageReader.getSurface());
        // not sure why we need to add 180 rotation here
        // the original image was 180 degree off
        mCaptureRequestBuilder.set(CaptureRequest.JPEG_ORIENTATION, mTotalRotation);

4. Start the capture action.

		lang=java
        mPreviewCaptureSession.capture(mCaptureRequestBuilder.build(), stillCaptureCallback, null);

5. Create image file for each new image when capture request has been sent. `CameraCaptureSession.CaptureCallback` callback in `startStillCaptureRequest()` method)
		
		lang=java
        imageFileTest = createImageFileName();


6. Use `AcquireLatestImage()` to get the image when the image is available in the callback. (`ImageReader.OnImageAvailableListener` callback in `MainActivity`)

		lang=java
        private final ImageReader.OnImageAvailableListener mOnImageAvailableListener = new ImageReader.OnImageAvailableListener() {
            @Override
            public void onImageAvailable(ImageReader imageReader) {
                Image image = imageReader.acquireLatestImage();
                if (image != null) {
                    mBackgroundHandler.post(new ImageSaver(image));
                }
            }
        };

7. Use backgroudn thread to save image to file.

		lang=java
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
         ...
         ...

---
## Start and Stop Video Recording Workflow

1. This action is triggered by clicking on the touch pad.
2. Create video file and start recording.
3. Set up `MediaRecorder` in `setupMediaRecorder()` method and prepare the `MediaRecorder`.
4. Use `CaptureRequestBuilder` to build the capture request. Builder setup includes setting request to `CameraDevice.TEMPLATE_STILL_CAPTURE`, adding `Preview` and `recordSurface` to target.
5. Send video capture request to start recording.
6. Stop recording when user clicking on the touch pad again.
7. Stop `MediaRecorder` and reset it. update UI accordingly and send a global notification so that gallery app can view the most recent photo.

---
## Resource Release (When user exiting the app)

- Stop recording if currently in recording session.
- Close any camera resource for other app to use in the future.

---
# RokidCamera Module Introduction

- [Camera2 Maven Remote URL](https://jitpack.io/#yihanseattle/Camera2Module/master)

The RokidCamera module manage the Camera2 API cycle and automatically set up Camera class. Developers can use RokidCamera module to set Camera parameters, callbacks, format, etc. Example Camera setup using RokidCamera module:

## How to use Rokid Camera

### Use builder to create an instance of RokidCamera (recommend to create inside of onCreate() method)

		lang=java
        mRokidCamera = new RokidCameraBuilder(this, mTextureView)
                .setPreviewEnabled(true)
                .setImageFormat(ImageFormat.JPEG)

                // rename the method



                .setMaximumImages(5)
                .setRokidCameraRecordingListener(this)
                .setRokidCameraStateListener(this)
                .setRokidCameraOnImageAvailableListener(RokidCamera.STILL_PHOTO_MODE_SINGLE_NO_CALLBACK, this, this)
                .build();

### Sync RokidCamera with Activity lifecycle

Activity onStart() method:

		lang=java
        @Override
        protected void onStart() {
            super.onStart();
            mRokidCamera.onStart();
        }

Acitivity onStop() method:

		lang=java
        @Override
        protected void onStop() {
            super.onStop();
            if (mIsRecording) {
                mRokidCamera.stopRecording();
            }

            // test stop before start
            mRokidCamera.onStop();
        }

### Support Actions

Start Camera Preview:
		lang=java
        mRokidCamera.startPreview();

Start Recording:

        lang=java
        mRokidCamera.startVideoRecording();

Stop Recording:

		lang=java
        mRokidCamera.stopRecording();

Take photo:

		lang=java
        mRokidCamera.takeStillPicture();

## Current Available Functions:

### RokidCameraStateListener: 

- `RokidCameraStateListener` interface: 

		lang=java
        /**
         * Callbacks from CameraDevice inside RokidCamera class. The purpose is to let the user know
         * if camera states has changed.
         *
         * Currently only send callback when:
         *      - RokidCamera is opened
         */
        public interface RokidCameraStateListener {
            /**
             * Callback when RokidCamera opened successfully.
             */
            void onRokidCameraOpened();
        }

- Implementation example inside Activity class for listening events: 

		lang=java
        public class MainActivity implements RokidCameraStateListener {

        ...
        ...

            @Override
            public void onRokidCameraOpened() {
                // UI update or other actions
            }

### RokidCameraRocordingListener

- `RokidCameraRocordingListener` interface: 
		lang=java
        /**
         * Callback for Video Recording status.
         * One for recording started and one for Recording ended.
         */
        public interface RokidCameraVideoRecordingListener {
            /**
             * Callback when video recording started.
             */
            void onRokidCameraRecordingStarted();

            /**
             * Callback when video recording ended.
             */
            void onRokidCameraRocordingFinished();
        }

- Implementation example inside Activity class for listening events: 
		lang=java
        public class MainActivity implements RokidCameraVideoRecordingListener {

        ...
        ...

        @Override
        public void onRokidCameraRecordingStarted() {
            // UI update
            mChronometer.setBase(SystemClock.elapsedRealtime());
            mChronometer.setVisibility(View.VISIBLE);
            mChronometer.start();
            mIsRecording = true;
        }

        @Override
        public void onRokidCameraRocordingFinished() {
            // UI update
            mChronometer.stop();
            updateButtonText(mCameraMode);
            disableProgressTextView();

            mCameraMode = CameraMode.VIDEO_STOPPED;
            // app state and UI
            mIsRecording = false;
        }

### RokidCameraOnImageAvailableListener & RokidCameraIOListener

- `RokidCameraOnImageAvailableListener` interface: 
		lang=java
        /**
         * Image callback for Image from ImageReader. RokidCamera will send the image back to user Activity.
         */
        public interface RokidCameraOnImageAvailableListener {
            /**
             * Callback when Image available to send back to Activity.
             *
             * @param image : Image in a Camera Frame
             */
            void onRokidCameraImageAvailable(Image image);
        }

- `RokidCameraIOListener` interface:
		lang=java
        /**
         * Callback to user to let them know that the file saving has completed.
         */

        public interface RokidCameraIOListener {
            /**
             * Callback when File IO is completed.
             */
            void onRokidCameraFileSaved();
        }

- Implementation example inside Activity class for listening events: 
		lang=java
        public class MainActivity implements RokidCameraOnImageAvailableListener, RokidCameraIOListener {

        ...
        ...

        @Override
        public void onRokidCameraImageAvailable(Image image) {
            // handle Image object here
        }

        @Override
        public void onRokidCameraFileSaved() {
            // UI update or other actions
        }

### `setPreviewEnabled` method : Change visibility of Camera Preview.

- Implementation example inside Activity class for listening events: 
		lang=java
        public class MainActivity {

        ...
        ...

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            new RokidCameraBuilder(this, mTextureView)
                                .setPreviewEnabled(true)
            ..
            ..

### `setImageFormat` method : Change ImageFormat to user specified output format.

- Implementation example inside Activity class for listening events: 
		lang=java
        public class MainActivity {

        ...
        ...

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            new RokidCameraBuilder(this, mTextureView)
                                .setImageFormat(ImageFormat.JPEG)
            ..
            ..

### `setMaximumImages` method : Set MaxImageBuffer size for ImageReader.

- Implementation example inside Activity class for listening events: 
		lang=java
        public class MainActivity {

        ...
        ...

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            new RokidCameraBuilder(this, mTextureView)
                                .setMaximumImages(5)
            ..
            ..
