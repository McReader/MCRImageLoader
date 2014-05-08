package by.grsu.mcreader.mcrimageloader.imageloader.cache;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import by.grsu.mcreader.mcrimageloader.imageloader.utils.Converter;
import by.grsu.mcreader.mcrimageloader.imageloader.utils.IOUtils;

public abstract class BaseDiscCache {

    private static final String LOG_TAG = BaseDiscCache.class.getSimpleName();

    private static final CompressFormat DEFAULT_COMPRESS_FORMAT = CompressFormat.JPEG;
    private static final int DEFAULT_COMPRESS_QUALITY = 70;

    protected File mCacheDir;

    public BaseDiscCache(Context context) {

        this(context == null ? null : context.getCacheDir());

    }

    public BaseDiscCache(File cacheDir) {

        if (cacheDir == null) {

            throw new IllegalArgumentException(BaseDiscCache.class.getName() + ": can't get cache directory!");

        }

        mCacheDir = cacheDir;
    }

    //TODO: IOUtils.getFileFromDir
//    public File getFile(String name) {
//
//        String key = Converter.stringToMD5(name);
//
//        File cacheFile = new File(mCacheDir, key);
//
//        return cacheFile;
//    }

    public File put(String name, Bitmap value) {

        String key = Converter.stringToMD5(name);

        FileOutputStream fos = null;

        File cacheFile = new File(mCacheDir, key);

        try {

            fos = new FileOutputStream(cacheFile);

            value.compress(DEFAULT_COMPRESS_FORMAT, DEFAULT_COMPRESS_QUALITY,
                    fos);

            cacheFile.setLastModified(System.currentTimeMillis());

        } catch (IOException e) {

            Log.e(LOG_TAG, "putBitmapToCache - " + e);

        } finally {

            IOUtils.closeStream(fos);

        }

        return cacheFile;
    }

    //TODO: IOUtils.clear
//    public void clear() {
//
//        File[] files = mCacheDir.listFiles();
//
//        for (File file : files) {
//
//            if (!file.delete()) {
//
//                Log.d(LOG_TAG, "failed to delete");
//
//            }
//        }
//    }

    public abstract Bitmap get(String name);
}
