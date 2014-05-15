package by.grsu.mcreader.mcrimageloader.imageloader;

import java.io.IOException;
import java.io.InputStream;

import by.grsu.mcreader.mcrimageloader.imageloader.http.HttpWorker;
import by.grsu.mcreader.mcrimageloader.imageloader.utils.AndroidVersionsUtils;
import by.grsu.mcreader.mcrimageloader.imageloader.utils.BitmapSizeUtil;
import by.grsu.mcreader.mcrimageloader.imageloader.utils.Converter;
import by.grsu.mcreader.mcrimageloader.imageloader.utils.IOUtils;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.util.Log;

public class ImageWorker {

    private static final String LOG_TAG = ImageWorker.class.getSimpleName();

    private static final String GIF = "image/gif";

    private final CacheHelper mCacheHelper;

    private HttpWorker mHttpWorker;

    protected ImageWorker(CacheHelper cacheHelper) {

        mHttpWorker = new HttpWorker();

        mCacheHelper = cacheHelper;
    }

    public Bitmap loadBitmap(Context context, String url, int reqWidthDp, int reqHeightDp) throws IOException {

        return loadBitmap(url, Math.round(Converter.convertDpToPixel(context, reqWidthDp)), Math.round(Converter.convertDpToPixel(context, reqHeightDp)));

    }

    public Bitmap loadBitmap(String url, int reqWidth, int reqHeight) throws IOException {

        InputStream is = null;

        FlushedInputStream fis = null;

        try {

            is = mHttpWorker.getStream(url);

            if (is == null) {

                // Trying to load from file
                BitmapFactory.Options options = new BitmapFactory.Options();

                options.inJustDecodeBounds = true;

                BitmapFactory.decodeFile(url, options);

                options.inJustDecodeBounds = false;
                options.inSampleSize = BitmapSizeUtil.calculateInSampleSize(options, reqWidth, reqHeight);
                options.inPurgeable = true;

                return BitmapFactory.decodeFile(url, options);

            }

            fis = new FlushedInputStream(is);

            fis.mark(is.available());

            BitmapFactory.Options options = new BitmapFactory.Options();

            options.inJustDecodeBounds = true;

            BitmapFactory.decodeStream(fis, null, options);

            options.inSampleSize = BitmapSizeUtil.calculateInSampleSize(options, reqWidth, reqHeight);

            Log.d(LOG_TAG, "input width = " + options.outWidth + ", " + "input height = " + options.outHeight);
            Log.d(LOG_TAG, "sample size = " + options.inSampleSize);
            Log.d(LOG_TAG, "format = " + options.outMimeType);

            options.inJustDecodeBounds = false;

            if (AndroidVersionsUtils.hasHoneycomb() && options.outMimeType != null && !options.outMimeType.equals(GIF)) {

                addInBitmapOptions(options, mCacheHelper);

            }


            fis.reset();

            Bitmap result = BitmapFactory.decodeStream(fis, null, options);

            if (result != null) {

                Log.d(LOG_TAG, "output width = " + result.getWidth() + "\n" + "output height = " + result.getHeight() + "\n" + "result config" + result.getConfig());

            }

            return result;

        } finally {

            IOUtils.closeStream(fis);
            IOUtils.closeStream(is);

        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private static void addInBitmapOptions(BitmapFactory.Options options, CacheHelper cache) {

        options.inMutable = true;

        if (cache != null) {

            Bitmap inBitmap = cache.getBitmapFromReusableSet(options);

            if (inBitmap != null) {

                Log.d(LOG_TAG, "Found bitmap to use for inBitmap");

                options.inBitmap = inBitmap;

            }
        }
    }
}
