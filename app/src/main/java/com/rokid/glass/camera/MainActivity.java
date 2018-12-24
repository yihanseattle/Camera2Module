package com.rokid.glass.camera;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.ImageFormat;
import android.graphics.Typeface;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.Image;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
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
import com.rokid.glass.camera.utils.PermissionHelper;
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
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements
        RokidCameraIOListener,
        RokidCameraStateListener,
        RokidCameraVideoRecordingListener,
        RokidCameraOnImageAvailableListener {

    public static final String TAG = "Camera2VideoImage";

    public void setPermissionHelper(PermissionHelper permissionHelper) {
        mPermissionHelper = permissionHelper;
    }

    // permission helper to check and request permission
    private PermissionHelper mPermissionHelper;

    // sound related
    private SoundPool mSoundPool;
    private AudioManager mAudioManager;
    // Maximum sound stream.
    private static final int MAX_STREAMS = 5;
    // Stream type.
    private static final int streamType = AudioManager.STREAM_MUSIC;
    private boolean mLoaded;
    private int mSoundIdPhoto;
    private int mSoundIdVideoStart;
    private int mSoundIdVideoStop;
    private float mVolume;

    // disable button press(touch pad event) during animation
    private boolean mTouchpadIsDisabled;
    // animation
    private final int mTouchpadAnimationInterval = 300;

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

        mPermissionHelper = new PermissionHelper(this);

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
    }

    private boolean mIsWakeupAlways = false;
    private boolean mNeedToContinueOnPause = false;
    private final static String DB_WAKEUP_KEY = "rokid_wakeup_setting";

    @Override
    protected void onResume() {
        super.onResume();
        int wakeup = Settings.Global.getInt(
                MainActivity.this.getContentResolver(),
                DB_WAKEUP_KEY, 0);
        mIsWakeupAlways = wakeup != 0 ? true : false;
        mNeedToContinueOnPause = mIsWakeupAlways;
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mNeedToContinueOnPause) {
            // 退出录像，继续语音播放
            sendContinueServer();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mPermissionHelper.arePermissionsGranted()) {
            initApp();
        } else {
            mPermissionHelper.requestAllPermissions();
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

    private void initApp() {
        initRecyclerView();
        initCameraButton();
        initPreview();
        mRokidCamera.onStart();
        mTouchpadIsDisabled = false;
    }

    private void initSound() {
        // AudioManager audio settings for adjusting the mVolume
        mAudioManager = (AudioManager) getSystemService(AUDIO_SERVICE);

        // Current volumn Index of particular stream type.
        float currentVolumeIndex = 0;
        if (mAudioManager != null) {
            currentVolumeIndex = (float) mAudioManager.getStreamVolume(streamType);
        }

        // Get the maximum mVolume index for a particular stream type.
        float maxVolumeIndex  = 0;
        if (mAudioManager != null) {
            maxVolumeIndex = (float) mAudioManager.getStreamMaxVolume(streamType);
        }

        // Volumn (0 --> 1)
        this.mVolume = currentVolumeIndex / maxVolumeIndex;

        // Suggests an audio stream whose mVolume should be changed by
        // the hardware mVolume controls.
        this.setVolumeControlStream(streamType);

        AudioAttributes audioAttrib = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();

        SoundPool.Builder builder= new SoundPool.Builder();
        builder.setAudioAttributes(audioAttrib).setMaxStreams(MAX_STREAMS);

        this.mSoundPool = builder.build();

        // When Sound Pool load complete.
        this.mSoundPool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
            @Override
            public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
                mLoaded = true;
            }
        });

        // Load sound file (photo.wav) into SoundPool.
        this.mSoundIdPhoto = this.mSoundPool.load(this, R.raw.photo,1);
        // Load sound file (video_start.wav) into SoundPool.
        this.mSoundIdVideoStart = this.mSoundPool.load(this, R.raw.video_start,1);
        // Load sound file (video_stop.wav) into SoundPool.
        this.mSoundIdVideoStop = this.mSoundPool.load(this, R.raw.video_stop,1);
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
                String formattedTime = hh + ":" + mm + ":" + ss;
                mChronometer.setText(formattedTime);
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

    /**
     * Button UI
     */
    private void initRecyclerView() {
        ArrayList<String> mCameraModes = new ArrayList<>();

        Locale chineseLang = new Locale(Locale.CHINESE.getLanguage());
        Locale englishLang = new Locale(Locale.ENGLISH.getLanguage());
        if (Resources.getSystem().getConfiguration().locale.getLanguage().equals(englishLang.getLanguage())) {
            // placeholder at first and last position
            mCameraModes.add("                  ");
            mCameraModes.add(getResources().getString(R.string.CAMERAMODE_PHOTO));
            mCameraModes.add(getResources().getString(R.string.CAMERAMODE_VIDEO));
            mCameraModes.add("                  ");
        } else if (Resources.getSystem().getConfiguration().locale.getLanguage().equals(chineseLang.getLanguage())) {
            // system language is Chinese
            // placeholder at first and last position
            mCameraModes.add("                 ");
            mCameraModes.add(getResources().getString(R.string.CAMERAMODE_PHOTO));
            mCameraModes.add(getResources().getString(R.string.CAMERAMODE_VIDEO));
            mCameraModes.add("                 ");
        }



        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        mRecyclerView = findViewById(R.id.recyclerView);
        mRecyclerView.setLayoutManager(layoutManager);
        RecyclerViewAdapter adapter = new RecyclerViewAdapter(this, mCameraModes);
        mRecyclerView.setAdapter(adapter);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (!mTouchpadIsDisabled) {
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

            case KeyEvent.KEYCODE_DPAD_DOWN:
                // RIGHT
                performSwipeToPhoto();
                break;

            case KeyEvent.KEYCODE_DPAD_UP:
                // LEFT
                performSwipeToVideo();
                break;

            default:
                break;
        }
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
                    mTouchpadIsDisabled = false;
                }
            }, mTouchpadAnimationInterval);
            this.mCameraMode = CameraMode.PHOTO_STOPPED;
        } else if (cameraMode == CameraMode.VIDEO_STOPPED) {
            mIVCameraButton.setImageDrawable(getResources().getDrawable(R.drawable.videoinactive));
        } else if (cameraMode == CameraMode.VIDEO_RECORDING) {
            mIVCameraButton.setImageDrawable(getResources().getDrawable(R.drawable.videoactive));
        }
    }

    /**
     * Switch to photo button.
     * RecyclerView has placeholder at position 0 and 3.
     */
    private void initCameraModeForPhoto() {
        View view;
        TextView textView;
        Typeface typeface;
        // 拍照
        view = mRecyclerView.findViewHolderForAdapterPosition(1).itemView;
        textView = view.findViewById(R.id.tvCameraMode);
        textView.setTextSize(getResources().getDimension(R.dimen.font_size_text_highlighted));
        textView.setTextColor(getColor(R.color.color_text_highlight));
        textView.setPadding(
                Utils.getDPFromPx(this, Constants.CAMERA_MODE_TEXT_PADDING_LEFT),
                0,
                Utils.getDPFromPx(this, Constants.CAMERA_MODE_TEXT_PADDING_RIGHT),
                0);
        typeface = ResourcesCompat.getFont(this, R.font.notosanscjk_medium);
        textView.setTypeface(typeface);
        textView.setIncludeFontPadding(false);
        // 摄像
        view = mRecyclerView.findViewHolderForAdapterPosition(2).itemView;
        textView = view.findViewById(R.id.tvCameraMode);
        textView.setTextSize(getResources().getDimension(R.dimen.font_size_text_default));
        textView.setTextColor(getColor(R.color.color_text_default));
        textView.setPadding(
                Utils.getDPFromPx(this, Constants.CAMERA_MODE_TEXT_PADDING_LEFT),
                Utils.getDPFromPx(this, Constants.CAMERA_MODE_TEXT_PADDING_TOP),
                Utils.getDPFromPx(this, Constants.CAMERA_MODE_TEXT_PADDING_RIGHT),
                0);
        typeface = ResourcesCompat.getFont(this, R.font.notosanscjk_regular);
        textView.setTypeface(typeface);
        textView.setIncludeFontPadding(false);
    }

    /**
     * Switch to video button
     * RecyclerView has placeholder at position 0 and 3.
     */
    private void initCameraModeForVideo() {
        View view;
        TextView textView;
        Typeface typeface;
        // 拍照
        view = mRecyclerView.findViewHolderForAdapterPosition(1).itemView;
        textView = view.findViewById(R.id.tvCameraMode);
        textView.setTextSize(getResources().getDimension(R.dimen.font_size_text_default));
        textView.setTextColor(getColor(R.color.color_text_default));
        textView.setPadding(
                Utils.getDPFromPx(this, Constants.CAMERA_MODE_TEXT_PADDING_LEFT),
                Utils.getDPFromPx(this, Constants.CAMERA_MODE_TEXT_PADDING_TOP),
                Utils.getDPFromPx(this, Constants.CAMERA_MODE_TEXT_PADDING_RIGHT),
                0);
        typeface = ResourcesCompat.getFont(this, R.font.notosanscjk_regular);
        textView.setTypeface(typeface);
        textView.setIncludeFontPadding(false);
        // 摄像
        view = mRecyclerView.findViewHolderForAdapterPosition(2).itemView;
        textView = view.findViewById(R.id.tvCameraMode);
        textView.setTextSize(getResources().getDimension(R.dimen.font_size_text_highlighted));
        textView.setTextColor(getColor(R.color.color_text_highlight));
        textView.setPadding(
                Utils.getDPFromPx(this, Constants.CAMERA_MODE_TEXT_PADDING_LEFT),
                0,
                Utils.getDPFromPx(this, Constants.CAMERA_MODE_TEXT_PADDING_RIGHT),
                0);
        typeface = ResourcesCompat.getFont(this, R.font.notosanscjk_medium);
        textView.setTypeface(typeface);
        textView.setIncludeFontPadding(false);
    }

    /**
     * Init UI
     */
    private void initCameraButton() {
        mCameraMode = CameraMode.PHOTO_STOPPED;
        updateButtonText(mCameraMode);
    }

    /**
     * Init camera preview
     *
     * Default mode is photo
     */
    private void initPreview() {
        // Create our Preview view and set it as the content of our activity.
        mCameraMode = CameraMode.PHOTO_STOPPED;
    }

    /**
     * Video recording progress UI
     */
    private void disableProgressTextView() {
//        mRecyclerView.setVisibility(View.VISIBLE);
        mLinearLayoutVideoProgress.setVisibility(View.GONE);
    }

    /**
     * Video recording progress UI
     */
    private void enableProgressTextView() {
//        mRecyclerView.setVisibility(View.GONE);
        mLinearLayoutVideoProgress.setVisibility(View.VISIBLE);

    }

    /**
     * Key Event action: press the button
     */
    private void performCameraButtonAction() {
        if (mCameraMode == CameraMode.PHOTO_STOPPED) {
            mCameraMode = CameraMode.PHOTO_TAKING;
            mTouchpadIsDisabled = true;
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
     * 发布停止语音录音命令
     */
    private void sendPauseServer() {
        Intent intent = new Intent();
        intent.setAction("com.rokid.glass.audio.layer.command");
        intent.putExtra("LAYER_ITENT_COMMAND", "PAUSE_AUDIO_SERVER");
        intent.putExtra("PACKAGE_NAME", this.getPackageName());
        intent.putExtra("ACTIVITY_NAME", this.getComponentName().getClassName());
        intent.addCategory(Intent.CATEGORY_INFO);
        this.sendBroadcast(intent);
    }

    /**
     * 发布继续语音录音命令
     */
    private void sendContinueServer() {
        Intent intent = new Intent();
        intent.setAction("com.rokid.glass.audio.layer.command");
        intent.putExtra("LAYER_ITENT_COMMAND", "CONTINUE_AUDIO_SERVER");
        intent.putExtra("PACKAGE_NAME", this.getPackageName());
        intent.putExtra("ACTIVITY_NAME", this.getComponentName().getClassName());
        intent.addCategory(Intent.CATEGORY_INFO);
        this.sendBroadcast(intent);
    }

    /**
     * Key Event action : swipe
     */
    private void performSwipeToVideo() {
        // only can swipe to video if not currently recording
        if (mCameraMode != CameraMode.VIDEO_RECORDING) {

            // 进入录像，停止语音播放
            if (mIsWakeupAlways) {
                sendPauseServer();
                new Handler(getMainLooper()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mCameraMode = CameraMode.VIDEO_STOPPED;
                        updateButtonText(mCameraMode);
                        mRecyclerView.smoothScrollToPosition(3);
                        initCameraModeForVideo();
                    }
                }, 200);
            }
            else {
                mCameraMode = CameraMode.VIDEO_STOPPED;
                updateButtonText(mCameraMode);
                mRecyclerView.smoothScrollToPosition(3);
                initCameraModeForVideo();
            }
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

            if (mIsWakeupAlways) {
                // 退出录像，继续语音播放
                sendContinueServer();
                mNeedToContinueOnPause = false;
            }
        }
    }

    /**
     * Video button event
     */
    private void handleVideoButton() {
        if (mIsRecording) {
            mRokidCamera.stopRecording();
            // restart preview
            mRokidCamera.createCameraPreviewSession();
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

    /**
     * Callback when Video Recording starts in RokidCamera.
     * Now, the app can update accordingly.
     */
    @Override
    public void onRokidCameraRecordingStarted() {
        // UI update
        mChronometer.setBase(SystemClock.elapsedRealtime());
        mChronometer.setVisibility(View.VISIBLE);
        mChronometer.start();
        mIsRecording = true;
    }

    /**
     * Callback when Video Recording stops in RokidCamera.
     * Now, the app can update accordingly.
     */
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
     * Callback when file is saved successfully in RokidCamera module.
     */
    @Override
    public void onRokidCameraFileSaved() {
        // UI update or other actions
    }

    /**
     * Callback when Camera is opened successfully in RokidCamera module.
     */
    @Override
    public void onRokidCameraOpened() {
        // UI update or other actions
    }

    /**
     * Callbacks for Image from ImageReader in RokidCamera module
     * @param image : Image object from ImageReader
     */
    @Override
    public void onRokidCameraImageAvailable(Image image) {
        // handle Image object here
    }



    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (mPermissionHelper.arePermissionsGranted()) {
            initApp();
        } else {
            Toast.makeText(this, "Please grant all permission so the app will work properly.", Toast.LENGTH_SHORT).show();
        }
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

    /**
     * Play sound for taking a photo.
     */
    private void playSoundPhoto()  {
        if(mLoaded)  {
            float leftVolumn = mVolume;
            float rightVolumn = mVolume;
            this.mSoundPool.play(this.mSoundIdPhoto,leftVolumn, rightVolumn, 1, 0, 1f);
        }
    }

    /**
     * Play a sound when video recording starts.
     */
    private void playSoundVideoStart()  {
        if(mLoaded)  {
            float leftVolumn = mVolume;
            float rightVolumn = mVolume;
            this.mSoundPool.play(this.mSoundIdVideoStart,leftVolumn, rightVolumn, 1, 0, 1f);
        }
    }

    /**
     * Play a sound when video recording stops.
     */
    private void playSoundVideoStop()  {
        if(mLoaded)  {
            float leftVolumn = mVolume;
            float rightVolumn = mVolume;
            this.mSoundPool.play(this.mSoundIdVideoStop,leftVolumn, rightVolumn, 1, 0, 1f);
        }
    }

    public PermissionHelper getPermissionHelper() {
        return mPermissionHelper;
    }

    public Chronometer getChronometer() {
        return mChronometer;
    }

    public void setChronometer(Chronometer chronometer) {
        mChronometer = chronometer;
    }

    public ImageView getIVCameraButton() {
        return mIVCameraButton;
    }

    public void setIVCameraButton(ImageView IVCameraButton) {
        mIVCameraButton = IVCameraButton;
    }

    public ImageView getIVRecordingRedDot() {
        return mIVRecordingRedDot;
    }

    public void setIVRecordingRedDot(ImageView IVRecordingRedDot) {
        mIVRecordingRedDot = IVRecordingRedDot;
    }

    public RecyclerView getRecyclerView() {
        return mRecyclerView;
    }

    public void setRecyclerView(RecyclerView recyclerView) {
        mRecyclerView = recyclerView;
    }

}
