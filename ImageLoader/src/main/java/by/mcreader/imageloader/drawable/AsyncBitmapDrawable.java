package by.mcreader.imageloader.drawable;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;

import java.lang.ref.WeakReference;

import by.mcreader.imageloader.BaseBitmapLoader;
import by.mcreader.imageloader.SuperImageLoaderCore;

public class AsyncBitmapDrawable extends BitmapDrawable {

    private WeakReference<SuperImageLoaderCore.ImageAsyncTask> mDrawableTaskReference;

    private WeakReference<BaseBitmapLoader> mBitmapLoader;

    public AsyncBitmapDrawable(Resources resources, Bitmap loadingBitmap, SuperImageLoaderCore.ImageAsyncTask loaderTask, BaseBitmapLoader bitmapLoader) {
        super(resources, loadingBitmap);

        mDrawableTaskReference = new WeakReference<SuperImageLoaderCore.ImageAsyncTask>(loaderTask);

        mBitmapLoader = new WeakReference<BaseBitmapLoader>(bitmapLoader);
    }

    public SuperImageLoaderCore.ImageAsyncTask getLoaderTask() {
        return mDrawableTaskReference.get();
    }

    public BaseBitmapLoader getBitmapLoader() {
        return mBitmapLoader.get();
    }
}
