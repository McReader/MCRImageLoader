package by.grsu.mcreader.mcrimageloader.imageloader.utils;

import android.graphics.Bitmap;
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

}
