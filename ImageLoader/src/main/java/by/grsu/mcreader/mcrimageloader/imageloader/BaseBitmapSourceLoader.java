package by.grsu.mcreader.mcrimageloader.imageloader;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import by.grsu.mcreader.mcrimageloader.imageloader.utils.BitmapSizeUtil;
import by.grsu.mcreader.mcrimageloader.imageloader.utils.IOUtils;

/**
 * Created by dzianis_roi on 21.07.2014.
 */
public abstract class BaseBitmapSourceLoader<ResultSource> {

    private static final String TAG = BaseBitmapSourceLoader.class.getSimpleName();

    private Bundle mParams;

    protected Bitmap getBitmap(String url, int width, int height) {

        BitmapFactory.Options options = new BitmapFactory.Options();

        ResultSource source = getSource(url, options);

        if (source == null) return null;

        Bitmap result;

        int rotationDegree = getRotationDegree(url);

        options.inJustDecodeBounds = true;

        if (source instanceof FileInputStream) {

            result = decodeFileInputStream((FileInputStream) source, width, height, options);

        } else if (source instanceof InputStream) {

            result = decodeInputStream((InputStream) source, width, height, options);

        } else if (source instanceof File) {

            result = decodeFile(url, width, height, options);

        } else if (source instanceof FileDescriptor) {

            result = decodeFileDescriptor(((FileDescriptor) source), width, height, options);

        } else {

            result = decodeByteArray(((byte[]) source), width, height, options);

        }

        return rotationDegree != -1 ? rotate(result, rotationDegree) : result;
    }

    private Bitmap decodeFileInputStream(FileInputStream source, int width, int height, BitmapFactory.Options options) {

        Bitmap result = null;

        try {

            FileDescriptor fd = source.getFD();

            BitmapFactory.decodeFileDescriptor(fd, null, options);

            options.inSampleSize = BitmapSizeUtil.calculateInSampleSize(options, width, height);

            options.inJustDecodeBounds = false;

            result = BitmapFactory.decodeFileDescriptor(fd, null, options);

        } catch (IOException e) {

            Log.e(TAG, TextUtils.isEmpty(e.getMessage()) ? "Error decodeFileInputStream" : e.getMessage());

        } finally {

            IOUtils.closeStream(source);

        }

        return result;
    }

    private Bitmap decodeInputStream(InputStream source, int width, int height, BitmapFactory.Options options) {

        Bitmap result = null;

        try {

            source.mark(source.available());

            BitmapFactory.decodeStream(source, null, options);

            options.inSampleSize = BitmapSizeUtil.calculateInSampleSize(options, width, height);

            options.inJustDecodeBounds = false;

            source.reset();

            result = BitmapFactory.decodeStream(source, null, options);

        } catch (IOException e) {

            Log.e(TAG, TextUtils.isEmpty(e.getMessage()) ? "Error decodeInputStream" : e.getMessage());

        } finally {

            IOUtils.closeStream(source);

        }

        return result;
    }

    private Bitmap decodeFile(String url, int width, int height, BitmapFactory.Options options) {
        BitmapFactory.decodeFile(url, options);

        options.inSampleSize = BitmapSizeUtil.calculateInSampleSize(options, width, height);

        options.inJustDecodeBounds = false;

        return BitmapFactory.decodeFile(url, options);
    }

    private Bitmap decodeFileDescriptor(FileDescriptor source, int width, int height, BitmapFactory.Options options) {

        BitmapFactory.decodeFileDescriptor(source, null, options);

        options.inSampleSize = BitmapSizeUtil.calculateInSampleSize(options, width, height);

        options.inJustDecodeBounds = false;

        return BitmapFactory.decodeFileDescriptor(source, null, options);

    }

    private Bitmap decodeByteArray(byte[] source, int width, int height, BitmapFactory.Options options) {

        BitmapFactory.decodeByteArray(source, 0, source.length, options);

        options.inSampleSize = BitmapSizeUtil.calculateInSampleSize(options, width, height);

        options.inJustDecodeBounds = false;

        return BitmapFactory.decodeByteArray(source, 0, source.length, options);
    }

    protected int defineRotationDegree(String path) {

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

    private Bitmap rotate(Bitmap target, Integer degree) {

        if (degree != null) {

            Matrix mtx = new Matrix();

            mtx.postRotate(degree);

            return Bitmap.createBitmap(target, 0, 0, target.getWidth(), target.getHeight(), mtx, true);
        }

        return target;
    }

    protected abstract ResultSource getSource(String url, BitmapFactory.Options options);

    protected int getRotationDegree(String url) {
        return -1;
    }

    public void setParams(Bundle params) {
        this.mParams = params;
    }

    public Bundle getParams() {
        return mParams;
    }

}
