package by.mcreader.imageloader;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import by.mcreader.imageloader.cache.FileCache;
import by.mcreader.imageloader.cache.MemoryCache;
import by.mcreader.imageloader.utils.Converter;

/**
 * Created by Dzianis_Roi on 21.11.2014.
 */
public class SuperManager {

    private final static Map<String, Service> _services = new HashMap<String, Service>();

    protected static Bitmap placeholder;

    private static SuperImageLoader imageLoader;

    private SuperManager(Builder b) {
        registerServices(b.services);

        setPlaceholder(b.placeholder);

        imageLoader = new SuperImageLoader(b.res, b.fadeIn, b.fadeInTime);
    }

    protected static void registerServices(List<Service> services) {
        if (services != null)
            for (Service s : services) registerService(s);
    }

    protected static void registerService(Service entity) {
        _services.put(Converter.stringToMD5(entity.id()), entity);
    }

    protected static void unregisterService(Service entity) {
        _services.remove(Converter.stringToMD5(entity.id()));
    }

    protected static MemoryCache getMemCache(String id) {
        Service s = getService(id);

        if (s == null) return null;

        if (!(s instanceof MemoryCache))
            throw new IllegalArgumentException("Service with id: " + id + " is not MemCache instance.");

        return (MemoryCache) s;
    }

    protected static FileCache getFileCache(String id) {
        Service s = getService(id);

        if (s == null) return null;

        if (!(s instanceof MemoryCache))
            throw new IllegalArgumentException("Service with id: " + id + " is not FileCache instance.");

        return (FileCache) s;
    }

    protected static BaseBitmapLoader getLoader(String id) {
        Service l = getService(id);

        if (!(l instanceof BaseBitmapLoader))
            throw new IllegalArgumentException("No loader exists with given id " + id);

        return (BaseBitmapLoader) l;
    }

    public static Service getService(String id) {
        return _services.get(Converter.stringToMD5(id));
    }

    public void load(Request r) {
        imageLoader.load(r);
    }

    public void pause(boolean pause) {
        imageLoader.pause(pause);
    }

    public void setPlaceholder(final Resources r, final int bitmapRes) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                setPlaceholder(BitmapFactory.decodeResource(r, bitmapRes));
            }
        }).run();
    }

    public static void setPlaceholder(Bitmap b) {
        placeholder = b;
    }

    public class Builder {

        private List<Service> services;

        private Bitmap placeholder;

        private boolean fadeIn = true;
        private int fadeInTime = 300;

        private Resources res;

        public Builder(Resources resources) {
            this.res = resources;
        }

        public Builder memoryCache(MemoryCache cache) {
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

        public Builder placeholder(Bitmap bitmap) {
            placeholder = bitmap;
            return this;
        }

        public SuperManager build() {
            return new SuperManager(this);
        }
    }
}
