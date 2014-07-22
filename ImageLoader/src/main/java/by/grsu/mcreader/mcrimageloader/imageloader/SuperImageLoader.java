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
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.ImageView;

import java.lang.ref.WeakReference;

import by.grsu.mcreader.mcrimageloader.imageloader.callback.ImageLoaderCallback;
import by.grsu.mcreader.mcrimageloader.imageloader.drawable.AsyncBitmapDrawable;
import by.grsu.mcreader.mcrimageloader.imageloader.drawable.RecyclingBitmapDrawable;
import by.grsu.mcreader.mcrimageloader.imageloader.source.DefaultBitmapSourceLoader;
import by.grsu.mcreader.mcrimageloader.imageloader.utils.AndroidVersionsUtils;
import by.grsu.mcreader.mcrimageloader.imageloader.utils.BitmapSizeUtil;

public class SuperImageLoader {

    private static final String LOG_TAG = SuperImageLoader.class.getSimpleName();

    private static final String GIF = "image/gif";

    private boolean mFadeIn = true;
    private int mFadeInTime = 600; // Default fade in time

    private final static int DEFAULT_IMAGE_WIDTH = 300;
    private final static int DEFAULT_IMAGE_HEIGHT = 300;

    private final static ColorDrawable TRANSPARENT_DRAWABLE = new ColorDrawable(android.R.color.transparent);

    private Bitmap mPlaceholderBitmap;

    private final Context mContext;
    private final Resources mResources;

    private ImageCacher mImageCacher;

    private final BaseBitmapSourceLoader mBitmapSourceLoader;

    private final Object mPauseWorkLock = new Object();
    private boolean mPauseWork = false;

    private SuperImageLoader(ImageLoaderBuilder builder) {

        mContext = builder.sContext;
        mResources = builder.sResources;

        mPlaceholderBitmap = builder.sPlaceHolderImage;

        mFadeIn = builder.sFadeIn;
        mFadeInTime = builder.sFadeInTime;

        mImageCacher = new ImageCacher(
                mContext.getCacheDir(),
                builder.sMemoryCacheEnabled,
                builder.sDiscCacheEnabled,
                builder.sMemoryCacheSize,
                builder.sDiscCacheSize);

        mBitmapSourceLoader = builder.sCustomLoader == null ? new DefaultBitmapSourceLoader() : builder.sCustomLoader;
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

    public BitmapDrawable loadBitmapSync(String url) {
        return loadBitmapSync(url, 0, 0);
    }

    public BitmapDrawable loadBitmapSync(String url, int widthInPx, int heightInPx) {

        if (TextUtils.isEmpty(url)) {

            Log.e(LOG_TAG, "Empty url!");

            return null;
        }

        boolean careAboutSize = widthInPx > 0 || heightInPx > 0;

        widthInPx = widthInPx <= 0 ? DEFAULT_IMAGE_WIDTH : widthInPx;
        heightInPx = heightInPx <= 0 ? DEFAULT_IMAGE_HEIGHT : heightInPx;

        BitmapDrawable bitmapDrawable = mImageCacher.getBitmapFromMemoryCache(url);

        if (bitmapDrawable != null) {

            if (careAboutSize) {

                if (BitmapSizeUtil.inspectDimensions(bitmapDrawable, widthInPx, heightInPx)) {

                    return bitmapDrawable;

                }

            } else {

                return bitmapDrawable;

            }
        }

        bitmapDrawable = mImageCacher.getBitmapFromFileCache(mContext.getResources(), url);

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

        BitmapFactory.Options options = new BitmapFactory.Options();

        byte[] buffer = mBitmapSourceLoader.getBitmapSource(url, widthInPx, heightInPx, options);

        options.inJustDecodeBounds = true;

        BitmapFactory.decodeByteArray(buffer, 0, buffer.length, options);

        options.inSampleSize = BitmapSizeUtil.calculateInSampleSize(options, widthInPx, heightInPx);

        options.inJustDecodeBounds = false;

        Log.d(LOG_TAG, String.format(" \ninput width = %s, \ninput height = %s \nsample size = %s \nformat = %s", options.outWidth, options.outHeight, options.inSampleSize, options.outMimeType));

        if (AndroidVersionsUtils.hasHoneycomb() && !TextUtils.isEmpty(options.outMimeType) && !options.outMimeType.equals(GIF)) {

            mImageCacher.addInBitmapOptions(options);

        }

        bitmap = BitmapFactory.decodeByteArray(buffer, 0, buffer.length, options);

        if (bitmap != null) {

            if (AndroidVersionsUtils.hasHoneycomb() && bitmap.getConfig() != null) {

                bitmapDrawable = new BitmapDrawable(mResources, bitmap);

            } else {

                bitmapDrawable = new RecyclingBitmapDrawable(mResources, bitmap);

            }

            mImageCacher.put(url, bitmapDrawable);
        }

        return bitmapDrawable;
    }

    public void loadBitmap(ImageView imageView, String url) {
        loadBitmap(imageView, url, 0, 0, null, null);
    }

    public void loadBitmap(ImageView imageView, String url, int widthInPx, int heightInPx) {
        loadBitmap(imageView, url, widthInPx, heightInPx, null, null);
    }

    public void loadBitmap(ImageView imageView, String url, int widthInPx, int heightInPx, ImageLoaderCallback callback) {
        loadBitmap(imageView, url, widthInPx, heightInPx, null, callback);
    }

    public void loadBitmap(ImageView imageView, String url, ImageLoaderCallback callback) {
        loadBitmap(imageView, url, 0, 0, null, callback);
    }

    public void loadBitmap(ImageView imageView, String url, int widthInPx, int heightInPx, Bundle params, ImageLoaderCallback callback) {

        if (callback != null) callback.onLoadingStarted(url);

        if (imageView == null || TextUtils.isEmpty(url)) {

            Log.e(LOG_TAG, "Can't load image");

            if (callback != null)
                callback.onLoadingError(new NullPointerException("Can't load image"), url);

            return;
        }

//        boolean careAboutSize = widthInPx > 0 && heightInPx > 0;

        BitmapDrawable bitmapDrawable = mImageCacher.getBitmapFromMemoryCache(url);

        if (bitmapDrawable != null) {

            // TODO: handle careAboutSize BitmapSizeUtil.inspectDimensions(bitmapDrawable, widthInPx, heightInPx)

            imageView.setImageDrawable(bitmapDrawable);

            if (callback != null) callback.onLoadingFinished(bitmapDrawable);

            return;

        }

        if (cancelPotentialDownload(imageView, url)) {

            ImageAsyncTask imageAsyncTask = new ImageAsyncTask(imageView, callback);

            imageAsyncTask.setParams(params);

            AsyncBitmapDrawable asyncbitmapDrawable = new AsyncBitmapDrawable(mResources, mPlaceholderBitmap, imageAsyncTask);

            imageView.setImageDrawable(asyncbitmapDrawable);

            if (widthInPx > 0 && heightInPx > 0) {

                imageAsyncTask.start(url, widthInPx, heightInPx);

            } else {

                imageAsyncTask.start(url);

            }
        }
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

            setBackground(imageView, new BitmapDrawable(mResources, mPlaceholderBitmap));

            imageView.setImageDrawable(td);

            td.startTransition(mFadeInTime);

        }
    }

    @SuppressLint("NewApi")
    private void setBackground(ImageView imageView, BitmapDrawable drawable) {

        if (AndroidVersionsUtils.hasJellyBean()) {

            imageView.setBackground(drawable);

        } else {

            imageView.setBackgroundDrawable(drawable);

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

    public class ImageAsyncTask extends AsyncTask<String, Void, BitmapDrawable> {

        protected String mUrl;

        private int mWidth;
        private int mHeight;

        private final WeakReference<ImageView> mImageViewReference;

        private final ImageLoaderCallback mCallback;

        private Bundle mParams;

        public ImageAsyncTask(ImageView imageView, ImageLoaderCallback callback) {

            mImageViewReference = new WeakReference<ImageView>(imageView);

            mCallback = callback;

        }

        public void start(String url) {

            this.start(url, DEFAULT_IMAGE_WIDTH, DEFAULT_IMAGE_HEIGHT);

        }

        public void setParams(Bundle params) {
            mParams = params;
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

            synchronized (mPauseWorkLock) {

                while (mPauseWork && !isCancelled()) {

                    try {

                        mPauseWorkLock.wait();

                    } catch (InterruptedException e) {
                        // can be ignored
                    }
                }
            }

            BitmapDrawable bitmapDrawable = mImageCacher.getBitmapFromFileCache(mContext.getResources(), mUrl);

            if (bitmapDrawable != null) {

                return bitmapDrawable;

            }

            Bitmap bitmap = null;


            if (!isCancelled() && getAttachedImageView() != null) {

                if (mParams != null) {

                    mBitmapSourceLoader.setParams(mParams);

                }

                BitmapFactory.Options options = new BitmapFactory.Options();

                byte[] buffer = mBitmapSourceLoader.getBitmapSource(mUrl, mWidth, mHeight, options);

                if (buffer == null || buffer.length <= 0) {

                    mCallback.onLoadingError(new IllegalArgumentException("Can't get image source!"), mUrl);

                    return null;
                }

                options.inJustDecodeBounds = true;

                BitmapFactory.decodeByteArray(buffer, 0, buffer.length, options);

                options.inSampleSize = BitmapSizeUtil.calculateInSampleSize(options, mWidth, mHeight);

                options.inJustDecodeBounds = false;

                Log.d(LOG_TAG, String.format("Input bitmap: \nwidth = %s, \nheight = %s \nsample size = %s \nformat = %s", options.outWidth, options.outHeight, options.inSampleSize, options.outMimeType));

                if (AndroidVersionsUtils.hasHoneycomb() && !TextUtils.isEmpty(options.outMimeType) && !options.outMimeType.equals(GIF)) {

                    mImageCacher.addInBitmapOptions(options);

                }

                bitmap = BitmapFactory.decodeByteArray(buffer, 0, buffer.length, options);

            }

            if (bitmap != null) {

                Log.d(LOG_TAG, String.format("Result Bitmap: \nwidth = %s \n height = %s \nconfig = %s", bitmap.getWidth(), bitmap.getHeight(), bitmap.getConfig()));

                if (AndroidVersionsUtils.hasHoneycomb()) {

                    bitmapDrawable = new BitmapDrawable(mResources, bitmap);

                } else {

                    bitmapDrawable = new RecyclingBitmapDrawable(mResources, bitmap);

                }

                mImageCacher.put(mUrl, bitmapDrawable);
            }

            return bitmapDrawable;
        }

        @Override
        protected void onPostExecute(BitmapDrawable result) {

            result = isCancelled() ? null : result;

            ImageView imageView = mImageViewReference.get();

            // Change bitmap only if this process is still associated with it
            if (this == getImageLoaderTask(imageView)) {

                if (imageView != null && result != null) {

                    setImageDrawable(imageView, result);

                }
            }

            if (mCallback != null) mCallback.onLoadingFinished(result);
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

        private boolean sFadeIn = true;
        private int sFadeInTime = 600;

        private Bitmap sPlaceHolderImage;

        private final Context sContext;
        private final Resources sResources;

        private boolean sMemoryCacheEnabled, sDiscCacheEnabled;

        private int sDiscCacheSize = -1, sMemoryCacheSize = -1;

        private float sPartOfAvailableMemoryCache = -1;

        private BaseBitmapSourceLoader sCustomLoader;

        public ImageLoaderBuilder(Context context) {

            sContext = context;
            sResources = context.getResources();

        }

        public ImageLoaderBuilder setDiscCacheEnabled(boolean isEnabled) {

            sDiscCacheEnabled = isEnabled;

            return this;
        }

        public ImageLoaderBuilder setMemoryCacheEnabled(boolean isEnabled) {

            sMemoryCacheEnabled = isEnabled;

            return this;
        }

        public ImageLoaderBuilder setDiscCacheSize(int discCacheSizeInBytes) {

            sDiscCacheSize = discCacheSizeInBytes;

            return this;
        }

        public ImageLoaderBuilder setMemoryCacheSize(int memoryCacheSizeInBytes) {

            sMemoryCacheSize = memoryCacheSizeInBytes;

            return this;
        }

        public ImageLoaderBuilder setPartOfAvailableMemoryCache(float part) {

            sMemoryCacheSize = -1;

            sPartOfAvailableMemoryCache = part;

            return this;
        }

        public ImageLoaderBuilder enableFadeIn(boolean isEnabled) {

            sFadeIn = isEnabled;

            return this;
        }

        public ImageLoaderBuilder setFadeInTime(int time) {

            sFadeInTime = time;

            return this;
        }

        public ImageLoaderBuilder setLoadingImage(int resDrawableID) {

            sPlaceHolderImage = BitmapFactory.decodeResource(sResources, resDrawableID);

            return this;
        }

        public ImageLoaderBuilder setLoadingImage(Bitmap bitmap) {

            sPlaceHolderImage = bitmap;

            return this;
        }

        public ImageLoaderBuilder setCustomLoader(BaseBitmapSourceLoader loader) {

            this.sCustomLoader = loader;

            return this;
        }

        public SuperImageLoader build() {
            return new SuperImageLoader(this);
        }
    }
}