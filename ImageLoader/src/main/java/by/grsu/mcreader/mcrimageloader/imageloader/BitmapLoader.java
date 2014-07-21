package by.grsu.mcreader.mcrimageloader.imageloader;

import android.annotation.TargetApi;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import by.grsu.mcreader.mcrimageloader.imageloader.utils.AndroidVersionsUtils;
import by.grsu.mcreader.mcrimageloader.imageloader.utils.BitmapSizeUtil;

/**
 * Created by dzianis_roi on 21.07.2014.
 */
public abstract class BitmapLoader {

    private static final String LOG_TAG = BitmapLoader.class.getSimpleName();

    private static final String GIF = "image/gif";

    private Bundle mParams;

    private CacheHelper mCacheHelper;

    protected abstract byte[] getBuffer(String url, int width, int height, BitmapFactory.Options options);

    protected Bitmap load(String url, int width, int height) {

        BitmapFactory.Options options = new BitmapFactory.Options();

        byte[] source = getBuffer(url, width, height, options);

        options.inJustDecodeBounds = true;

        BitmapFactory.decodeByteArray(source, 0, source.length, options);

        options.inJustDecodeBounds = true;

        options.inSampleSize = BitmapSizeUtil.calculateInSampleSize(options, width, height);

        Log.d(LOG_TAG, String.format("input width = %s, input height = %s \nsample size = %s \nformat = %s", options.outWidth, options.outHeight, options.inSampleSize, options.outMimeType));

        if (AndroidVersionsUtils.hasHoneycomb() && !TextUtils.isEmpty(options.outMimeType) && !options.outMimeType.equals(GIF)) {

            addInBitmapOptions(options);

        }

        return BitmapFactory.decodeByteArray(source, 0, source.length, options);
    }


    public void setParams(Bundle params) {
        this.mParams = params;
    }

    public Bundle getParams() {
        return mParams;
    }

    protected void setCacheHelper(CacheHelper helper) {
        this.mCacheHelper = helper;
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void addInBitmapOptions(BitmapFactory.Options options) {

        options.inMutable = true;

        if (mCacheHelper != null) {

            Bitmap inBitmap = mCacheHelper.getBitmapFromReusableSet(options);

            if (inBitmap != null) {

                Log.d(LOG_TAG, "Found bitmap to use for inBitmap");

                options.inBitmap = inBitmap;

            }
        }
    }
}
