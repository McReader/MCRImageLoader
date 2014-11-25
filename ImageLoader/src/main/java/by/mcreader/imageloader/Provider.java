package by.mcreader.imageloader;

import android.graphics.Bitmap;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import by.mcreader.imageloader.cache.FileCache;
import by.mcreader.imageloader.cache.MemCache;
import by.mcreader.imageloader.utils.Converter;

/**
 * Created by Dzianis_Roi on 21.11.2014.
 */
public class Provider {

    private final static Map<String, Service> _services = new HashMap<String, Service>();

    protected static Bitmap placeholder;

    private static SuperImageLoader imageLoader;

    protected Provider(Builder b) {
        registerServices(b.services);

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

    protected static MemCache getMemCache(String id) {
        Service s = getService(id);

        if (s == null) return null;

        if (!(s instanceof MemCache))
            throw new IllegalArgumentException("Service with id: " + id + " is not MemCache instance.");

        return (MemCache) s;
    }

    protected static FileCache getFileCache(String id) {
        Service s = getService(id);

        if (s == null) return null;

        if (!(s instanceof MemCache))
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

    public SuperImageLoader getImageLoader() {
//        imageLoader.res = resources;
//
//        imageLoader.fadeIn = fadeIn;
//        imageLoader.fadeInTime = fadeInTime;

        return imageLoader;
    }
}
