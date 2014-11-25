package by.mcreader.imageloader;

import android.content.res.Resources;

import java.util.ArrayList;
import java.util.List;

import by.mcreader.imageloader.cache.FileCache;
import by.mcreader.imageloader.cache.MemCache;

/**
 * Created by Dzianis_Roi on 21.11.2014.
 */
public class Builder {

    protected List<Service> services;

    protected boolean fadeIn = true;
    protected int fadeInTime = 300;

    public Resources res;

    public Builder(Resources resources) {
        this.res = resources;
    }

    public Builder memoryCache(MemCache cache) {
        if (services == null) services = new ArrayList<Service>();

        services.add(cache);
        return this;
    }

    public Builder fileCache(FileCache cache) {
        if (services == null) services = new ArrayList<Service>();

        services.add(cache);
        return this;
    }

    public Builder loader(BaseBitmapLoader loader) {
        if (services == null) services = new ArrayList<Service>();

        services.add(loader);
        return this;
    }

    public Builder fadeIn(boolean isEnabled) {
        fadeIn = isEnabled;
        return this;
    }

    public Builder fadeInTime(int time) {
        fadeInTime = time;
        return this;
    }

    public Provider build() {
        return new Provider(this);
    }
}