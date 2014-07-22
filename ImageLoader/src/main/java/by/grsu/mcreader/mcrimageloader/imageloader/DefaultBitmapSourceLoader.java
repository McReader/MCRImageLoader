package by.grsu.mcreader.mcrimageloader.imageloader;

import android.graphics.BitmapFactory;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;

import by.grsu.mcreader.mcrimageloader.imageloader.http.HttpWorker;
import by.grsu.mcreader.mcrimageloader.imageloader.utils.IOUtils;

/**
 * Created by dzianis_roi on 21.07.2014.
 */
public class DefaultBitmapSourceLoader extends BitmapSourceLoader {

    private static final String LOG_TAG = DefaultBitmapSourceLoader.class.getSimpleName();

    private HttpWorker mHttpWorker;

    protected DefaultBitmapSourceLoader() {
        mHttpWorker = new HttpWorker();
    }

    @Override
    protected byte[] getBuffer(String url, int width, int height, BitmapFactory.Options options) {

        InputStream is = null;

        byte[] result = null;

        try {

            is = mHttpWorker.getStream(url);

            result = new byte[is.available()];

            is.read(result);

        } catch (IOException e) {

            Log.d(LOG_TAG, e.getMessage());

        } finally {

            IOUtils.closeStream(is);

        }

        return result;

    }
}
