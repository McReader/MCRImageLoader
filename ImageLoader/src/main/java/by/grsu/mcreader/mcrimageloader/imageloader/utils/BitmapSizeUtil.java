package by.grsu.mcreader.mcrimageloader.imageloader.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;

public class BitmapSizeUtil {

    private BitmapSizeUtil() {
    }

    public static boolean inspectDimensions(BitmapDrawable drawable, int requiredWidth, int requiredHeight) {

        // TODO: check null pointer
        return drawable != null && inspectDimensions(drawable.getBitmap(), requiredWidth, requiredHeight);

    }

    public static boolean inspectDimensions(Bitmap bitmap, int requiredWidth, int requiredHeight) {

        // TODO: check null pointer
        return bitmap != null && bitmap.getWidth() == requiredWidth && bitmap.getHeight() == requiredHeight;

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
