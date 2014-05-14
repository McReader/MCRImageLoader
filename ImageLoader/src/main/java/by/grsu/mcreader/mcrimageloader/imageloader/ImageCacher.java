package by.grsu.mcreader.mcrimageloader.imageloader;

import image.cache.disc.LimitedDiscCache;
import image.drawable.RecyclingBitmapDrawable;

import java.io.File;
import java.lang.ref.SoftReference;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import utils.AndroidVersionsUtils;
import utils.L;
import android.app.ActivityManager;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.support.v4.util.LruCache;
import android.text.TextUtils;

public class ImageCacher {

    protected static final String LOG_TAG = ImageCacher.class.getSimpleName();

    public static final String SYSTEM_SERVICE_KEY = "framework:imagecacher";

    private static final boolean DEFAULT_MEM_CACHE_ENABLED = true;
    private static final boolean DEFAULT_DISK_CACHE_ENABLED = false;

    private boolean mIsMemoryCacheEnabled = DEFAULT_MEM_CACHE_ENABLED;
    private boolean mIsDiscCacheEnabled = DEFAULT_DISK_CACHE_ENABLED;

    private static final int DEFAULT_MEMORY_CACHE_LIMIT = 2 * 1024 * 1024;// 2MB
    private int mMemoryCacheSize = DEFAULT_MEMORY_CACHE_LIMIT;

    private static final int DEFAULT_DISC_CACHE_LIMIT = 10 * 1024 * 1024; // 10MB
    private int mDiscCacheSize = DEFAULT_DISC_CACHE_LIMIT;

    private LruCache<String, BitmapDrawable> mStorage;

    private Set<SoftReference<Bitmap>> mReusableBitmaps;

    private Resources mResources;
    private File mCacheDir;

    private LimitedDiscCache mDiscCache;

    private final Context mContext;

    protected ImageCacher(Context context) {
        mContext = context;
        mResources = context.getResources();
        init(context);
    }

    private void init(Context context) {
        mCacheDir = context.getCacheDir();
        if (AndroidVersionsUtils.hasHoneycomb()) {
            mReusableBitmaps = Collections
                    .synchronizedSet(new HashSet<SoftReference<Bitmap>>());
        }
        mStorage = new LruCache<String, BitmapDrawable>(mMemoryCacheSize) {
            @Override
            protected int sizeOf(String key, BitmapDrawable value) {
                return value.getBitmap().getRowBytes()
                        * value.getBitmap().getHeight();
            }

            @Override
            protected void entryRemoved(boolean evicted, String key,
                                        BitmapDrawable oldValue, BitmapDrawable newValue) {
                if (RecyclingBitmapDrawable.class.isInstance(oldValue)) {
                    ((RecyclingBitmapDrawable) oldValue).setIsCached(false);
                }
                if (AndroidVersionsUtils.hasHoneycomb()) {
                    if (mReusableBitmaps.size() < 50) {
                        mReusableBitmaps.add(new SoftReference<Bitmap>(oldValue
                                .getBitmap()));
                    }
                }
            }
        };
        mDiscCache = new LimitedDiscCache(mCacheDir, mDiscCacheSize);
    }

    public void setDiscCacheEnabled(boolean isEnabled) {
        this.mIsDiscCacheEnabled = isEnabled;
    }

    public void setMemoryCacheEnabled(boolean isEnabled) {
        this.mIsMemoryCacheEnabled = isEnabled;
    }

    public void setDiscCacheSize(int discCacheSizeInBytes) {
        this.mDiscCacheSize = discCacheSizeInBytes;
    }

    public void setMemoryCacheSize(int memoryCacheSizeInBytes) {
        this.mMemoryCacheSize = memoryCacheSizeInBytes;
    }

    public void setPartOfAvailableMemoryCache(float part) {
        if (part > 0f && part <= 1f) {
            int memClass = ((ActivityManager) mContext
                    .getSystemService(Context.ACTIVITY_SERVICE))
                    .getMemoryClass();
            mMemoryCacheSize = (int) (1024 * 1024 * memClass * part);
        }
    }

    protected BitmapDrawable getBitmapFromFileCache(String url) {
        Bitmap result = mDiscCache.get(url);
        BitmapDrawable drawable = null;
        if (result != null) {
            if (AndroidVersionsUtils.hasHoneycomb()) {
                drawable = new BitmapDrawable(mResources, result);
            } else {
                drawable = new RecyclingBitmapDrawable(mResources, result);
            }
        }
        if (mIsMemoryCacheEnabled) {
            putBitmapToMemoryCache(url, drawable);
        }
        return drawable;
    }

    protected void putBitmapToFileCache(String key, BitmapDrawable value) {
        if (key == null || TextUtils.isEmpty(key) || value == null) {
            L.w(LOG_TAG, "Cant't put bitmap to file cache. Illegal arguments!");
            return;
        }
        mDiscCache.put(key, value.getBitmap());
    }

    protected BitmapDrawable getBitmapFromMemoryCache(String key) {
        if (key == null || TextUtils.isEmpty(key)) {
            return null;
        }
        if (mStorage != null) {
            return mStorage.get(key);
        } else {
            throw new NullPointerException("LruCache object is null!!");
        }
    }

    protected void put(String url, BitmapDrawable value) {
        if (mIsMemoryCacheEnabled) {
            putBitmapToMemoryCache(url, value);
        }
        if (mIsDiscCacheEnabled) {
            putBitmapToFileCache(url, value);
        }
    }

    protected void putBitmapToMemoryCache(String key, BitmapDrawable value) {
        if (key == null || TextUtils.isEmpty(key) || value == null) {
            L.w(LOG_TAG,
                    "Cant't put bitmap to memory cache. Illegal arguments!");
            return;
        }
        if (mStorage != null && getBitmapFromMemoryCache(key) == null) {
            if (RecyclingBitmapDrawable.class.isInstance(value)) {
                ((RecyclingBitmapDrawable) value).setIsCached(true);
            }
            mStorage.put(key, value);
        }
    }

    protected Bitmap getBitmapFromReusableSet(BitmapFactory.Options options) {
        Bitmap bitmap = null;
        if (mReusableBitmaps != null && !mReusableBitmaps.isEmpty()) {
            synchronized (mReusableBitmaps) {
                final Iterator<SoftReference<Bitmap>> iterator = mReusableBitmaps
                        .iterator();
                Bitmap item = null;
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

    protected static boolean canUseForInBitmap(Bitmap candidate,
                                               BitmapFactory.Options targetOptions) {
        if (targetOptions.inSampleSize <= 0) {
            return false;
        }
        int width = targetOptions.outWidth / targetOptions.inSampleSize;
        int height = targetOptions.outHeight / targetOptions.inSampleSize;
        return candidate.getWidth() == width && candidate.getHeight() == height
                && candidate.getConfig() == targetOptions.inPreferredConfig;
    }

}
