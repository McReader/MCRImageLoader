package by.mcreader.imageloader.drawable;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;

import java.lang.ref.WeakReference;

import by.mcreader.imageloader.SuperImageLoader;

public class AsyncBitmapDrawable extends BitmapDrawable {

    private WeakReference<SuperImageLoader.ImageAsyncTask> mDrawableTaskReference;

    public AsyncBitmapDrawable(Resources resources, Bitmap loadingBitmap, SuperImageLoader.ImageAsyncTask loaderTask) {
        super(resources, loadingBitmap);

        mDrawableTaskReference = new WeakReference<SuperImageLoader.ImageAsyncTask>(loaderTask);
    }

    public SuperImageLoader.ImageAsyncTask getLoaderTask() {
        return mDrawableTaskReference.get();
    }

}
