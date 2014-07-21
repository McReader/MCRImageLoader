package by.grsu.mcreader.mcrimageloader.imageloader;

import android.graphics.BitmapFactory;

import java.io.IOException;
import java.io.InputStream;

import by.grsu.mcreader.mcrimageloader.imageloader.http.HttpWorker;
import by.grsu.mcreader.mcrimageloader.imageloader.utils.IOUtils;

/**
 * Created by dzianis_roi on 21.07.2014.
 */
public class DefaultBitmapLoader extends BitmapLoader {

    private static final String LOG_TAG = DefaultBitmapLoader.class.getSimpleName();

    private static final String GIF = "image/gif";

    private HttpWorker mHttpWorker;

    protected DefaultBitmapLoader() {
        mHttpWorker = new HttpWorker();
    }

    @Override
    protected byte[] getBuffer(String url, int width, int height, BitmapFactory.Options options) {
        InputStream is = null;

        FlushedInputStream fis = null;

        byte[] buffer = null;

        try {

            is = mHttpWorker.getStream(url);

            fis = new FlushedInputStream(is);

            buffer = new byte[fis.available()];

            fis.read(buffer);

        } catch (IOException e) {

            // TODO

        } finally {

            IOUtils.closeStream(fis);
            IOUtils.closeStream(is);

        }

        return buffer;
    }
}
