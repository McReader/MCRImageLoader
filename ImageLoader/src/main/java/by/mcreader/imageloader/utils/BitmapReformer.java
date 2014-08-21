package by.mcreader.imageloader.utils;

import android.graphics.Bitmap;
import android.graphics.Matrix;

public class BitmapReformer {

    private static final String TAG = BitmapReformer.class.getSimpleName();

    private BitmapReformer() {
    }

    public static Bitmap rotate(Bitmap target, Integer degree) {

        if (degree != null) {

            Matrix mtx = new Matrix();

            mtx.postRotate(degree);

            return Bitmap.createBitmap(target, 0, 0, target.getWidth(), target.getHeight(), mtx, true);
        }

        return target;
    }
}
