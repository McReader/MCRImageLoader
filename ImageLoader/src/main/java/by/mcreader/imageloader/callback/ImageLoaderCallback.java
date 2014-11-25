package by.mcreader.imageloader.callback;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;

import by.mcreader.imageloader.view.RecyclingImageView;

public interface ImageLoaderCallback {

    void onStarted(Bundle params, RecyclingImageView imageView);

    abstract void onError(Bundle params, RecyclingImageView imageView);

    abstract void onFinished(Bundle params, RecyclingImageView imageView, BitmapDrawable drawable);

}
