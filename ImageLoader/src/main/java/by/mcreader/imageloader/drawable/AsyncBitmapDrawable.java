package by.mcreader.imageloader.drawable;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.util.Log;

import java.lang.ref.WeakReference;

import by.mcreader.imageloader.BaseBitmapLoader;
import by.mcreader.imageloader.SuperImageLoaderCore;
import by.mcreader.imageloader.callback.ImageLoaderCallback;

public class AsyncBitmapDrawable extends BitmapDrawable {

    private WeakReference<SuperImageLoaderCore.ImageAsyncTask> mDrawableTaskReference;

    private WeakReference<BaseBitmapLoader> mBitmapLoader;

    private WeakReference<ImageLoaderCallback> mCallback;

    public AsyncBitmapDrawable(Resources resources, Bitmap loadingBitmap, SuperImageLoaderCore.ImageAsyncTask loaderTask, BaseBitmapLoader bitmapLoader, ImageLoaderCallback callback) {
        super(resources, loadingBitmap);

        mDrawableTaskReference = new WeakReference<SuperImageLoaderCore.ImageAsyncTask>(loaderTask);

        mBitmapLoader = new WeakReference<BaseBitmapLoader>(bitmapLoader);

        if (callback != null) mCallback = new WeakReference<ImageLoaderCallback>(callback);
    }

    public SuperImageLoaderCore.ImageAsyncTask getLoaderTask() {
        return mDrawableTaskReference.get();
    }

    public BaseBitmapLoader getBitmapLoader() {
        return mBitmapLoader.get();
    }

    public ImageLoaderCallback getImageLoaderCallback() {
        return mCallback == null ? null : mCallback.get();
    }
}
