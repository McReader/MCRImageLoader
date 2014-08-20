package by.grsu.mcreader.mcrimageloader.imageloader.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.media.ExifInterface;
import android.text.TextUtils;
import android.util.Log;

import java.io.IOException;

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
