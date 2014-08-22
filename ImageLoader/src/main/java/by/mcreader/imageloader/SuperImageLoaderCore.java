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

public class SuperImageLoaderCore {

    private static final String LOG_TAG = SuperImageLoaderCore.class.getSimpleName();

    private boolean mFadeIn = true;
    private int mFadeInTime;

    private final static ColorDrawable TRANSPARENT_DRAWABLE = new ColorDrawable(android.R.color.transparent);

    private Bitmap mPlaceholderBitmap;

    Context mContext;
    private Resources mResources;

    ImageCacher mImageCacher;

    private final Object mPauseWorkLock = new Object();
    private boolean mPauseWork = false;

    SuperImageLoaderCore(Context context) {
        mContext = context;
        mResources = context.getResources();
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

    public BitmapDrawable loadBitmapSync(String url, int widthInPx, int heightInPx, Bundle extra, BaseBitmapLoader loader) {

        if (TextUtils.isEmpty(url)) {

            Log.e(LOG_TAG, "Empty url!");

            return null;
        }

        BitmapDrawable bitmapDrawable = mImageCacher.getBitmapFromMemoryCache(url);

        if (bitmapDrawable != null) return bitmapDrawable;

        bitmapDrawable = mImageCacher.getBitmapFromFileCache(mContext.getResources(), url);

        if (bitmapDrawable != null) return bitmapDrawable;

        if (extra == null) extra = new Bundle();

        extra.putInt(BaseBitmapLoader.IMAGE_WIDTH_EXTRA, widthInPx <= 0 ? BaseBitmapLoader.DEFAULT_IMAGE_WIDTH : widthInPx);
        extra.putInt(BaseBitmapLoader.IMAGE_HEIGHT_EXTRA, heightInPx <= 0 ? BaseBitmapLoader.DEFAULT_IMAGE_HEIGHT : heightInPx);

        Bitmap bitmap = loader.loadBitmap(url, extra);

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

    public void loadImage(ImageView imageView, String url, int widthInPx, int heightInPx, Bundle params, ImageLoaderCallback callback, BaseBitmapLoader customLoader) {

        if (callback != null) callback.onLoadingStarted(url);

        if (imageView == null || TextUtils.isEmpty(url)) {

            Log.e(LOG_TAG, "Can't load image");

            if (callback != null)
                callback.onLoadingError(new NullPointerException("Can't load image"), url);

            return;
        }

        boolean careAboutSize = widthInPx != -1 || heightInPx != -1;

        BitmapDrawable bitmapDrawable = mImageCacher.getBitmapFromMemoryCache(url);

        // TODO do smth clever
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

            customLoader = customLoader == null ? new DefaultBitmapLoader() : customLoader;

            AsyncBitmapDrawable asyncbitmapDrawable = new AsyncBitmapDrawable(mResources, mPlaceholderBitmap, imageAsyncTask, customLoader);

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

    private static BaseBitmapLoader getAttachedBitmapLoader(ImageView imageView) {

        final Drawable drawable = imageView == null ? null : imageView.getDrawable();

        return drawable instanceof AsyncBitmapDrawable ? ((AsyncBitmapDrawable) drawable).getBitmapLoader() : null;
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

        if (AndroidVersions.hasJellyBean())
            imageView.setBackground(drawable);
        else
            imageView.setBackgroundDrawable(drawable);

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

            if (!mPauseWork) mPauseWorkLock.notifyAll();
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

            start(url, BaseBitmapLoader.DEFAULT_IMAGE_WIDTH, BaseBitmapLoader.DEFAULT_IMAGE_HEIGHT, extra);

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

            if (bitmapDrawable != null) return bitmapDrawable;

            Bitmap bitmap = null;

            ImageView imageView = getAttachedImageView();

            if (!isCancelled() && imageView != null) {

                bitmap = getAttachedBitmapLoader(imageView).loadBitmap(mUrl, params[0]);

            }

            if (bitmap != null) {

                Log.d(LOG_TAG, String.format("Result Bitmap: width = %s height = %s config = %s", bitmap.getWidth(), bitmap.getHeight(), bitmap.getConfig()));

//                if (AndroidVersions.hasHoneycomb()) {
//
//                    bitmapDrawable = new BitmapDrawable(mResources, bitmap);
//
//                }
//                else {

                bitmapDrawable = new RecyclingBitmapDrawable(mResources, bitmap);

//                }

                mImageCacher.put(mUrl, bitmapDrawable);
            }

            return bitmapDrawable;
        }

        @Override
        protected void onPostExecute(BitmapDrawable result) {

            result = isCancelled() ? null : result;

            ImageView imageView = getAttachedImageView();

            // Change bitmap only if this process is still associated with it
            if (imageView != null)
                setImageDrawable(imageView, result);

            // TODO: get attached callback
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

            return this == bitmapWorkerTask ? imageView : null;
        }
    }
}