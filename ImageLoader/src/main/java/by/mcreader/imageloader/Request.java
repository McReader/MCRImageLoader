package by.mcreader.imageloader;

import android.os.Bundle;
import android.text.TextUtils;

import by.mcreader.imageloader.callback.ImageLoaderCallback;
import by.mcreader.imageloader.request.KEYS;
import by.mcreader.imageloader.request.ParamsProcessor;
import by.mcreader.imageloader.view.RecyclingImageView;

/**
 * Created by Dzianis_Roi on 20.11.2014.
 */
public class Request extends ParamsProcessor {

    private RecyclingImageView view;

    private ImageLoaderCallback callback;

    private boolean hasError, sync;

    private Request(Bundle params) {
        super(params);
    }

    public boolean hasError() {
        return hasError;
    }

    public ImageLoaderCallback getCallback() {
        return this.callback;
    }

    public RecyclingImageView getView() {
        return view;
    }

    public static class Builder {

        protected Bundle params = new Bundle();
        protected RecyclingImageView view;

        protected ImageLoaderCallback callback;

        protected boolean hasError, sync;

        public Builder from(String url) {

            if (TextUtils.isEmpty(url)) return setError("Empty url!!");

            params.putString(KEYS.src.getKey(), url);

            return this;
        }

        public Builder to(RecyclingImageView imageView) {
            if (imageView == null) return setError("View is NULL!!");

            view = imageView;

            return this;
        }

        public Builder size(int width, int height) {
            int[] size = {width <= 0 ? 300 : width, height <= 0 ? 300 : height};

            this.params.putIntArray(KEYS.size.getKey(), size);

            return this;
        }

        public Builder extra(Bundle extra) {
            if (extra != null) this.params.putAll(extra);
            return this;
        }

        public Builder loader(String loaderId) {
            this.params.putString(KEYS.ldr.getKey(), loaderId);
            return this;
        }

        public Builder memoryCache(String memoryCacheId) {
            this.params.putString(KEYS.memCache.getKey(), memoryCacheId);
            return this;
        }

        public Builder fileCache(String fileCacheId) {
            this.params.putString(KEYS.fileCache.getKey(), fileCacheId);
            return this;
        }

        public Builder callback(ImageLoaderCallback callback) {
            this.callback = callback;
            return this;
        }

        public Builder sync(boolean sync) {
            this.params.putBoolean(KEYS.sync.getKey(), sync);
            return this;
        }

        private Builder setError(String message) {
            hasError = true;

            this.params.putString(KEYS.err.getKey(), message);
            return this;
        }

        public Request build() {
            Request r = new Request(params);

            r.view = view;

            r.callback = callback;

            r.hasError = hasError;
            r.sync = sync;

            return r;
        }
    }
}
