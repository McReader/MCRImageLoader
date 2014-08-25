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

    private static final int ALLOWED_INFELICITY = 300;

    private BitmapAnalizer() {
    }

    public static int analizRotationDegree(String path) {

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

    public static boolean inspectDimensions(BitmapDrawable drawable, int requiredWidth, int requiredHeight) {

        return drawable != null && inspectDimensions(drawable.getBitmap(), requiredWidth, requiredHeight);

    }

    public static boolean inspectDimensions(Bitmap bitmap, int requiredWidth, int requiredHeight) {

        if (bitmap == null) return false;

        int actualWidth = bitmap.getWidth(), actualHeight = bitmap.getHeight();

        return actualWidth <= requiredWidth + ALLOWED_INFELICITY && actualWidth >= requiredWidth - ALLOWED_INFELICITY
                &&
                actualHeight <= requiredHeight + ALLOWED_INFELICITY && actualHeight >= requiredHeight - ALLOWED_INFELICITY;
    }

    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {

        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;

        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            // Calculate ratios of height and width to requested height and
            // width
            final int heightRatio = Math.round((float) height / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);

            // Choose the smallest ratio as inSampleSize value, this will
            // guarantee
            // a final image with both dimensions larger than or equal to the
            // requested height and width.
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
        }

        return inSampleSize;
    }

}
