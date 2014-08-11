package by.grsu.mcreader.mcrimageloader.imageloader;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
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

    private Bundle mParams;

    protected Bitmap getBitmap(String url, int width, int height) {

        BitmapFactory.Options options = new BitmapFactory.Options();

        ResultSource source = getSource(url, options);

        if (source == null) return null;

        options.inJustDecodeBounds = true;

        if (source instanceof FileInputStream) {

            return decodeFileInputStream((FileInputStream) source, width, height, options);

        } else if (source instanceof InputStream) {

            return decodeInputStream((InputStream) source, width, height, options);

        } else if (source instanceof File) {

            return decodeFile(((File) source), url, width, height, options);

        } else if (source instanceof FileDescriptor) {

            return decodeFileDescriptor(((FileDescriptor) source), width, height, options);

        } else {

            return decodeByteArray(((byte[]) source), url, width, height, options);

        }
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

            e.printStackTrace();
            // TODO
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

            e.printStackTrace();
            // TODO
        } finally {

            IOUtils.closeStream(source);

        }

        return result;
    }

    private Bitmap decodeFile(File source, String url, int width, int height, BitmapFactory.Options options) {
        return null;
    }

    private Bitmap decodeFileDescriptor(FileDescriptor source, int width, int height, BitmapFactory.Options options) {

        BitmapFactory.decodeFileDescriptor(source, null, options);

        options.inSampleSize = BitmapSizeUtil.calculateInSampleSize(options, width, height);

        options.inJustDecodeBounds = false;

        return BitmapFactory.decodeFileDescriptor(source, null, options);

    }

    private Bitmap decodeByteArray(byte[] source, String url, int width, int height, BitmapFactory.Options options) {

        BitmapFactory.decodeByteArray(source, 0, source.length, options);

        options.inJustDecodeBounds = false;

        return BitmapFactory.decodeByteArray(source, 0, source.length, options);
    }

    protected abstract ResultSource getSource(String url, BitmapFactory.Options options);

    public void setParams(Bundle params) {
        this.mParams = params;
    }

    public Bundle getParams() {
        return mParams;
    }

}
