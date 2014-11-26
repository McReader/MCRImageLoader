package by.mcreader.imageloader.cache;

import android.graphics.drawable.BitmapDrawable;

import by.mcreader.imageloader.Service;

/**
 * Created by Dzianis_Roi on 21.11.2014.
 */
public interface MemoryCache extends Service {

    BitmapDrawable get(String key);

    void put(String key, BitmapDrawable value);
}
