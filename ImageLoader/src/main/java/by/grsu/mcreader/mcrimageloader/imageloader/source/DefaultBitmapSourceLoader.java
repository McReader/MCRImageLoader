package by.grsu.mcreader.mcrimageloader.imageloader.source;

import android.graphics.BitmapFactory;
import android.os.Bundle;

import java.io.InputStream;

import by.grsu.mcreader.mcrimageloader.imageloader.BaseBitmapSourceLoader;
import by.grsu.mcreader.mcrimageloader.imageloader.http.HttpWorker;

/**
 * Created by dzianis_roi on 21.07.2014.
 */
public class DefaultBitmapSourceLoader extends BaseBitmapSourceLoader<InputStream> {

    private HttpWorker mHttpWorker;

    public DefaultBitmapSourceLoader() {
        mHttpWorker = new HttpWorker();
    }

    @Override
    protected InputStream getSource(String url, BitmapFactory.Options options, Bundle extra) {

        return mHttpWorker.getStream(url);

    }
}
