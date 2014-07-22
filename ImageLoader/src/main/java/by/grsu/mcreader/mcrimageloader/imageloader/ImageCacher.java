package by.grsu.mcreader.mcrimageloader.imageloader;


import android.annotation.TargetApi;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.support.v4.util.LruCache;
import android.text.TextUtils;
import android.util.Log;

import java.io.File;
import java.lang.ref.SoftReference;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import by.grsu.mcreader.mcrimageloader.imageloader.cache.LimitedDiscCache;
import by.grsu.mcreader.mcrimageloader.imageloader.drawable.RecyclingBitmapDrawable;
import by.grsu.mcreader.mcrimageloader.imageloader.utils.AndroidVersionsUtils;
import by.grsu.mcreader.mcrimageloader.imageloader.utils.ReusableBitmapUtil;

public class ImageCacher {

    protected static final String LOG_TAG = ImageCacher.class.getSimpleName();

    private LruCache<String, BitmapDrawable> mStorage;

    private final Set<SoftReference<Bitmap>> mReusableBitmaps;

    private LimitedDiscCache mDiscCache;

    protected ImageCacher(File cacheDir, boolean memoryCache, boolean diskCache, int memoryCacheSize, int discCacheSize) {

        mReusableBitmaps = AndroidVersionsUtils.hasHoneycomb() ? Collections.synchronizedSet(new HashSet<SoftReference<Bitmap>>()) : null;

        init(
                cacheDir,
                memoryCache,
                diskCache,
                memoryCacheSize > 1 * 1024 * 1024 ? memoryCacheSize : 5 * 1024 * 1024, // if > 1mb then apply non-default memory cache size
                discCacheSize > 1 * 1024 * 1024 ? discCacheSize : 5 * 1024 * 1024); //if > 1mb then apply non-default disk cache size
    }

    private void init(File cacheDir, boolean memoryCache, boolean diskCache, int memoryCacheSize, int discCacheSize) {

        mStorage = memoryCache ? new LruCache<String, BitmapDrawable>(memoryCacheSize) {

            @Override
            protected int sizeOf(String key, BitmapDrawable value) {

                return value.getBitmap().getRowBytes() * value.getBitmap().getHeight();

            }

            @Override
            protected void entryRemoved(boolean evicted, String key, BitmapDrawable oldValue, BitmapDrawable newValue) {

                if (RecyclingBitmapDrawable.class.isInstance(oldValue)) {

                    ((RecyclingBitmapDrawable) oldValue).setIsCached(false);

                }

                if (mReusableBitmaps != null) {

                    mReusableBitmaps.add(new SoftReference<Bitmap>(oldValue.getBitmap()));

                }

            }
        } : null;

        mDiscCache = diskCache ? new LimitedDiscCache(cacheDir, discCacheSize) : null;
    }

    protected BitmapDrawable getBitmapFromFileCache(Resources resources, String url) {

        if (mDiscCache == null) return null;

        Bitmap result = mDiscCache.get(url);

        BitmapDrawable drawable = result == null ? null : AndroidVersionsUtils.hasHoneycomb() ? new BitmapDrawable(resources, result) : new RecyclingBitmapDrawable(resources, result);

        if (mStorage != null) {

            putBitmapToMemoryCache(url, drawable);

        }

        return drawable;
    }

    protected void putBitmapToFileCache(String key, BitmapDrawable value) {

        if (TextUtils.isEmpty(key) || value == null) {

            Log.w(LOG_TAG, "Can't put bitmap to file cache. Illegal arguments!");

            return;
        }

        mDiscCache.put(key, value.getBitmap());
    }

    protected BitmapDrawable getBitmapFromMemoryCache(String key) {

        return TextUtils.isEmpty(key) || mStorage == null ? null : mStorage.get(key);

    }

    protected void put(String url, BitmapDrawable value) {

        if (mStorage != null) putBitmapToMemoryCache(url, value);

        if (mDiscCache != null) putBitmapToFileCache(url, value);

    }

    protected void putBitmapToMemoryCache(String key, BitmapDrawable value) {

        if (mStorage == null) return;

        if (TextUtils.isEmpty(key) || value == null) {

            Log.w(LOG_TAG, "Can't put bitmap to memory cache. Illegal arguments!");

            return;
        }

        if (getBitmapFromMemoryCache(key) == null) {

            if (RecyclingBitmapDrawable.class.isInstance(value)) {

                ((RecyclingBitmapDrawable) value).setIsCached(true);

            }

            mStorage.put(key, value);
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    void addInBitmapOptions(BitmapFactory.Options options) {

        options.inMutable = true;

        Bitmap inBitmap = getBitmapFromReusableSet(options);

        if (inBitmap != null) {

            Log.d(LOG_TAG, "Found bitmap to use for inBitmap");

            options.inBitmap = inBitmap;
        }
    }

    protected Bitmap getBitmapFromReusableSet(BitmapFactory.Options options) {

        Bitmap bitmap = null;

        if (mReusableBitmaps != null) {

            synchronized (mReusableBitmaps) {

                final Iterator<SoftReference<Bitmap>> iterator = mReusableBitmaps.iterator();

                Bitmap item;

                while (iterator.hasNext()) {

                    item = iterator.next().get();

                    if (null != item && item.isMutable()) {

                        if (ReusableBitmapUtil.canUseForInBitmap(item, options)) {

                            bitmap = item;

                            iterator.remove();

                            break;
                        }

                    } else {

                        iterator.remove();

                    }
                }
            }
        }

        return bitmap;
    }
}
