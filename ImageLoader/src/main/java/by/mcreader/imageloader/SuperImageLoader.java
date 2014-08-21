package by.mcreader.imageloader;

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

import by.mcreader.imageloader.callback.ImageLoaderCallback;
import by.mcreader.imageloader.drawable.AsyncBitmapDrawable;
import by.mcreader.imageloader.drawable.RecyclingBitmapDrawable;
import by.mcreader.imageloader.source.DefaultBitmapLoader;
import by.mcreader.imageloader.utils.AndroidVersions;
import by.mcreader.imageloader.utils.BitmapAnalizer;

public class SuperImageLoader {

    private static final String LOG_TAG = SuperImageLoader.class.getSimpleName();

    private boolean mFadeIn = true;
    private int mFadeInTime; // Default fade in time

    public final static int DEFAULT_IMAGE_WIDTH = 300;
    public final static int DEFAULT_IMAGE_HEIGHT = 300;

    private final static ColorDrawable TRANSPARENT_DRAWABLE = new ColorDrawable(android.R.color.transparent);

    private Bitmap mPlaceholderBitmap;

    private final Context mContext;
    private final Resources mResources;

    private ImageCacher mImageCacher;

    private BaseBitmapLoader mBitmapSourceLoader;

    private final Object mPauseWorkLock = new Object();
    private boolean mPauseWork = false;

    private SuperImageLoader(ImageLoaderBuilder builder) {

        mContext = builder.sContext;
        mResources = builder.sResources;

        mPlaceholderBitmap = builder.sPlaceholder;

        mFadeIn = builder.sFadeIn;
        mFadeInTime = builder.sFadeInTime;

        mImageCacher = new ImageCacher(
                mContext.getCacheDir(),
                builder.sMemoryCacheEnabled,
                builder.sDiscCacheEnabled,
                builder.sMemoryCacheSize,
                builder.sDiscCacheSize);

        setBitmapSourceLoader(builder.sCustomLoader);
    }

    public void setBitmapSourceLoader(BaseBitmapLoader bitmapSourceLoader) {
        if (this.mBitmapSourceLoader == bitmapSourceLoader) return;

        this.mBitmapSourceLoader = bitmapSourceLoader == null ? new DefaultBitmapLoader() : bitmapSourceLoader;
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
        return loadBitmapSync(url, 0, 0, null);
    }

    public BitmapDrawable loadBitmapSync(String url, Bundle extra) {
        return loadBitmapSync(url, 0, 0, extra);
    }

    public BitmapDrawable loadBitmapSync(String url, int widthInPx, int heightInPx, Bundle extra) {

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

                if (BitmapAnalizer.inspectDimensions(bitmapDrawable, widthInPx, heightInPx)) {

                    return bitmapDrawable;

                }

            } else {

                return bitmapDrawable;

            }
        }

        bitmapDrawable = mImageCacher.getBitmapFromFileCache(mContext.getResources(), url);

        if (bitmapDrawable != null) {

            if (careAboutSize) {

                if (BitmapAnalizer.inspectDimensions(bitmapDrawable, widthInPx, heightInPx)) {

                    return bitmapDrawable;

                }

            } else {

                return bitmapDrawable;

            }
        }

//        if (AndroidVersions.hasHoneycomb() && !TextUtils.isEmpty(options.outMimeType) && (options.outMimeType.equals(JPEG) || options.outMimeType.equals(PNG))) {
//
//            mImageCacher.addInBitmapOptions(options);
//
//        }

        Bitmap bitmap = mBitmapSourceLoader.loadBitmap(url, extra);

        if (bitmap != null) {

//            if (AndroidVersions.hasHoneycomb() && bitmap.getConfig() != null) {
//
//                bitmapDrawable = new BitmapDrawable(mResources, bitmap);
//
//            } else {

            bitmapDrawable = new RecyclingBitmapDrawable(mResources, bitmap);

//            }

            mImageCacher.put(url, bitmapDrawable);
        }

        return bitmapDrawable;
    }

    public void loadBitmap(ImageView imageView, String url) {
        loadBitmap(imageView, url, -1, -1, null, null);
    }

    public void loadBitmap(ImageView imageView, String url, int widthInPx, int heightInPx) {
        loadBitmap(imageView, url, widthInPx, heightInPx, null, null);
    }

    public void loadBitmap(ImageView imageView, String url, int widthInPx, int heightInPx, ImageLoaderCallback callback) {
        loadBitmap(imageView, url, widthInPx, heightInPx, null, callback);
    }

    public void loadBitmap(ImageView imageView, String url, ImageLoaderCallback callback) {
        loadBitmap(imageView, url, -1, -1, null, callback);
    }

    public void loadBitmap(ImageView imageView, String url, int widthInPx, int heightInPx, Bundle params, ImageLoaderCallback callback) {

        if (callback != null) callback.onLoadingStarted(url);

        if (imageView == null || TextUtils.isEmpty(url)) {

            Log.e(LOG_TAG, "Can't load image");

            if (callback != null)
                callback.onLoadingError(new NullPointerException("Can't load image"), url);

            return;
        }

        boolean careAboutSize = widthInPx != -1 || heightInPx != -1;

        BitmapDrawable bitmapDrawable = mImageCacher.getBitmapFromMemoryCache(url);

        if (careAboutSize && !BitmapAnalizer.inspectDimensions(bitmapDrawable, widthInPx, heightInPx)) {
            bitmapDrawable = null;
        }

        if (bitmapDrawable != null) {

            imageView.setImageDrawable(bitmapDrawable);

            if (callback != null) callback.onLoadingFinished(bitmapDrawable);

            return;

        }

        if (cancelPotentialDownload(imageView, url)) {

            ImageAsyncTask imageAsyncTask = new ImageAsyncTask(imageView, callback);

            AsyncBitmapDrawable asyncbitmapDrawable = new AsyncBitmapDrawable(mResources, mPlaceholderBitmap, imageAsyncTask);

            imageView.setImageDrawable(asyncbitmapDrawable);

            if (widthInPx > 0 && heightInPx > 0) {

                imageAsyncTask.start(url, widthInPx, heightInPx, params);

            } else {

                imageAsyncTask.start(url, params);

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

        if (AndroidVersions.hasJellyBean()) {

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

    public class ImageAsyncTask extends AsyncTask<Bundle, Void, BitmapDrawable> {

        protected String mUrl;

        private final WeakReference<ImageView> mImageViewReference;

        private final ImageLoaderCallback mCallback;

        public ImageAsyncTask(ImageView imageView, ImageLoaderCallback callback) {

            mImageViewReference = new WeakReference<ImageView>(imageView);

            mCallback = callback;

        }

        public void start(String url, Bundle extra) {

            start(url, DEFAULT_IMAGE_WIDTH, DEFAULT_IMAGE_HEIGHT, extra);

        }

        public void start(String url, int width, int height, Bundle extra) {

            mUrl = url;

            if (extra == null) extra = new Bundle();

            extra.putInt(BaseBitmapLoader.IMAGE_WIDTH_EXTRA, width);
            extra.putInt(BaseBitmapLoader.IMAGE_HEIGHT_EXTRA, height);

            // custom version of AsyncTask
            executeOnExecutor(DUAL_THREAD_EXECUTOR, extra);
        }

        @Override
        protected BitmapDrawable doInBackground(Bundle... params) {

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

//                Log.d(LOG_TAG, String.format("Input bitmap: \nwidth = %s, \nheight = %s \nsample size = %s \nformat = %s", options.outWidth, options.outHeight, options.inSampleSize, options.outMimeType));

//                if (AndroidVersions.hasHoneycomb()) {
//
//                    mImageCacher.addInBitmapOptions(options);
//
//                }

                bitmap = mBitmapSourceLoader.loadBitmap(mUrl, params[0]);

            }

            if (bitmap != null) {

                Log.d(LOG_TAG, String.format("Result Bitmap: \nwidth = %s \n height = %s \nconfig = %s", bitmap.getWidth(), bitmap.getHeight(), bitmap.getConfig()));

                /*if (AndroidVersions.hasHoneycomb()) {

                    bitmapDrawable = new BitmapDrawable(mResources, bitmap);

                } else {*/

                bitmapDrawable = new RecyclingBitmapDrawable(mResources, bitmap);

//                }

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
        private int sFadeInTime = 300;

        private Bitmap sPlaceholder;

        private final Context sContext;
        private final Resources sResources;

        private boolean sMemoryCacheEnabled, sDiscCacheEnabled;

        private int sDiscCacheSize = -1, sMemoryCacheSize = -1;

        private BaseBitmapLoader sCustomLoader;

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

        public ImageLoaderBuilder enableFadeIn(boolean isEnabled) {

            sFadeIn = isEnabled;

            return this;
        }

        public ImageLoaderBuilder setFadeInTime(int time) {

            sFadeInTime = time;

            return this;
        }

        public ImageLoaderBuilder setLoadingImage(int resDrawableID) {

            sPlaceholder = BitmapFactory.decodeResource(sResources, resDrawableID);

            return this;
        }

        public ImageLoaderBuilder setPlaceholder(Bitmap bitmap) {

            sPlaceholder = bitmap;

            return this;
        }

        public ImageLoaderBuilder setCustomLoader(BaseBitmapLoader loader) {

            this.sCustomLoader = loader;

            return this;
        }

        public SuperImageLoader build() {
            return new SuperImageLoader(this);
        }
    }
}