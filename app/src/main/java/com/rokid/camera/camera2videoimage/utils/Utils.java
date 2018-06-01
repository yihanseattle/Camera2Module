package com.rokid.camera.camera2videoimage.utils;

import android.content.Context;

/**
 * Created by yihan on 5/20/18.
 */

public class Utils {

    public static String convertToHourMinuteSecond(int biggy)
    {
        long longVal = biggy;
        int hours = (int) longVal / 3600;
        int remainder = (int) longVal - hours * 3600;
        int mins = remainder / 60;
        remainder = remainder - mins * 60;
        int secs = remainder;

        return (hours < 10 ? "0" + hours : hours) + ":"
                + (mins < 10 ? "0" + mins : mins) + ":"
                + (secs < 10 ? "0" + secs : secs);
    }

    public static int getDPFromPx(Context context, int dp) {
        float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5f);
    }
}
