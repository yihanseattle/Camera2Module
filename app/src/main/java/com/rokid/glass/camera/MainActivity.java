package com.rokid.glass.camera;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.graphics.Typeface;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.Image;
import android.media.SoundPool;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.TextureView;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.widget.Chronometer;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.rokid.glass.camera.constant.Constants;
import com.rokid.glass.camera.recyclerviews.RecyclerViewAdapter;
import com.rokid.glass.camera.utils.Utils;
import com.rokid.glass.rokidcamera.RokidCamera;
import com.rokid.glass.rokidcamera.RokidCameraBuilder;
import com.rokid.glass.rokidcamera.callbacks.RokidCameraIOListener;
import com.rokid.glass.rokidcamera.callbacks.RokidCameraOnImageAvailableListener;
import com.rokid.glass.rokidcamera.callbacks.RokidCameraStateListener;
import com.rokid.glass.rokidcamera.callbacks.RokidCameraVideoRecordingListener;
import com.rokid.glass.rokidcamera.utils.RokidCameraParameters;
import com.rokid.glass.rokidcamera.utils.RokidCameraSize;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements
        RokidCameraIOListener,
        RokidCameraStateListener,
        RokidCameraVideoRecordingListener,
        RokidCameraOnImageAvailableListener {

    public static final String TAG = "Camera2VideoImage";

    // sound related
    private SoundPool soundPool;
    private AudioManager audioManager;
    // Maximumn sound stream.
    private static final int MAX_STREAMS = 5;
    // Stream type.
    private static final int streamType = AudioManager.STREAM_MUSIC;
    private boolean loaded;
    private int soundIdPhoto;
    private int soundIdVideoStart;
    private int soundIdVideoStop;
    private float volume;


    // disable button press(touch pad event) during animation
    private boolean touchpadIsDisabled;
    // animation
    private final int touchpadAnimationInterval = 300;

    // video recording timer
    private Chronometer mChronometer;

    // camera 1
    private ImageView mIVCameraButton;
    private LinearLayout mLinearLayoutVideoProgress;
    private ImageView mIVRecordingRedDot;
    private RecyclerView mRecyclerView;
    private TextureView mTextureView;

    // new RokidCamera SDK
    RokidCamera mRokidCamera;
    private boolean mIsRecording = false;
    private CameraMode mCameraMode;

    // Different camera modes for button control
    public enum CameraMode {
        PHOTO_STOPPED,
        PHOTO_TAKING,
        VIDEO_STOPPED,
        VIDEO_RECORDING
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera2_video_image);

        initLayoutAndUI();
        initSound();

        mRokidCamera = new RokidCameraBuilder(this, mTextureView)
                .setPreviewEnabled(false)
                .setImageFormat(ImageFormat.JPEG)
                .setMaximumImages(5)
                .setRokidCameraRecordingListener(this)
                .setRokidCameraStateListener(this)
                .setRokidCameraOnImageAvailableListener(RokidCamera.STILL_PHOTO_MODE_SINGLE_NO_CALLBACK, this, this)
                .setSizePreview(RokidCameraSize.SIZE_PREVIEW)
                .setSizeImageReader(RokidCameraSize.SIZE_IMAGE_READER_STILL_PHOTO)
                .setSizeVideoRecorder(RokidCameraSize.SIZE_VIDEO_RECORDING)
                .setRokidCameraParamAEMode(RokidCameraParameters.ROKID_CAMERA_PARAM_AE_MODE_ON)
                .setRokidCameraParamAFMode(RokidCameraParameters.ROKID_CAMERA_PARAM_AF_MODE_PICTURE)
                .setRokidCameraParamAWBMode(RokidCameraParameters.ROKID_CAMERA_PARAM_AWB_MODE_AUTO)
                .setRokidCameraParamCameraID(RokidCameraParameters.ROKID_CAMERA_PARAM_CAMERA_ID_ROKID_GLASS)
                .build();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }
    }

    private void initSound() {
        // AudioManager audio settings for adjusting the volume
        audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);

        // Current volumn Index of particular stream type.
        float currentVolumeIndex = (float) audioManager.getStreamVolume(streamType);

        // Get the maximum volume index for a particular stream type.
        float maxVolumeIndex  = (float) audioManager.getStreamMaxVolume(streamType);

        // Volumn (0 --> 1)
        this.volume = currentVolumeIndex / maxVolumeIndex;

        // Suggests an audio stream whose volume should be changed by
        // the hardware volume controls.
        this.setVolumeControlStream(streamType);

        // For Android SDK >= 21
        if (Build.VERSION.SDK_INT >= 21 ) {

            AudioAttributes audioAttrib = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build();

            SoundPool.Builder builder= new SoundPool.Builder();
            builder.setAudioAttributes(audioAttrib).setMaxStreams(MAX_STREAMS);

            this.soundPool = builder.build();
        }

        // When Sound Pool load complete.
        this.soundPool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
            @Override
            public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
                loaded = true;
            }
        });

        // Load sound file (photo.wav) into SoundPool.
        this.soundIdPhoto = this.soundPool.load(this, R.raw.photo,1);
        // Load sound file (video_start.wav) into SoundPool.
        this.soundIdVideoStart = this.soundPool.load(this, R.raw.video_start,1);
        // Load sound file (video_stop.wav) into SoundPool.
        this.soundIdVideoStop = this.soundPool.load(this, R.raw.video_stop,1);
    }

    private void initLayoutAndUI() {
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
        mLinearLayoutVideoProgress = findViewById(R.id.linearlayoutVideoProgress);
        mIVRecordingRedDot = findViewById(R.id.ivVideoRecordingRedDot);
        Animation mAnimation = new AlphaAnimation(1, 0);
        mAnimation.setDuration(700);
        mAnimation.setInterpolator(new LinearInterpolator());
        mAnimation.setRepeatCount(Animation.INFINITE);
        mAnimation.setRepeatMode(Animation.REVERSE);
        mIVRecordingRedDot.startAnimation(mAnimation);

        // Add a listener to the Capture button
        mIVCameraButton = findViewById(R.id.ivCameraButton);
        mIVCameraButton.setOnClickListener(
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
    protected void onStop() {
        super.onStop();
        if (mIsRecording) {
            mRokidCamera.stopRecording();
        }
        mRokidCamera.onStop();
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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (arePermissionsGranted()) {
            initApp();
        } else {
            Toast.makeText(this, "Please grant all permission so the app will work properly.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (!touchpadIsDisabled) {
            handleGlassAction(keyCode);
        }
        return super.onKeyUp(keyCode, event);
    }

    private void handleGlassAction(int keyCode) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_ENTER:
                // ENTER
                performCameraButtonAction();
                break;

            case KeyEvent.KEYCODE_DPAD_UP:
                // RIGHT
                performSwipeToPhoto();
                break;

            case KeyEvent.KEYCODE_DPAD_DOWN:
                // LEFT
                performSwipeToVideo();
                break;

            default:
                break;
        }
    }

    /**
     * Button UI
     */
    private void initRecyclerView() {
        Log.d(TAG, "initRecyclerView: init recyclerview");
        ArrayList<String> mCameraModes = new ArrayList<>();
        mCameraModes.add("               ");
        mCameraModes.add(getResources().getString(R.string.CAMERAMODE_PHOTO));
        mCameraModes.add(getResources().getString(R.string.CAMERAMODE_VIDEO));
        mCameraModes.add("              ");

        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        mRecyclerView = findViewById(R.id.recyclerView);
        mRecyclerView.setLayoutManager(layoutManager);
        RecyclerViewAdapter adapter = new RecyclerViewAdapter(this, mCameraModes);
        mRecyclerView.setAdapter(adapter);

    }

    /**
     * Update Text under the button
     *
     * @param cameraMode : update UI depends on the mode
     */
    private void updateButtonText(final CameraMode cameraMode) {
        if (cameraMode == CameraMode.PHOTO_STOPPED) {
            mIVCameraButton.setImageDrawable(getResources().getDrawable(R.drawable.shutterinactive));
        } else if (cameraMode == CameraMode.PHOTO_TAKING) {
            mIVCameraButton.setImageDrawable(getResources().getDrawable(R.drawable.shutteractive));
            new Handler(getMainLooper()).postDelayed(new Runnable() {
                @Override
                public void run() {
                    mIVCameraButton.setImageDrawable(getResources().getDrawable(R.drawable.shutterinactive));
                    touchpadIsDisabled = false;
                }
            }, touchpadAnimationInterval);
            this.mCameraMode = CameraMode.PHOTO_STOPPED;
        } else if (cameraMode == CameraMode.VIDEO_STOPPED) {
            mIVCameraButton.setImageDrawable(getResources().getDrawable(R.drawable.videoinactive));
        } else if (cameraMode == CameraMode.VIDEO_RECORDING) {
            mIVCameraButton.setImageDrawable(getResources().getDrawable(R.drawable.videoactive));
        }
    }

    /**
     * Switch to photo button
     */
    private void initCameraModeForPhoto() {

        View view;
        TextView textView;
        Typeface typeface;
        view = mRecyclerView.findViewHolderForAdapterPosition(1).itemView;
        textView = view.findViewById(R.id.tvCameraMode);
        textView.setTextSize(getResources().getDimension(R.dimen.font_size_text_highlighted));
        textView.setTextColor(getColor(R.color.color_text_highlight));
        textView.setPadding(
                Utils.getDPFromPx(this, Constants.CAMERA_MODE_TEXT_PADDING_LEFT),
                0,
                Utils.getDPFromPx(this, Constants.CAMERA_MODE_TEXT_PADDING_RIGHT),
                0);
//        typeface = ResourcesCompat.getFont(this, R.font.notosanscjk_medium);
//        textView.setTypeface(typeface);
        textView.setTypeface(null, Typeface.BOLD);
        view = mRecyclerView.findViewHolderForAdapterPosition(2).itemView;
        textView = view.findViewById(R.id.tvCameraMode);
        textView.setTextSize(getResources().getDimension(R.dimen.font_size_text_default));
        textView.setTextColor(getColor(R.color.color_text_default));
        textView.setPadding(
                Utils.getDPFromPx(this, Constants.CAMERA_MODE_TEXT_PADDING_LEFT),
                Utils.getDPFromPx(this, Constants.CAMERA_MODE_TEXT_PADDING_TOP),
                Utils.getDPFromPx(this, Constants.CAMERA_MODE_TEXT_PADDING_RIGHT),
                0);
//        typeface = ResourcesCompat.getFont(this, R.font.notosanscjk_regular);
//        textView.setTypeface(typeface);
        textView.setTypeface(null, Typeface.NORMAL);
    }

    /**
     * Switch to video button
     */
    private void initCameraModeForVideo() {
        View view;
        TextView textView;
        Typeface typeface;
        view = mRecyclerView.findViewHolderForAdapterPosition(1).itemView;
        textView = view.findViewById(R.id.tvCameraMode);
        textView.setTextSize(getResources().getDimension(R.dimen.font_size_text_default));
        textView.setTextColor(getColor(R.color.color_text_default));
        textView.setPadding(
                Utils.getDPFromPx(this, Constants.CAMERA_MODE_TEXT_PADDING_LEFT),
                Utils.getDPFromPx(this, Constants.CAMERA_MODE_TEXT_PADDING_TOP),
                Utils.getDPFromPx(this, Constants.CAMERA_MODE_TEXT_PADDING_RIGHT),
                0);
//        typeface = ResourcesCompat.getFont(this, R.font.notosanscjk_regular);
//        textView.setTypeface(typeface);
        textView.setTypeface(null, Typeface.NORMAL);
        view = mRecyclerView.findViewHolderForAdapterPosition(2).itemView;
        textView = view.findViewById(R.id.tvCameraMode);
        textView.setTextSize(getResources().getDimension(R.dimen.font_size_text_highlighted));
        textView.setTextColor(getColor(R.color.color_text_highlight));
        textView.setPadding(
                Utils.getDPFromPx(this, Constants.CAMERA_MODE_TEXT_PADDING_LEFT),
                0,
                Utils.getDPFromPx(this, Constants.CAMERA_MODE_TEXT_PADDING_RIGHT),
                0);
//        typeface = ResourcesCompat.getFont(this, R.font.notosanscjk_medium);
//        textView.setTypeface(typeface);
        textView.setTypeface(null, Typeface.BOLD);
    }

    /**
     * Video recording progress UI
     */
    private void disableProgressTextView() {
        mRecyclerView.setVisibility(View.VISIBLE);
        mLinearLayoutVideoProgress.setVisibility(View.GONE);
    }

    /**
     * Video recording progress UI
     */
    private void enableProgressTextView() {
        mRecyclerView.setVisibility(View.GONE);
        mLinearLayoutVideoProgress.setVisibility(View.VISIBLE);

    }

    /**
     * Init UI
     */
    private void initCameraButton() {
        mCameraMode = CameraMode.PHOTO_STOPPED;
        updateButtonText(mCameraMode);
    }

    /**
     * Init preview
     *
     * Default mode is photo
     */
    private void initPreview() {
        // Create our Preview view and set it as the content of our activity.
        mCameraMode = CameraMode.PHOTO_STOPPED;
    }

    /**
     * Key Event action : swipe
     */
    private void performSwipeToVideo() {
        // only can swipe to video if not currently recording
        if (mCameraMode != CameraMode.VIDEO_RECORDING) {
            mCameraMode = CameraMode.VIDEO_STOPPED;
            updateButtonText(mCameraMode);
            mRecyclerView.smoothScrollToPosition(3);
            initCameraModeForVideo();
        }
    }

    /**
     * Key Event action : swipe
     */
    private void performSwipeToPhoto() {
        // only can swipe to photo if not currently recording
        if (mCameraMode != CameraMode.VIDEO_RECORDING) {
            mCameraMode = CameraMode.PHOTO_STOPPED;
            updateButtonText(mCameraMode);
            mRecyclerView.smoothScrollToPosition(0);
            initCameraModeForPhoto();
            initPreview();
        }
    }

    /**
     * Key Event action: press the button
     */
    private void performCameraButtonAction() {
        if (mCameraMode == CameraMode.PHOTO_STOPPED) {
            mCameraMode = CameraMode.PHOTO_TAKING;
            touchpadIsDisabled = true;
            // get an image from the camera
            handleStillPictureButton();
            updateButtonText(mCameraMode);
            // sound
            playSoundPhoto();
        } else {
            handleVideoButton();
        }
    }

    /**
     * Video button event
     */
    private void handleVideoButton() {
        if (mIsRecording) {
            mRokidCamera.stopRecording();
            // restart preview
            mRokidCamera.startPreview();
            // sound
            playSoundVideoStop();
        } else {
            mIsRecording = true;
            mCameraMode = CameraMode.VIDEO_RECORDING;
            updateButtonText(mCameraMode);
            enableProgressTextView();
            mRokidCamera.startVideoRecording();
            // sound
            playSoundVideoStart();
        }
    }

    /**
     * Try to take picture
     */
    private void handleStillPictureButton() {
        mRokidCamera.takeStillPicture();
    }

    private void initApp() {
        initRecyclerView();
        initCameraButton();
        initPreview();
        mRokidCamera.onStart();
        touchpadIsDisabled = false;
    }

    private boolean arePermissionsGranted() {
        return (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);
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

    @Override
    public void onRokidCameraFileSaved() {
        // UI update or other actions
    }

    @Override
    public void onRokidCameraOpened() {
        // UI update or other actions
    }

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
        mCameraMode = CameraMode.VIDEO_STOPPED;
        updateButtonText(mCameraMode);
        disableProgressTextView();

        // app state and UI
        mIsRecording = false;
    }

    /**
     *
     *
     * @param image
     */
    @Override
    public void onRokidCameraImageAvailable(Image image) {
        // handle Image object here
    }

    public void playSoundPhoto()  {
        if(loaded)  {
            float leftVolumn = volume;
            float rightVolumn = volume;
            // Play sound of gunfire. Returns the ID of the new stream.
            int streamId = this.soundPool.play(this.soundIdPhoto,leftVolumn, rightVolumn, 1, 0, 1f);
        }
    }

    public void playSoundVideoStart()  {
        if(loaded)  {
            float leftVolumn = volume;
            float rightVolumn = volume;
            // Play sound of gunfire. Returns the ID of the new stream.
            int streamId = this.soundPool.play(this.soundIdVideoStart,leftVolumn, rightVolumn, 1, 0, 1f);
        }
    }

    public void playSoundVideoStop()  {
        if(loaded)  {
            float leftVolumn = volume;
            float rightVolumn = volume;
            // Play sound of gunfire. Returns the ID of the new stream.
            int streamId = this.soundPool.play(this.soundIdVideoStop,leftVolumn, rightVolumn, 1, 0, 1f);
        }
    }


}
