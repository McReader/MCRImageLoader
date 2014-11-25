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

import by.mcreader.imageloader.request.ParamsProcessor;
import by.mcreader.imageloader.utils.BitmapUtil;
import by.mcreader.imageloader.utils.IOUtils;

/**
 * Created by dzianis_roi on 21.07.2014.
 */
public abstract class BaseBitmapLoader<ResultSource> implements Service {

    private static final String TAG = BaseBitmapLoader.class.getSimpleName();

    protected Bitmap loadBitmap(Bundle params) {

        if (params == null) throw new IllegalArgumentException("Illegal extra for download!!");

        BitmapFactory.Options options = new BitmapFactory.Options();

        ResultSource source = getSource(params, options);

        if (source == null) return null;

        Bitmap result;

        options.inJustDecodeBounds = true;

        if (source instanceof FileInputStream)
            result = decodeFileInputStream((FileInputStream) source, params, options);

        else if (source instanceof InputStream)
            result = decodeInputStream((InputStream) source, params, options);

        else if (source instanceof File)
            result = decodeFile(params, options);

        else if (source instanceof FileDescriptor)
            result = decodeFileDescriptor(((FileDescriptor) source), params, options);

        else
            result = decodeByteArray(((byte[]) source), params, options);

        return onBitmapReady(params, result);
    }

    private Bitmap decodeFileInputStream(FileInputStream source, Bundle params, BitmapFactory.Options options) {

        try {

            return decodeFileDescriptor(source.getFD(), params, options);

        } catch (IOException e) {

            Log.e(TAG, TextUtils.isEmpty(e.getMessage()) ? "Error decodeFileInputStream" : e.getMessage());

        } finally {

            IOUtils.closeStream(source);

        }

        return null;
    }

    private Bitmap decodeInputStream(InputStream source, Bundle params, BitmapFactory.Options options) {

        Bitmap result = null;

        ParamsProcessor pp = new ParamsProcessor(params);

        try {

            source.mark(source.available());

            BitmapFactory.decodeStream(source, null, options);

            options.inSampleSize = BitmapUtil.calculateInSampleSize(options, pp.size());

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

    private Bitmap decodeFile(Bundle params, BitmapFactory.Options options) {
        ParamsProcessor pp = new ParamsProcessor(params);

        BitmapFactory.decodeFile(pp.url(), options);

        options.inSampleSize = BitmapUtil.calculateInSampleSize(options, pp.size());

//        addInOptionsBitmap(options);

        options.inJustDecodeBounds = false;

        return BitmapFactory.decodeFile(pp.url(), options);
    }

    private Bitmap decodeFileDescriptor(FileDescriptor source, Bundle params, BitmapFactory.Options options) {
        ParamsProcessor pp = new ParamsProcessor(params);

        BitmapFactory.decodeFileDescriptor(source, null, options);

        options.inSampleSize = BitmapUtil.calculateInSampleSize(options, pp.size());

//        addInOptionsBitmap(options);

        options.inJustDecodeBounds = false;

        return BitmapFactory.decodeFileDescriptor(source, null, options);

    }

    private Bitmap decodeByteArray(byte[] source, Bundle params, BitmapFactory.Options options) {

        ParamsProcessor pp = new ParamsProcessor(params);

        BitmapFactory.decodeByteArray(source, 0, source.length, options);

        options.inSampleSize = BitmapUtil.calculateInSampleSize(options, pp.size());

//        addInOptionsBitmap(options);

        options.inJustDecodeBounds = false;

        return BitmapFactory.decodeByteArray(source, 0, source.length, options);
    }

//    private void addInOptionsBitmap(BitmapFactory.Options options) {
//        if (AndroidVersions.hasHoneycomb()) cache.addInBitmapOptions(options);
//    }

    protected abstract ResultSource getSource(Bundle params, BitmapFactory.Options options);

    protected Bitmap onBitmapReady(Bundle params, Bitmap result) {
        return result;
    }
}