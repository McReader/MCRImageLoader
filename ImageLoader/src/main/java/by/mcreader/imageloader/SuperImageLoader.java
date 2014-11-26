package by.mcreader.imageloader;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;

import java.lang.ref.WeakReference;

import by.mcreader.imageloader.cache.file.FileCache;
import by.mcreader.imageloader.cache.memory.MemoryCache;
import by.mcreader.imageloader.callback.ImageLoaderCallback;
import by.mcreader.imageloader.drawable.AsyncBitmapDrawable;
import by.mcreader.imageloader.drawable.RecyclingBitmapDrawable;
import by.mcreader.imageloader.task.AsyncTask;
import by.mcreader.imageloader.utils.AndroidVersions;
import by.mcreader.imageloader.utils.BitmapUtil;
import by.mcreader.imageloader.utils.ImageBinder;
import by.mcreader.imageloader.view.RecyclingImageView;

public class SuperImageLoader {

    private static final String TAG = SuperImageLoader.class.getSimpleName();

    protected Resources res;

    protected boolean fadeIn;
    protected int fadeInTime;

    private final Object pauseWorkLock = new Object();
    private boolean pauseWork = false;

    protected SuperImageLoader(Resources res, boolean fadeIn, int fadeInTime) {
        this.res = res;

        this.fadeIn = fadeIn;
        this.fadeInTime = fadeInTime;
    }

    public void load(Request r) {
        ImageLoaderCallback cb = r.getCallback();

        // if has any error => invoke error callback and finishing execution
        if (r.hasError() && cb != null) {
            cb.onError(r.params(), r.getView());
            return;
        }

        // looking for memory cache implementation instance by id
        MemoryCache cache = (MemoryCache) SuperManager.getService(r.memCacheId());
        // get target view
        RecyclingImageView view = r.getView();
        // get all params
        Bundle params = r.params();

        // notifying, that loading has been started if callback exists
        if (cb != null) cb.onStarted(params, view);

        // if cache exists => trying to get bitmap from memory cache.
        BitmapDrawable bd = cache == null ? null : cache.get(r.url());

        if (BitmapUtil.isMatchedSize(bd, r.size())) {

            if (view != null) view.setImageDrawable(bd);

            if (cb != null) cb.onFinished(params, view, bd);

        } else {

            if (r.sync())
                loadSync(r);
            else
                loadAsync(r);

        }
    }

    private void loadSync(Request r) {
        // TODO
    }

    private void loadAsync(Request r) {
        RecyclingImageView view = r.getView();

        if (cancelPotentialDownload(view, r.url())) {

            ImageAsyncTask imageAsyncTask = new ImageAsyncTask();

            AsyncBitmapDrawable asyncbitmapDrawable = new AsyncBitmapDrawable(res, SuperManager.placeholder, imageAsyncTask, r);

            if (view != null) view.setImageDrawable(asyncbitmapDrawable);

            imageAsyncTask.start(r);
        }
    }

    private static boolean cancelPotentialDownload(ImageView imageView, String url) {
        ImageAsyncTask bitmapAsyncTask = getImageLoaderTask(imageView);

        if (bitmapAsyncTask == null) return true;

        if (bitmapAsyncTask.url == null || !bitmapAsyncTask.url.equals(url)) {

            bitmapAsyncTask.cancel(true);

            Log.d(TAG, "cancelPotentialDownload for " + url);

            return true;
        }

        return false;
    }

    private static ImageAsyncTask getImageLoaderTask(ImageView imageView) {
        final Drawable drawable = imageView == null ? null : imageView.getDrawable();

        return drawable instanceof AsyncBitmapDrawable ? ((AsyncBitmapDrawable) drawable).getLoaderTask() : null;
    }

    public class ImageAsyncTask extends AsyncTask<Request, Void, BitmapDrawable> {

        private WeakReference<Request> reqReference;

        protected String url;

        private ImageAsyncTask() {
        }

        public void start(Request r) {
            url = r.url();

            this.reqReference = new WeakReference<Request>(r);
            // custom version of AsyncTask
            executeOnExecutor(DUAL_THREAD_EXECUTOR, r);
        }

        @Override
        protected BitmapDrawable doInBackground(Request... params) {
            synchronized (pauseWorkLock) {
                while (pauseWork && !isCancelled()) {
                    try {
                        pauseWorkLock.wait();
                    } catch (InterruptedException e) {
                        // can be ignored
                    }
                }
            }

            Request r = getAttachedRequest();

            if (r == null) return null;

            FileCache file = SuperManager.getFileCache(r.fileCacheId());

            Bitmap b = file == null ? null : file.get(url);

            if (BitmapUtil.isMatchedSize(b, r.size()))
                return AndroidVersions.hasHoneycomb() ? new BitmapDrawable(res, b) : new RecyclingBitmapDrawable(res, b);

            if (isCancelled()) return null;

            BaseBitmapLoader l = SuperManager.getLoader(r.loaderId());
            BitmapDrawable bd = null;

            b = l.loadBitmap(r.params());

            if (b != null) {
                Log.d(TAG, String.format("Result Bitmap: width = %s height = %s config = %s", b.getWidth(), b.getHeight(), b.getConfig()));

                bd = AndroidVersions.hasHoneycomb() ? new BitmapDrawable(res, b) : new RecyclingBitmapDrawable(res, b);

                MemoryCache memory = SuperManager.getMemCache(r.memCacheId());

                if (memory != null) memory.put(url, bd);
                if (file != null) file.put(url, b);
            }

            return bd;
        }

        @Override
        protected void onPostExecute(BitmapDrawable result) {
            result = isCancelled() ? null : result;

            Request r = getAttachedRequest();

            // Change bitmap only if this process is still associated with it
            if (r != null) {
                RecyclingImageView view = r.getView();
                ImageLoaderCallback cb = r.getCallback();

                ImageBinder.setImageDrawable(res, view, result, SuperManager.placeholder, fadeIn, fadeInTime);

                if (cb != null)
                    cb.onFinished(r.params(), view, result);
            }
        }

        protected Request getAttachedRequest() {
            final Request r = reqReference.get();

            if (r == null) return null;

            final ImageAsyncTask bitmapWorkerTask = getImageLoaderTask(r.getView());

            return this == bitmapWorkerTask ? r : null;
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();

            synchronized (pauseWorkLock) {
                pauseWorkLock.notifyAll();
            }
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
    public void pause(boolean pause) {
        synchronized (pauseWorkLock) {
            this.pauseWork = pause;
            if (!pauseWork) pauseWorkLock.notifyAll();
        }
    }
}