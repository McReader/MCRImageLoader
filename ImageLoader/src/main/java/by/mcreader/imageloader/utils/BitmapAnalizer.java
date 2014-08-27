package by.mcreader.imageloader.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.media.ExifInterface;
import android.text.TextUtils;
import android.util.Log;

import java.io.IOException;

/**
 * Created by Dzianis_Roi on 20.08.2014.
 */
public class BitmapAnalizer {

    private static final String TAG = BitmapAnalizer.class.getSimpleName();

    private BitmapAnalizer() {
    }

    public static int countRotationDegree(String path) {

        if (TextUtils.isEmpty(path)) return -1;

        try {

            ExifInterface exif = new ExifInterface(path);

            String orientation = exif.getAttribute(ExifInterface.TAG_ORIENTATION);

            int degree = 0;

            if (orientation.equalsIgnoreCase("6")) {
                degree = 90;
            } else if (orientation.equalsIgnoreCase("8")) {
                degree = 270;
            } else if (orientation.equalsIgnoreCase("3")) {
                degree = 180;
            }

            return degree;

        } catch (IOException e) {

            Log.e(TAG, TextUtils.isEmpty(e.getMessage()) ? "Error defineRotationDegree" : e.getMessage());

        }

        return -1;
    }

    public static BitmapDrawable inspectBitmap(BitmapDrawable bitmapDrawable, int reqWidth, int reqHeight) {

        if (bitmapDrawable == null) return null;

        boolean careAboutSize = reqWidth != -1 && reqHeight != -1;

        if (!careAboutSize) {

            return bitmapDrawable;

        } else if (inspectDimensions(bitmapDrawable, reqWidth, reqHeight)) {

            return bitmapDrawable;

        }

        return null;
    }

    private static boolean inspectDimensions(BitmapDrawable existsDrawable, int reqWidth, int reqHeight) {

        Bitmap bitmap = existsDrawable.getBitmap();

        if (bitmap == null || reqHeight <= 0 || reqWidth <= 0) return false;

        final int heightRatio = (int) Math.floor((float) bitmap.getWidth() / (float) reqHeight);
        final int widthRatio = (int) Math.floor((float) bitmap.getHeight() / (float) reqWidth);

        return heightRatio == 1 || widthRatio == 1;

    }

    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {

        final int height = options.outHeight;
        final int width = options.outWidth;

        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int heightRatio = Math.round((float) height / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);

            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
        }

        return inSampleSize;
    }
}
