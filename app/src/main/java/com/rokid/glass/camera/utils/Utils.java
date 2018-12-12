package com.rokid.glass.camera.utils;

import android.content.Context;

/**
 * Helper methods.
 */

public class Utils {

    /**
     * Convert pixel to dp.
     *
     * @param context : application context
     * @param pixel : input pixel
     * @return : output dp
     */
    public static int getDPFromPx(Context context, int pixel) {
        float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pixel * scale + 0.5f);
    }
}
