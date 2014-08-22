package by.mcreader.imageloader;


import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.support.v4.util.LruCache;
import android.text.TextUtils;
import android.util.Log;

import java.io.File;

import by.mcreader.imageloader.cache.LimitedDiscCache;
import by.mcreader.imageloader.drawable.RecyclingBitmapDrawable;
import by.mcreader.imageloader.utils.AndroidVersions;
import by.mcreader.imageloader.utils.ReusableBitmapUtil;

public class ImageCacher {

    protected static final String LOG_TAG = ImageCacher.class.getSimpleName();

    private LruCache<String, BitmapDrawable> mStorage;

//    private final Set<SoftReference<Bitmap>> mReusableBitmaps;

    private LimitedDiscCache mDiscCache;

    protected ImageCacher(File cacheDir, boolean memoryCache, boolean diskCache, int memoryCacheSize, int discCacheSize) {

//        mReusableBitmaps = AndroidVersions.hasHoneycomb() ? Collections.synchronizedSet(new HashSet<SoftReference<Bitmap>>()) : null;

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

                return value == null ? 0 : ReusableBitmapUtil.byteSizeOf(value.getBitmap());

            }

            @Override
            protected void entryRemoved(boolean evicted, String key, BitmapDrawable
                    oldValue, BitmapDrawable newValue) {

                if (RecyclingBitmapDrawable.class.isInstance(oldValue)) {

                    ((RecyclingBitmapDrawable) oldValue).setIsCached(false);

                }
//                else {
//
//                    if (mReusableBitmaps != null) {
//
//                        mReusableBitmaps.add(new SoftReference<Bitmap>(oldValue.getBitmap()));
//
//                    }
//                }
            }
        } : null;

        mDiscCache = diskCache ? new LimitedDiscCache(cacheDir, discCacheSize) : null;
    }

    protected BitmapDrawable getBitmapFromFileCache(Resources resources, String url) {

        if (mDiscCache == null) return null;

        Bitmap result = mDiscCache.get(url);

        BitmapDrawable drawable = result == null ? null : AndroidVersions.hasHoneycomb() ? new BitmapDrawable(resources, result) : new RecyclingBitmapDrawable(resources, result);

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

//    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
//    void addInBitmapOptions(BitmapFactory.Options options) {
//
//        options.inMutable = true;
//
//        Bitmap inBitmap = getBitmapFromReusableSet(options);
//
//        if (inBitmap != null) {
//
//            Log.d(LOG_TAG, "Found bitmap to use for inBitmap");
//
//            options.inBitmap = inBitmap;
//        }
//    }
//
//    protected Bitmap getBitmapFromReusableSet(BitmapFactory.Options options) {
//        Bitmap bitmap = null;
//
//        if (mReusableBitmaps != null && !mReusableBitmaps.isEmpty()) {
//            synchronized (mReusableBitmaps) {
//                final Iterator<SoftReference<Bitmap>> iterator
//                        = mReusableBitmaps.iterator();
//                Bitmap item;
//
//                while (iterator.hasNext()) {
//                    item = iterator.next().get();
//
//                    if (null != item && item.isMutable()) {
//                        // Check to see it the item can be used for inBitmap.
//                        if (ReusableBitmapUtil.canUseForInBitmap(item, options)) {
//                            bitmap = item;
//
//                            // Remove from reusable set so it can't be used again.
//                            iterator.remove();
//                            break;
//                        }
//                    } else {
//                        // Remove from the set if the reference has been cleared.
//                        iterator.remove();
//                    }
//                }
//            }
//        }
//        return bitmap;
//    }
}
