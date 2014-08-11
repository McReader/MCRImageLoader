package by.grsu.mcreader.mcrimageloader.imageloader.source;

import android.graphics.BitmapFactory;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;

import by.grsu.mcreader.mcrimageloader.imageloader.BaseBitmapSourceLoader;
import by.grsu.mcreader.mcrimageloader.imageloader.http.HttpWorker;
import by.grsu.mcreader.mcrimageloader.imageloader.utils.IOUtils;

/**
 * Created by dzianis_roi on 21.07.2014.
 */
public class DefaultBitmapSourceLoader extends BaseBitmapSourceLoader<InputStream> {

    private HttpWorker mHttpWorker;

    public DefaultBitmapSourceLoader() {
        mHttpWorker = new HttpWorker();
    }

    @Override
    protected InputStream getSource(String url, BitmapFactory.Options options) {

        return mHttpWorker.getStream(url);

    }
}
