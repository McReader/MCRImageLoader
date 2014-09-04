package by.mcreader.imageloader;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.ImageView;

import by.mcreader.imageloader.callback.ImageLoaderCallback;

/**
 * Created by Dzianis_Roi on 22.08.2014.
 */
public class SuperImageLoader extends SuperImageLoaderCore {

    private SuperImageLoader(ImageLoaderBuilder builder) {
        super(builder.sContext);

        setPlaceholder(builder.sPlaceholder);

        setFadeIn(builder.sFadeIn);
        setFadeInTime(builder.sFadeInTime);

        mImageCacher = new ImageCacher(
                mContext.getCacheDir(),
                builder.sMemoryCacheEnabled,
                builder.sDiscCacheEnabled,
                builder.sMemoryCacheSize,
                builder.sDiscCacheSize);
    }


    /**
     * ===========  Sync   =========
     */

    public void loadBitmapSync(String url) {
        super.loadBitmapSync(url, -1, -1, null, getDefaultLoader());
    }

    public void loadBitmapSync(String url, Bundle extra) {
        super.loadBitmapSync(url, -1, -1, extra, getDefaultLoader());
    }

    public void loadBitmapSync(String url, BaseBitmapLoader loader) {
        super.loadBitmapSync(url, -1, -1, null, loader);
    }

    public void loadBitmapSync(String url, int widthInPx, int heightInPx) {
        super.loadBitmapSync(url, widthInPx, heightInPx, null, getDefaultLoader());
    }

    public void loadBitmapSync(String url, int widthInPx, int heightInPx, Bundle params) {
        super.loadBitmapSync(url, widthInPx, heightInPx, params, getDefaultLoader());
    }

    /**
     * ======== In Background ========
     */

    public void loadImage(ImageView imageView, String url) {
        super.loadImage(imageView, url, -1, -1, null, null, getDefaultLoader());
    }

    public void loadImage(ImageView imageView, String url, Bundle extra) {
        super.loadImage(imageView, url, -1, -1, extra, null, getDefaultLoader());
    }

    public void loadImage(ImageView imageView, String url, ImageLoaderCallback callback) {
        super.loadImage(imageView, url, -1, -1, null, callback, getDefaultLoader());
    }

    public void loadImage(ImageView imageView, String url, BaseBitmapLoader loader) {
        super.loadImage(imageView, url, -1, -1, null, null, loader);
    }

    public void loadImage(ImageView imageView, String url, int widthInPx, int heightInPx) {
        super.loadImage(imageView, url, widthInPx, heightInPx, null, null, getDefaultLoader());
    }

    public void loadImage(ImageView imageView, String url, int widthInPx, int heightInPx, Bundle params) {
        super.loadImage(imageView, url, widthInPx, heightInPx, params, null, getDefaultLoader());
    }

    public void loadImage(ImageView imageView, String url, int widthInPx, int heightInPx, Bundle params, ImageLoaderCallback callback) {
        super.loadImage(imageView, url, widthInPx, heightInPx, params, callback, getDefaultLoader());
    }

    public void loadImage(ImageView imageView, String url, int widthInPx, int heightInPx, Bundle params, BaseBitmapLoader loader) {
        super.loadImage(imageView, url, widthInPx, heightInPx, params, null, loader);
    }

    public void loadImage(ImageView imageView, String url, int widthInPx, int heightInPx, ImageLoaderCallback callback) {
        super.loadImage(imageView, url, widthInPx, heightInPx, null, callback, getDefaultLoader());
    }

    public void loadImage(ImageView imageView, String url, int widthInPx, int heightInPx, ImageLoaderCallback callback, BaseBitmapLoader loader) {
        super.loadImage(imageView, url, widthInPx, heightInPx, null, callback, loader);
    }

    /**
     * ============ Builder ============
     */

    public static class ImageLoaderBuilder {

        private boolean sFadeIn = true;
        
        private int sFadeInTime = 300;

        private final Context sContext;

        private Bitmap sPlaceholder;

        private boolean sMemoryCacheEnabled, sDiscCacheEnabled;

        private int sDiscCacheSize = -1, sMemoryCacheSize = -1;

        public ImageLoaderBuilder(Context context) {
            sContext = context;
        }

        public ImageLoaderBuilder setDiscCacheEnabled(boolean isEnabled) {

            sDiscCacheEnabled = isEnabled;

            return this;
        }

        public ImageLoaderBuilder setMemoryCacheEnabled(boolean isEnabled) {

            sMemoryCacheEnabled = isEnabled;

            return this;
        }

        public ImageLoaderBuilder setDiscCacheSize(int discCacheSizeInBytes) {

            sDiscCacheSize = discCacheSizeInBytes;

            return this;
        }

        public ImageLoaderBuilder setMemoryCacheSize(int memoryCacheSizeInBytes) {

            sMemoryCacheSize = memoryCacheSizeInBytes;

            return this;
        }

        public ImageLoaderBuilder enableFadeIn(boolean isEnabled) {

            sFadeIn = isEnabled;

            return this;
        }

        public ImageLoaderBuilder setFadeInTime(int time) {

            sFadeInTime = time;

            return this;
        }

        public ImageLoaderBuilder setPlaceholder(int resDrawableID) {

            sPlaceholder = BitmapFactory.decodeResource(sContext.getResources(), resDrawableID);

            return this;
        }

        public ImageLoaderBuilder setPlaceholder(Bitmap bitmap) {

            sPlaceholder = bitmap;

            return this;
        }

        public SuperImageLoader build() {
            return new SuperImageLoader(this);
        }
    }
}
