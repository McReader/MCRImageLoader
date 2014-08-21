package by.mcreader.imageloader.callback;

import android.graphics.drawable.BitmapDrawable;

public interface ImageLoaderCallback {

    void onLoadingStarted(String url);

    abstract void onLoadingError(Exception e, String url);

    abstract void onLoadingFinished(BitmapDrawable drawable);

}
