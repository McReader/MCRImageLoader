package by.mcreader.imageloader.cache;

import android.graphics.Bitmap;

import by.mcreader.imageloader.Service;

/**
 * Created by Dzianis_Roi on 25.11.2014.
 */
public interface FileCache extends Service {
    Bitmap get(String key);

    void put(String key, Bitmap value);
}