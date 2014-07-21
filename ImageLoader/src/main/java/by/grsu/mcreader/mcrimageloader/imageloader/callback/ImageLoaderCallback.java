package by.grsu.mcreader.mcrimageloader.imageloader.callback;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;

public interface ImageLoaderCallback {

    void onLoadingStarted(String url);

    abstract void onLoadingError(Exception e, String url);

    abstract void onLoadingFinished(BitmapDrawable drawable);

}
