package by.mcreader.imageloader.request;

import android.os.Bundle;

/**
 * Created by Dzianis_Roi on 20.11.2014.
 */
public class ParamsProcessor {
    protected final Bundle params;

    public ParamsProcessor(Bundle params) {
        if (params == null)
            throw new IllegalArgumentException("Bad arguments for " + ParamsProcessor.class.getName());

        this.params = params;
    }

    public Bundle params() {
        return this.params;
    }

    public String url() {
        return params.getString(KEYS.src.getKey());
    }

    public String error() {
        return params.getString(KEYS.err.getKey());
    }

    public String memCacheId() {
        return params.getString(KEYS.memCache.getKey());
    }

    public String fileCacheId() {
        return params.getString(KEYS.fileCache.getKey());
    }

    public String loaderId() {
        return params.getString(KEYS.ldr.getKey());
    }

    public boolean sync() {
        return params.getBoolean(KEYS.sync.getKey(), false);
    }

    public int[] size() {
        return params.getIntArray(KEYS.size.getKey());
    }
}
