Rokid Camera

The camera app for Rokid Glass can take pictures and videos. Saves the file to `sdcard\DCIM\` folder. The user can swipe between picture and video mode by swiping on the touch bar.

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
		private CameraCaptureSession.CaptureCallback mPreviewCaptureCallback = ne  CameraCaptureSession.CaptureCallback()   
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

Photo:

```
2592 × 1944
```

Video:

```
2592 × 1944
```

---
## Basic App Initialization Workflow

{F288, layout=left, size=full, alt="a duckling"}

1. Make sure the file destination folders are available or create new one if does not exists.
2. Init all UI, RecyclerView(used for button swiping), background thread, preview, and CameraDevice. 
3. Dynamic permissions check and request permissions if needed.
4. Steps for setting up CameraDevice:
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
        int[] afAvailableModes = cameraCharacteristics.get(
        CameraCharacteristics.CONTROL_AF_AVAILABLE_MODES);
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


5. Steps for initialize CameraDevice
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

{F289, layout=left, size=full, alt="a duckling"}

1. This action is triggered by clicking on the touch pad
2. Will try to auto-focus if auto-focus is available and request capture inside auto-focus callback. Or will request still photo capture right away if auto-focus is not available.
3. Use `CaptureRequestBuilder` to build the capture request. Builder setup includes setting request to `CameraDevice.TEMPLATE_STILL_CAPTURE`, adding `ImageReader` to target, setting same orientation as preview so that the captured image will be the correct orientation.
4. Start the capture action.
5. Create image file for each new image when capture request has been sent.
6. Use `AcquireLatestImage()` to get the image when the image is available in the callback.
7. Use backgroudn thread to save image to file.

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



