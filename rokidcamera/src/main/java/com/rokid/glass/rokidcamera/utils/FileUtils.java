package com.rokid.glass.rokidcamera.utils;

import android.os.Environment;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by yihan on 7/25/18.
 */

public class FileUtils {


    // TODO: put to a Util class
    public static File createVideoFolder() {
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

    public static File createVideoFile(File mVideoFolder) throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyHHdd_HHmmss").format(new Date());
        String prepend = "ROKIDVIDEO_" + timeStamp;
        File videoFile = File.createTempFile(prepend, ".mp4", mVideoFolder);
//        mVideoFileName = videoFile.getAbsolutePath();
        return videoFile;
    }

    public static File createImageFolder() {
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

    public static File createImageFile(File mImageFolder) throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyHHdd_HHmmss").format(new Date());
        String prepend = "ROKIDIMAGE_" + timeStamp;
//        File imageFile = File.createTempFile("ROKIDTEST", ".jpg", mImageFolder);
        File imageFile = new File(mImageFolder, prepend + ".jpg");
//        mImageFileName = imageFile.getAbsolutePath();
        return imageFile;
    }
}
