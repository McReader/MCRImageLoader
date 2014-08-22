package by.mcreader.imageloader;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import by.mcreader.imageloader.utils.AndroidVersions;
import by.mcreader.imageloader.utils.BitmapAnalizer;
import by.mcreader.imageloader.utils.IOUtils;

/**
 * Created by dzianis_roi on 21.07.2014.
 */
public abstract class BaseBitmapLoader<ResultSource> {

    private static final String TAG = BaseBitmapLoader.class.getSimpleName();

    public static final String IMAGE_WIDTH_EXTRA = "by.mcreader.imageloader.EXTRA_IMAGE_WIDTH";
    public static final String IMAGE_HEIGHT_EXTRA = "by.mcreader.imageloader.IMAGE_HEIGHT_EXTRA";

    public final static int DEFAULT_IMAGE_WIDTH = 300;
    public final static int DEFAULT_IMAGE_HEIGHT = 300;

//    private ImageCacher mImageCacher;

    protected Bitmap loadBitmap(String url, Bundle extra) {

        if (extra == null) throw new IllegalArgumentException("Illegal extra for download!!");

        int width = extra.getInt(IMAGE_WIDTH_EXTRA, DEFAULT_IMAGE_WIDTH);
        int height = extra.getInt(IMAGE_HEIGHT_EXTRA, DEFAULT_IMAGE_HEIGHT);

        BitmapFactory.Options options = new BitmapFactory.Options();

        ResultSource source = getSource(url, options, extra);

        if (source == null) return null;

        Bitmap result;

        options.inJustDecodeBounds = true;

        if (source instanceof FileInputStream)
            result = decodeFileInputStream((FileInputStream) source, width, height, options);

        else if (source instanceof InputStream)
            result = decodeInputStream((InputStream) source, width, height, options);

        else if (source instanceof File)
            result = decodeFile(url, width, height, options);

        else if (source instanceof FileDescriptor)
            result = decodeFileDescriptor(((FileDescriptor) source), width, height, options);

        else
            result = decodeByteArray(((byte[]) source), width, height, options);


        return onBitmapReady(url, result, extra);
    }

    private Bitmap decodeFileInputStream(FileInputStream source, int width, int height, BitmapFactory.Options options) {

        Bitmap result = null;

        try {

            FileDescriptor fd = source.getFD();

            return decodeFileDescriptor(fd, width, height, options);

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

            options.inSampleSize = BitmapAnalizer.calculateInSampleSize(options, width, height);

//            addInOptionsBitmap(options);

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

        options.inSampleSize = BitmapAnalizer.calculateInSampleSize(options, width, height);

//        addInOptionsBitmap(options);

        options.inJustDecodeBounds = false;

        return BitmapFactory.decodeFile(url, options);
    }

    private Bitmap decodeFileDescriptor(FileDescriptor source, int width, int height, BitmapFactory.Options options) {

        BitmapFactory.decodeFileDescriptor(source, null, options);

        options.inSampleSize = BitmapAnalizer.calculateInSampleSize(options, width, height);

//        addInOptionsBitmap(options);

        options.inJustDecodeBounds = false;

        return BitmapFactory.decodeFileDescriptor(source, null, options);

    }

    private Bitmap decodeByteArray(byte[] source, int width, int height, BitmapFactory.Options options) {

        BitmapFactory.decodeByteArray(source, 0, source.length, options);

        options.inSampleSize = BitmapAnalizer.calculateInSampleSize(options, width, height);

//        addInOptionsBitmap(options);

        options.inJustDecodeBounds = false;

        return BitmapFactory.decodeByteArray(source, 0, source.length, options);
    }

//    private void addInOptionsBitmap(BitmapFactory.Options options) {
//        if (AndroidVersions.hasHoneycomb()) mImageCacher.addInBitmapOptions(options);
//    }


    protected abstract ResultSource getSource(String url, BitmapFactory.Options options, Bundle extra);

    protected Bitmap onBitmapReady(String url, Bitmap result, Bundle extra) {
        return result;
    }

//    public void setImageCacher(ImageCacher imageCacher) {
//        this.mImageCacher = imageCacher;
//    }
}
