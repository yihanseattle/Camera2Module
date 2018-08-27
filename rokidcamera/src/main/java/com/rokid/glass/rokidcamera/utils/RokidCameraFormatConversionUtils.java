package com.rokid.glass.rokidcamera.utils;

import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.media.Image;

import java.nio.ByteBuffer;

/**
 *
 * Created by yihan on 8/23/18.
 */

public class RokidCameraFormatConversionUtils {

    /**
     * Code got from BB8 project.
     * <p>
     * Transform from YUV_420_888 Image object to NV21 byte array.
     * <p>
     * Note: It seems that YUV_420_888 is essentially the same as NV21. The only difference
     * is that YUV_420_888 is wrapped inside an Image object by Android Camera2 API, while NV21
     * is often presented as a byte array directly (see Android Camera1 API callback argument list).
     * <p>
     * TODO: @Zhiyu mentioned that the "getCropRect" function may not work on latest Android API.
     */
    public static byte[] YUV_420_888toNV21(Image image) {
        Rect crop = image.getCropRect();
        int format = image.getFormat();
        int width = crop.width();
        int height = crop.height();
        Image.Plane[] planes = image.getPlanes();
        byte[] data = new byte[width * height * ImageFormat.getBitsPerPixel(format) / 8];
        byte[] rowData = new byte[planes[0].getRowStride()];

        int channelOffset = 0;
        int outputStride = 1;
        for (int i = 0; i < planes.length; i++) {
            switch (i) {
                case 0:
                    channelOffset = 0;
                    outputStride = 1;
                    break;
                case 1:
                    channelOffset = width * height + 1;
                    outputStride = 2;
                    break;
                case 2:
                    channelOffset = width * height;
                    outputStride = 2;
                    break;
            }

            ByteBuffer buffer = planes[i].getBuffer();
            int rowStride = planes[i].getRowStride();
            int pixelStride = planes[i].getPixelStride();

            int shift = (i == 0) ? 0 : 1;
            int w = width >> shift;
            int h = height >> shift;
            buffer.position(rowStride * (crop.top >> shift) + pixelStride * (crop.left >> shift));
            for (int row = 0; row < h; row++) {
                int length;
                if (pixelStride == 1 && outputStride == 1) {
                    length = w;
                    buffer.get(data, channelOffset, length);
                    channelOffset += length;
                } else {
                    length = (w - 1) * pixelStride + 1;
                    buffer.get(rowData, 0, length);
                    for (int col = 0; col < w; col++) {
                        data[channelOffset] = rowData[col * pixelStride];
                        channelOffset += outputStride;
                    }
                }
                if (row < h - 1) {
                    buffer.position(buffer.position() + rowStride - length);
                }
            }
        }
        return data;
    }

}
