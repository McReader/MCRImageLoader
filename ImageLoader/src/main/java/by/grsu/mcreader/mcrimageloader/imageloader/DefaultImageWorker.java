package by.grsu.mcreader.mcrimageloader.imageloader;

import java.io.IOException;
import java.io.InputStream;

import by.grsu.mcreader.mcrimageloader.imageloader.http.HttpWorker;
import by.grsu.mcreader.mcrimageloader.imageloader.utils.AndroidVersionsUtils;
import by.grsu.mcreader.mcrimageloader.imageloader.utils.BitmapSizeUtil;
import by.grsu.mcreader.mcrimageloader.imageloader.utils.IOUtils;

import android.annotation.TargetApi;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.util.Log;

public class DefaultImageWorker extends BitmapLoader {

    private static final String LOG_TAG = DefaultImageWorker.class.getSimpleName();

    private static final String GIF = "image/gif";

    private HttpWorker mHttpWorker;

    protected DefaultImageWorker() {

        mHttpWorker = new HttpWorker();

    }

    public Bitmap loadBitmap(String url, int reqWidth, int reqHeight) throws IOException {

        InputStream is = null;

        FlushedInputStream fis = null;

        try {

            is = mHttpWorker.getStream(url);

            fis = new FlushedInputStream(is);

            fis.mark(is.available());

            BitmapFactory.Options options = new BitmapFactory.Options();

            options.inJustDecodeBounds = true;

            BitmapFactory.decodeStream(fis, null, options);

            options.inJustDecodeBounds = false;

            options.inSampleSize = BitmapSizeUtil.calculateInSampleSize(options, reqWidth, reqHeight);

            Log.d(LOG_TAG, String.format("input width = %s, input height = %s \nsample size = %s \nformat = %s", options.outWidth, options.outHeight, options.inSampleSize, options.outMimeType));

            if (AndroidVersionsUtils.hasHoneycomb() && options.outMimeType != null && !options.outMimeType.equals(GIF)) {

                addInBitmapOptions(options);

            }

            fis.reset();

            Bitmap result = BitmapFactory.decodeStream(fis, null, options);

            if (result != null) {

                Log.d(LOG_TAG, String.format("output width = %s, output height = %s \nconfig = %s", result.getWidth(), result.getHeight(), result.getConfig()));

            }

            return result;

        } finally {

            IOUtils.closeStream(fis);
            IOUtils.closeStream(is);

        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private static void addInBitmapOptions(BitmapFactory.Options options) {

        CacheHelper cache = SuperImageLoader.getCacheHelper();

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
