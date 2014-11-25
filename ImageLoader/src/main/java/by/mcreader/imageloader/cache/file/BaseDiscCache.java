package by.mcreader.imageloader.cache.file;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import by.mcreader.imageloader.utils.Converter;
import by.mcreader.imageloader.utils.IOUtils;

public abstract class BaseDiscCache {

    private static final String TAG = BaseDiscCache.class.getSimpleName();

    private static final CompressFormat DEFAULT_COMPRESS_FORMAT = CompressFormat.JPEG;
    private static final int DEFAULT_COMPRESS_QUALITY = 70;

    protected File mCacheDir;

    public BaseDiscCache(Context context) {
        this(context == null ? null : context.getCacheDir());
    }

    public BaseDiscCache(File cacheDir) {
        if (cacheDir == null)
            throw new IllegalArgumentException(BaseDiscCache.class.getName() + ": can't get cache directory!");

        mCacheDir = cacheDir;
    }

    public File put(String name, Bitmap value) {
        String key = Converter.stringToMD5(name);

        FileOutputStream fos = null;

        File dir = new File(mCacheDir, key);

        try {
            fos = new FileOutputStream(dir);

            value.compress(DEFAULT_COMPRESS_FORMAT, DEFAULT_COMPRESS_QUALITY, fos);

            dir.setLastModified(System.currentTimeMillis());
        } catch (IOException e) {
            Log.e(TAG, "putBitmapToCache - " + e);
        } finally {
            IOUtils.closeStream(fos);
        }

        return dir;
    }

    public abstract Bitmap get(String name);
}
