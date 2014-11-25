package by.mcreader.imageloader.drawable;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;

import java.lang.ref.WeakReference;

import by.mcreader.imageloader.SuperImageLoader;
import by.mcreader.imageloader.Request;

public class AsyncBitmapDrawable extends BitmapDrawable {

    private WeakReference<SuperImageLoader.ImageAsyncTask> mTaskReference;

    private WeakReference<Request> request;

    public AsyncBitmapDrawable(Resources resources, int bitmapRes, SuperImageLoader.ImageAsyncTask loaderTask, Request r) {
        this(resources, BitmapFactory.decodeResource(resources, bitmapRes), loaderTask, r);
    }

    public AsyncBitmapDrawable(Resources resources, Bitmap bitmap, SuperImageLoader.ImageAsyncTask loaderTask, Request r) {
        super(resources, bitmap);

        mTaskReference = new WeakReference<SuperImageLoader.ImageAsyncTask>(loaderTask);

        request = new WeakReference<Request>(r);
    }

    public SuperImageLoader.ImageAsyncTask getLoaderTask() {
        return mTaskReference.get();
    }

    public Request getRequest() {
        return request.get();
    }
}
