package by.grsu.mcreader.mcrimageloader.imageloader;


import android.app.ActivityManager;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.support.v4.util.LruCache;
import android.text.TextUtils;
import android.util.Log;

import java.lang.ref.SoftReference;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import by.grsu.mcreader.mcrimageloader.imageloader.cache.LimitedDiscCache;
import by.grsu.mcreader.mcrimageloader.imageloader.drawable.RecyclingBitmapDrawable;
import by.grsu.mcreader.mcrimageloader.imageloader.utils.AndroidVersionsUtils;

public class CacheHelper {

    protected static final String LOG_TAG = CacheHelper.class.getSimpleName();

    private int mMemoryCacheSize = 2 * 1024 * 1024;// 2MB
    private int mDiscCacheSize = 10 * 1024 * 1024; // 10MB

    private LruCache<String, BitmapDrawable> mStorage;

    private final Set<SoftReference<Bitmap>> mReusableBitmaps;

    private LimitedDiscCache mDiscCache;

    protected CacheHelper(Context context, boolean memoryCache, boolean diskCache) {

        final boolean hasHoneycomb = AndroidVersionsUtils.hasHoneycomb();

        mReusableBitmaps = hasHoneycomb ? Collections.synchronizedSet(new HashSet<SoftReference<Bitmap>>()) : null;

        mStorage = memoryCache ? new LruCache<String, BitmapDrawable>(mMemoryCacheSize) {

            @Override
            protected int sizeOf(String key, BitmapDrawable value) {

                return value.getBitmap().getRowBytes() * value.getBitmap().getHeight();

            }

            @Override
            protected void entryRemoved(boolean evicted, String key, BitmapDrawable oldValue, BitmapDrawable newValue) {

                if (RecyclingBitmapDrawable.class.isInstance(oldValue)) {

                    ((RecyclingBitmapDrawable) oldValue).setIsCached(false);

                }

                if (hasHoneycomb && mReusableBitmaps != null && mReusableBitmaps.size() < 50) {

                    mReusableBitmaps.add(new SoftReference<Bitmap>(oldValue.getBitmap()));

                }
            }
        } : null;

        mDiscCache = diskCache ? new LimitedDiscCache(context.getCacheDir(), mDiscCacheSize) : null;
    }

    public void setDiscCacheSize(int discCacheSizeInBytes) {

        mDiscCacheSize = discCacheSizeInBytes > 0 ? discCacheSizeInBytes : mDiscCacheSize;

    }

    public void setMemoryCacheSize(int memoryCacheSizeInBytes) {

        mMemoryCacheSize = memoryCacheSizeInBytes > 0 ? memoryCacheSizeInBytes : mMemoryCacheSize;

    }

    public void setPartOfAvailableMemoryCache(Context context, float part) {

        mMemoryCacheSize = part > 0f && part <= 1f ? Math.round(1024 * 1024 * part * ((ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE)).getMemoryClass()) : mMemoryCacheSize;

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

        if (mStorage != null) {

            putBitmapToMemoryCache(url, value);

        }

        if (mDiscCache != null) {

            putBitmapToFileCache(url, value);

        }
    }

    protected void putBitmapToMemoryCache(String key, BitmapDrawable value) {

        if (mStorage == null) {

            return;

        }

        if (TextUtils.isEmpty(key) || value == null) {

            Log.w(LOG_TAG, "Cant't put bitmap to memory cache. Illegal arguments!");

            return;
        }

        if (getBitmapFromMemoryCache(key) == null) {

            if (RecyclingBitmapDrawable.class.isInstance(value)) {

                ((RecyclingBitmapDrawable) value).setIsCached(true);

            }

            mStorage.put(key, value);
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

                        if (canUseForInBitmap(item, options)) {

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

    protected static boolean canUseForInBitmap(Bitmap candidate, BitmapFactory.Options targetOptions) {

        if (targetOptions.inSampleSize <= 0) {

            return false;

        }

        int width = targetOptions.outWidth / targetOptions.inSampleSize, height = targetOptions.outHeight / targetOptions.inSampleSize;

        return candidate.getWidth() == width && candidate.getHeight() == height && candidate.getConfig() == targetOptions.inPreferredConfig;
    }
}
