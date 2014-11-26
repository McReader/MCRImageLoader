package by.mcreader.imageloader.cache.file;

import android.content.Context;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.util.Log;

/**
 * Created by Dzianis_Roi on 21.11.2014.
 */
public class DefaultFileCache implements FileCache {

    private static final String TAG = DefaultFileCache.class.getSimpleName();
    private static final String ID = "cache.file.DefaultFileCache";

    private LimitedDiscCache mDiscCache;

    public DefaultFileCache(Context context, int size) {
        mDiscCache = new LimitedDiscCache(context.getCacheDir(), size);
    }

    @Override
    public Bitmap get(String key) {
        return mDiscCache.get(key);
    }

    @Override
    public void put(String key, Bitmap value) {
        if (TextUtils.isEmpty(key) || value == null) {
            Log.w(TAG, "Can't put bitmap to file cache. Illegal arguments!");

            return;
        }

        mDiscCache.put(key, value);
    }

    public String id() {
        return null;
    }
}
