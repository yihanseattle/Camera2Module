# Rokid Camera App and Rokid Camera Module

The camera app for Rokid Glass can take pictures and videos. Recently, the app has integrated with Rokid Camera Module. The Camera app use much less code to use camera components with Camera Module. The Camera Module will increase the productivity of Rokid Glass app developers that uses camera components. For example, the camera app without Camera Module has 2500 lines of code in MainActivity class. However, now the MainActivity only has 800 lines of code using Camera Module. The Camera Module will also prevent any illegal configurations when setting up Camera instance. Also the Camera Module should support all apps that will be installed on Rokid Glass. This doc will introduce the followings:

    - Features of this app
    - The file path that photos and videos will be stored
    - Default resolutions (Can be configured to different resolution in Camera Module)
    - Integrated with Camera Module: Android Camera API overview
    - Integrated with Camera Module: App Initialization Workflow
    - Integrated with Camera Module: Start Preview
    - Integrated with Camera Module: Taking still photo
    - Integrated with Camera Module: Start and stop video recording
    - Integrated with Camera Module: Resource Release
    - Camera Module Introduction

To use this project, simply git clone and run under Android Studio 3. 

## Goal 
The goal of this app is to provide Camera app with glass-friendly UI so that the users can have the best user experience when using Rokid Glass. User should be able to successfully take still photos and record video. The project only contain the most basic features and doesn't provide manual camera configurations (Apature, ISO, shutter speed, etc.).

The goal of Camera Module is to create a general camera interface for apps run on Rokid Glass. Camera is one of the most commonly used components on Rokid Glass. The Camera Module can provide necessary features like still photo taking, video recording, or camera Byte data reading for algorithm use. After using this module, app developers should have more confidents when writing a camera featured app. Apps use Camera Module should be more stable as well since the Camera Module is throughly tested. 


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
## All Supported Configurations:

Below are all supported configurations from Camera Module. App developer can choose to configure based on their use case (Refer to API Doc for more detailed information)

### Camera Configurations:
- Enable or disable camera preview
- Set image format
- Set maximum image buffer size
- Set preview size
- Set ImageReader size
- Set Video Recorder size
- Set AE Mode
- Set AF Mode
- Set AWB Mode
- Set Camera ID

### Callback Listeners:
- Set camera state listener
- Set video recorder state listener
- Set ImageReader callback listener and ImageReader mode

### [Camera2 API Description](http://gitus.rokid-inc.com/diffusion/SDKOOOCAMERAMODULE/browse/master/Camera2APIInfo.md)

---
# RokidCamera Module Introduction

- [Camera2 Maven Remote URL](https://jitpack.io/#yihanseattle/Camera2Module/0.2.0)

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

## Additional Infomation (Branch Description):

yi_zhiyu_special_edition:
1. 开preview
2. 在2000 ms之后自动capture photo
3. 图片分辨率是4000 x 3000
4. 图片保存目录是："/storage/self/primary/DCIM/Camera"，文件名是："ROKIDIMAGE_test.jpg"。完整路径是：”/storage/self/primary/DCIM/Camera/ROKIDIMAGE_test.jpg"。如果DCIM下面没有Camera这个文件夹，app会创建一个。但是这个文件夹应该有的吧，这是默认camera app保存的目录。
5. 在File I/O保存结束后，自动close app


