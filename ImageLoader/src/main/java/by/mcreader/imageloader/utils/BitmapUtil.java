package by.mcreader.imageloader.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.media.ExifInterface;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;

import java.io.IOException;

/**
 * Created by dzianis_roi on 22.07.2014.
 */
public class BitmapUtil {

    private static final String TAG = BitmapUtil.class.getSimpleName();

    private BitmapUtil() {
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

    public static Bitmap rotate(Bitmap target, Integer degree) {
        if (degree != null && degree != 0) {

            Matrix mtx = new Matrix();

            mtx.postRotate(degree);

            Bitmap result = Bitmap.createBitmap(target, 0, 0, target.getWidth(), target.getHeight(), mtx, true);

            target.recycle();

            return result;
        }

        return target;
    }

    // TODO should be in other place
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

    public static boolean isMatchedSize(BitmapDrawable bd, int[] size) {
        return bd != null && isMatchedSize(bd.getBitmap(), size);
    }

    public static boolean isMatchedSize(Bitmap b, int[] size) {
        if (b == null) return false;

        int reqWidth = -1, reqHeight = -1;

        if (size != null && size.length == 2) {
            reqWidth = size[0];
            reqHeight = size[1];
        }

        if (reqWidth == -1 || reqHeight == -1) return true;

        final int heightRatio = (int) Math.floor((float) b.getWidth() / (float) reqHeight);
        final int widthRatio = (int) Math.floor((float) b.getHeight() / (float) reqWidth);

        return heightRatio == 1 || widthRatio == 1;
    }

    public static int calculateInSampleSize(BitmapFactory.Options options, int[] size) {

        final int height = options.outHeight;
        final int width = options.outWidth;

        int inSampleSize = 1;

        if (height > size[1] || width > size[0]) {

            final int heightRatio = Math.round((float) height / (float) size[1]);
            final int widthRatio = Math.round((float) width / (float) size[0]);

            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
        }

        return inSampleSize;
    }
}
