package by.mcreader.imageloader.source;

import android.graphics.BitmapFactory;
import android.os.Bundle;

import java.io.InputStream;

import by.mcreader.imageloader.BaseBitmapLoader;
import by.mcreader.imageloader.http.HttpWorker;

/**
 * Created by dzianis_roi on 21.07.2014.
 */
public class DefaultBitmapLoader extends BaseBitmapLoader<InputStream> {

    private HttpWorker mHttpWorker;

    public DefaultBitmapLoader() {
        mHttpWorker = new HttpWorker();
    }

    @Override
    protected InputStream getSource(String url, BitmapFactory.Options options, Bundle extra) {

        return mHttpWorker.getStream(url);

    }
}
