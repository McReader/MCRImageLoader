package by.mcreader.imageloader.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;

/**
 * Created by dzianis_roi on 22.07.2014.
 */
public class ReusableBitmapUtil {

    private ReusableBitmapUtil() {
    }

    public static boolean canUseForInBitmap(Bitmap candidate, BitmapFactory.Options targetOptions) {

        if (AndroidVersions.hasKitKat()) {
            // From Android 4.4 (KitKat) onward we can re-use if the byte size of
            // the new bitmap is smaller than the reusable bitmap candidate
            // allocation byte count.
            int width = targetOptions.outWidth / targetOptions.inSampleSize;
            int height = targetOptions.outHeight / targetOptions.inSampleSize;
            int byteCount = width * height * getBytesPerPixel(candidate.getConfig());
            return byteCount <= byteSizeOf(candidate);
        }

        // On earlier versions, the dimensions must match exactly and the inSampleSize must be 1
        return candidate.getWidth() >= targetOptions.outWidth && candidate.getHeight() >= targetOptions.outHeight
                && targetOptions.inSampleSize == 1;
    }

    public static int byteSizeOf(Bitmap bitmap) {

        if (bitmap == null) return 0;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {

            return bitmap.getAllocationByteCount();

        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1) {

            return bitmap.getByteCount();

        } else {

            return bitmap.getRowBytes() * bitmap.getHeight();

        }
    }

    /**
     * A helper function to return the byte usage per pixel of a bitmap based on its configuration.
     */
    public static int getBytesPerPixel(Bitmap.Config config) {

        if (config == Bitmap.Config.ARGB_8888) {
            return 4;
        } else if (config == Bitmap.Config.RGB_565) {
            return 2;
        } else if (config == Bitmap.Config.ARGB_4444) {
            return 2;
        } else if (config == Bitmap.Config.ALPHA_8) {
            return 1;
        }
        return 1;
    }
}
