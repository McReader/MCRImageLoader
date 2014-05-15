package by.grsu.mcreader.mcrimageloader.imageloader;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.text.TextUtils;
import android.util.Log;
import android.widget.ImageView;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.concurrent.ExecutionException;

import by.grsu.mcreader.mcrimageloader.imageloader.callback.ImageLoaderCallback;
import by.grsu.mcreader.mcrimageloader.imageloader.drawable.AsyncBitmapDrawable;
import by.grsu.mcreader.mcrimageloader.imageloader.drawable.RecyclingBitmapDrawable;
import by.grsu.mcreader.mcrimageloader.imageloader.utils.AndroidVersionsUtils;
import by.grsu.mcreader.mcrimageloader.imageloader.utils.BitmapSizeUtil;
import by.grsu.mcreader.mcrimageloader.imageloader.utils.Converter;
import by.grsu.mcreader.mcrimageloader.imageloader.utils.NetworkHelper;

public class SuperImageLoader {

    private static final String LOG_TAG = SuperImageLoader.class.getSimpleName();

    private boolean mFadeIn = true;
    private int mFadeInTime = 600; // Default fade in time

    private final static int DEFAULT_IMAGE_WIDTH = 300;
    private final static int DEFAULT_IMAGE_HEIGHT = 300;

    private final static ColorDrawable TRANSPARENT_DRAWABLE = new ColorDrawable(android.R.color.transparent);

    private Bitmap mPlaceholderBitmap;

    private final Context mContext;
    private final Resources mResources;

    private final CacheHelper mCacheHelper;
    private final ImageWorker mImageWorker;

    private final Object mPauseWorkLock = new Object();
    private boolean mPauseWork = false;

    private SuperImageLoader(ImageLoaderBuilder builder) {

        mContext = builder.mContext;
        mResources = builder.mResources;
        mPlaceholderBitmap = builder.mPlaceHolderImage;
        mFadeIn = builder.mFadeIn;
        mFadeInTime = builder.mFadeInTime;

        mCacheHelper = new CacheHelper(mContext, builder.mMemoryCacheEnabled, builder.mDiscCacheEnabled);
        mImageWorker = new ImageWorker(mCacheHelper);

        mCacheHelper.setDiscCacheSize(builder.mDiscCacheSize);
        mCacheHelper.setMemoryCacheSize(builder.mMemoryCacheSize);

    }

    public void setPlaceholder(int resDrawableID) {
        setPlaceholder(BitmapFactory.decodeResource(mResources, resDrawableID));
    }

    public void setPlaceholder(Bitmap bitmap) {
        mPlaceholderBitmap = bitmap;
    }

    public void setFadeIn(boolean fadeIn) {
        mFadeIn = fadeIn;
    }

    public void setFadeInTime(int time) {
        mFadeInTime = time;
    }

    public BitmapDrawable loadBitmapSync(String url, int widthInPx, int heightInPx) {

        if (TextUtils.isEmpty(url)) {

            Log.e(LOG_TAG, "Empty url!");

            return null;
        }

        boolean careAboutSize = widthInPx > 0 || heightInPx > 0;

        widthInPx = widthInPx <= 0 ? DEFAULT_IMAGE_WIDTH : widthInPx;
        heightInPx = heightInPx <= 0 ? DEFAULT_IMAGE_HEIGHT : heightInPx;

        BitmapDrawable bitmapDrawable = null;

        if (mCacheHelper != null) {

            bitmapDrawable = mCacheHelper.getBitmapFromMemoryCache(url);

            if (bitmapDrawable != null) {

                if (careAboutSize) {

                    if (BitmapSizeUtil.inspectDimensions(bitmapDrawable, widthInPx, heightInPx)) {

                        return bitmapDrawable;

                    }

                } else {

                    return bitmapDrawable;

                }
            }

            bitmapDrawable = mCacheHelper.getBitmapFromFileCache(mContext.getResources(), url);
        }

        if (bitmapDrawable != null) {

            if (careAboutSize) {

                if (BitmapSizeUtil.inspectDimensions(bitmapDrawable, widthInPx, heightInPx)) {

                    return bitmapDrawable;

                }

            } else {

                return bitmapDrawable;

            }
        }

        Bitmap bitmap = null;

        try {

            if (NetworkHelper.checkConnection(mContext)) {

                bitmap = mImageWorker.loadBitmap(url, widthInPx, heightInPx);

            }

        } catch (IOException e) {

            e.printStackTrace();

        }

        if (bitmap != null) {

            if (AndroidVersionsUtils.hasHoneycomb() && bitmap.getConfig() != null) {

                bitmapDrawable = new BitmapDrawable(mResources, bitmap);

            } else {

                bitmapDrawable = new RecyclingBitmapDrawable(mResources, bitmap);

            }

            mCacheHelper.put(url, bitmapDrawable);
        }

        return bitmapDrawable;
    }

    public BitmapDrawable loadBitmapSync(String url) {
        return loadBitmapSync(url, 0, 0);
    }

    public Bitmap loadBitmap(String url, int widthInPx, int heightInPx) {

        BitmapDrawable bitmapDrawable = mCacheHelper == null ? null : mCacheHelper.getBitmapFromMemoryCache(url);

        Bitmap result = bitmapDrawable == null ? null : bitmapDrawable.getBitmap();

        boolean careAboutSize = widthInPx > 0 || heightInPx > 0;

        BitmapAsyncTask bitmapAsyncTask = careAboutSize ? new BitmapAsyncTask(widthInPx, heightInPx) : new BitmapAsyncTask();

        if (result == null) {

            bitmapAsyncTask.start(url);

            try {

                result = bitmapAsyncTask.get();

            } catch (InterruptedException e) {

                Log.e(LOG_TAG, "Can't load bitmap. Troubles with asynctask execution!");

            } catch (ExecutionException e) {

                Log.e(LOG_TAG, "Can't load bitmap. Troubles with asynctask execution!");

            }

        } else {

            if (careAboutSize) {

                if (!BitmapSizeUtil.inspectDimensions(result, widthInPx, heightInPx)) {

                    result.recycle();
                    result = null;

                    bitmapAsyncTask = new BitmapAsyncTask(widthInPx, heightInPx);

                    try {

                        result = bitmapAsyncTask.get();

                    } catch (InterruptedException e) {

                        Log.e(LOG_TAG, "Can't load bitmap. Troubles with asynctask execution!");

                    } catch (ExecutionException e) {

                        Log.e(LOG_TAG, "Can't load bitmap. Troubles with asynctask execution!");

                    }
                }
            }
        }

        return result;
    }

    public Bitmap loadBitmap(Context context, String url, float widthInDp, float heightInDp) {

        return loadBitmap(url, Math.round(Converter.convertDpToPixel(context, widthInDp)), Math.round(Converter.convertDpToPixel(context, heightInDp)));

    }

    public Bitmap loadBitmap(String url) {
        return loadBitmap(url, 0, 0);
    }

    public void loadBitmap(ImageView imageView, String url, int widthInPx, int heightInPx) {

        if (imageView == null) {

            Log.e(LOG_TAG, "ImageView object is null");

            return;

        }

        BitmapDrawable bitmapDrawable = TextUtils.isEmpty(url) ? new BitmapDrawable(mResources, mPlaceholderBitmap) : mCacheHelper == null ? null : mCacheHelper.getBitmapFromMemoryCache(url);

        if (bitmapDrawable != null) {

            imageView.setImageDrawable(bitmapDrawable);

        } else if (cancelPotentialDownload(imageView, url)) {

            ImageAsyncTask bitmapAsyncTask = new ImageAsyncTask(imageView);

            AsyncBitmapDrawable asyncbitmapDrawable = new AsyncBitmapDrawable(mResources, mPlaceholderBitmap, bitmapAsyncTask);

            imageView.setImageDrawable(asyncbitmapDrawable);

            if (widthInPx > 0 || heightInPx > 0) {

                bitmapAsyncTask.start(url, widthInPx, heightInPx);

            } else {

                bitmapAsyncTask.start(url);

            }
        }
    }

    public void loadBitmap(Context context, ImageView imageView, String url, float widthInDp, float heightInDp) {

        loadBitmap(imageView, url, Math.round(Converter.convertDpToPixel(context, widthInDp)), Math.round(Converter.convertDpToPixel(context, heightInDp)));

    }

    public void loadBitmap(ImageView imageView, String url) {
        loadBitmap(imageView, url, 0, 0);
    }

    public void loadBitmap(ImageView imageView, String url, int widthInPx, int heightInPx, ImageLoaderCallback callback) {

        callback.onLoadStarted();

        if (TextUtils.isEmpty(url)) {

            Log.e(LOG_TAG, "empty or null url");

            setImageDrawable(imageView, new BitmapDrawable(mResources, mPlaceholderBitmap));
            callback.onLoadError();

            return;
        }

        BitmapDrawable bitmapDrawable = mCacheHelper == null ? null : mCacheHelper.getBitmapFromMemoryCache(url);

        if (bitmapDrawable != null) {

            imageView.setImageDrawable(bitmapDrawable);

            callback.onLoadFinished();

        } else if (cancelPotentialDownload(imageView, url)) {

            ImageAsyncTask bitmapAsyncTask = new ImageAsyncTask(imageView, callback);

            AsyncBitmapDrawable asyncbitmapDrawable = new AsyncBitmapDrawable(mResources, mPlaceholderBitmap, bitmapAsyncTask);

            imageView.setImageDrawable(asyncbitmapDrawable);

            if (widthInPx > 0 && heightInPx > 0) {

                bitmapAsyncTask.start(url, widthInPx, heightInPx);

            } else {

                bitmapAsyncTask.start(url);

            }
        }
    }

    public void loadBitmap(Context context, ImageView imageView, String url, float widthInDp, float heightInDp, ImageLoaderCallback callback) {

        loadBitmap(imageView, url, Math.round(Converter.convertDpToPixel(context, widthInDp)), Math.round(Converter.convertDpToPixel(context, heightInDp)), callback);

    }

    public void loadBitmap(ImageView imageView, String url, ImageLoaderCallback callback) {

        loadBitmap(imageView, url, 0, 0, callback);

    }

    private static boolean cancelPotentialDownload(ImageView imageView, String url) {

        ImageAsyncTask bitmapAsyncTask = getImageLoaderTask(imageView);

        if (bitmapAsyncTask != null) {

            String bitmapUrl = bitmapAsyncTask.mUrl;

            if (bitmapUrl == null || !bitmapUrl.equals(url)) {

                bitmapAsyncTask.cancel(true);

                Log.d(LOG_TAG, "cancelPotentialDownload for " + url);

            } else {

                return false;

            }
        }

        return true;
    }

    private static ImageAsyncTask getImageLoaderTask(ImageView imageView) {

        final Drawable drawable = imageView == null ? null : imageView.getDrawable();

        return drawable instanceof AsyncBitmapDrawable ? ((AsyncBitmapDrawable) drawable).getLoaderTask() : null;

    }

    private void setImageDrawable(ImageView imageView, Drawable drawable) {

        final TransitionDrawable td = mFadeIn && drawable != null ? new TransitionDrawable(new Drawable[]{TRANSPARENT_DRAWABLE, drawable}) : null;

        if (td == null) {

            imageView.setImageDrawable(drawable);

        } else {

            setBackground(imageView);

            imageView.setImageDrawable(td);

            td.startTransition(mFadeInTime);

        }
    }

    @SuppressLint("NewApi")
    private void setBackground(ImageView imageView) {

        if (AndroidVersionsUtils.hasJellyBean()) {

            imageView.setBackground(new BitmapDrawable(mResources, mPlaceholderBitmap));

        } else {

            imageView.setBackgroundDrawable(new BitmapDrawable(mResources, mPlaceholderBitmap));

        }
    }

    /**
     * Pause any ongoing background work. This can be used as a temporary
     * measure to improve performance. For example background work could be
     * paused when a ListView or GridView is being scrolled using a
     * {@link android.widget.AbsListView.OnScrollListener} to keep scrolling
     * smooth.
     * <p/>
     * If work is paused, be sure setPauseWork(false) is called again before
     * your fragment or activity is destroyed (for example during
     * {@link android.app.Activity#onPause()}), or there is a risk the
     * background thread will never finish.
     */
    public void setPauseWork(boolean pauseWork) {

        synchronized (mPauseWorkLock) {

            mPauseWork = pauseWork;

            if (!mPauseWork) {

                mPauseWorkLock.notifyAll();

            }
        }
    }

    public class BitmapAsyncTask extends AsyncTask<String, Void, Bitmap> {

        private String mUrl;

        private int mWidth;
        private int mHeight;

        private boolean mCareAboutSize;

        public BitmapAsyncTask() {
            this(0, 0);
        }

        public BitmapAsyncTask(int widthInPx, int heightInPx) {

            mCareAboutSize = widthInPx > 0 || heightInPx > 0;

            mWidth = widthInPx > 0 ? widthInPx : DEFAULT_IMAGE_WIDTH;
            mHeight = heightInPx > 0 ? heightInPx : DEFAULT_IMAGE_HEIGHT;
        }

        public void start(String url) {

            mUrl = url;

            // custom version of AsyncTask
            executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, url);

        }

        @Override
        protected Bitmap doInBackground(String... params) {

            if (TextUtils.isEmpty(mUrl)) {

                Log.e(LOG_TAG, "Can't load bitmap! Url empty or null!");

                return null;
            }

            BitmapDrawable bitmapDrawable = mCacheHelper == null ? null : mCacheHelper.getBitmapFromFileCache(mContext.getResources(), mUrl);
            Bitmap result = bitmapDrawable == null ? null : bitmapDrawable.getBitmap();

            if (result == null) {

                result = loadFromNetwork();

            } else {

                if (mCareAboutSize && !BitmapSizeUtil.inspectDimensions(result, mWidth, mHeight)) {

                    result.recycle();

                    result = loadFromNetwork();

                }
            }

            return result;
        }

        private Bitmap loadFromNetwork() {

            try {

                return NetworkHelper.checkConnection(mContext) ? mImageWorker.loadBitmap(mUrl, mWidth, mHeight) : null;

            } catch (IOException e) {

                Log.e(LOG_TAG, e.getMessage());

            }

            return null;
        }
    }

    public class ImageAsyncTask extends AsyncTask<String, Void, BitmapDrawable> {

        protected String mUrl;

        private int mWidth;
        private int mHeight;

        private WeakReference<ImageView> mImageViewReference;

        private ImageLoaderCallback mCallback;

        public ImageAsyncTask(ImageView imageView) {
            this(imageView, null);
        }

        public ImageAsyncTask(ImageView imageView, ImageLoaderCallback callback) {

            mImageViewReference = new WeakReference<ImageView>(imageView);

            mCallback = callback;

        }

        public void start(String url) {

            this.start(url, DEFAULT_IMAGE_WIDTH, DEFAULT_IMAGE_HEIGHT);

        }

        public void start(String url, int width, int height) {

            mUrl = url;

            mWidth = width;
            mHeight = height;

            // custom version of AsyncTask
            executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, url);

        }

        @Override
        protected BitmapDrawable doInBackground(String... params) {

            if (TextUtils.isEmpty(mUrl)) {

                Log.e(LOG_TAG, "Empty url!");

                return null;

            }

            synchronized (mPauseWorkLock) {

                while (mPauseWork && !isCancelled()) {

                    try {

                        mPauseWorkLock.wait();

                    } catch (InterruptedException e) {
                        // can be ignored
                    }
                }

            }

            BitmapDrawable bitmapDrawable = mCacheHelper == null ? null : mCacheHelper.getBitmapFromFileCache(mContext.getResources(), mUrl);

            if (bitmapDrawable != null) {

                return bitmapDrawable;

            }

            Bitmap bitmap;

            try {

                bitmap = NetworkHelper.checkConnection(mContext) && !isCancelled() && getAttachedImageView() != null ? mImageWorker.loadBitmap(mUrl, mWidth, mHeight) : null;

                if (bitmap != null) {

                    if (AndroidVersionsUtils.hasHoneycomb() && bitmap.getConfig() != null) {

                        bitmapDrawable = new BitmapDrawable(mResources, bitmap);

                    } else {

                        bitmapDrawable = new RecyclingBitmapDrawable(mResources, bitmap);

                    }

                    mCacheHelper.put(mUrl, bitmapDrawable);
                }

            } catch (IOException e) {

                Log.e(LOG_TAG, e.getMessage());

            }

            return bitmapDrawable;
        }

        @Override
        protected void onPostExecute(BitmapDrawable result) {

            result = isCancelled() ? null : result;

            if (mImageViewReference != null) {

                ImageView imageView = mImageViewReference.get();

                // Change bitmap only if this process is still associated with it
                if (this == getImageLoaderTask(imageView)) {

                    if (imageView != null && result != null) {

                        setImageDrawable(imageView, result);

                    }
                }
            }

            if (mCallback != null) {

                mCallback.onLoadFinished();

            }
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();

            synchronized (mPauseWorkLock) {

                mPauseWorkLock.notifyAll();

            }
        }

        protected ImageView getAttachedImageView() {

            final ImageView imageView = mImageViewReference.get();

            final ImageAsyncTask bitmapWorkerTask = getImageLoaderTask(imageView);

            if (this == bitmapWorkerTask) {

                return imageView;

            }

            return null;
        }
    }

    public static class ImageLoaderBuilder {

        private boolean mFadeIn = true;
        private int mFadeInTime = 600;

        private Bitmap mPlaceHolderImage;

        private final Context mContext;
        private final Resources mResources;

        private boolean mMemoryCacheEnabled, mDiscCacheEnabled;

        private int mDiscCacheSize = -1, mMemoryCacheSize = -1;
        private float mPartOfAvailableMemoryCache = -1;

        public ImageLoaderBuilder(Context context) {

            mContext = context;
            mResources = context.getResources();

        }

        public ImageLoaderBuilder setDiscCacheEnabled(boolean isEnabled) {

            mDiscCacheEnabled = isEnabled;

            return this;
        }

        public ImageLoaderBuilder setMemoryCacheEnabled(boolean isEnabled) {

            mMemoryCacheEnabled = isEnabled;

            return this;
        }

        public ImageLoaderBuilder setDiscCacheSize(int discCacheSizeInBytes) {

            mDiscCacheSize = discCacheSizeInBytes;

            return this;
        }

        public ImageLoaderBuilder setMemoryCacheSize(int memoryCacheSizeInBytes) {

            mMemoryCacheSize = memoryCacheSizeInBytes;

            return this;
        }

        public ImageLoaderBuilder setPartOfAvailableMemoryCache(float part) {

            mMemoryCacheSize = -1;

            mPartOfAvailableMemoryCache = part;

            return this;
        }

        public ImageLoaderBuilder enableFadeIn(boolean isEnabled) {

            mFadeIn = isEnabled;

            return this;
        }

        public ImageLoaderBuilder setFadeInTime(int time) {

            mFadeInTime = time;

            return this;
        }

        public ImageLoaderBuilder setLoadingImage(int resDrawableID) {

            mPlaceHolderImage = BitmapFactory.decodeResource(mResources, resDrawableID);

            return this;
        }

        public ImageLoaderBuilder setLoadingImage(Bitmap bitmap) {

            mPlaceHolderImage = bitmap;

            return this;
        }

        public SuperImageLoader build() {
            return new SuperImageLoader(this);
        }
    }
}