package by.mcreader.imageloader.loader;

import android.graphics.BitmapFactory;
import android.os.Bundle;

import java.io.InputStream;

import by.mcreader.imageloader.BaseBitmapLoader;
import by.mcreader.imageloader.utils.HttpUtil;
import by.mcreader.imageloader.request.KEYS;

/**
 * Created by dzianis_roi on 21.07.2014.
 */
public class DefaultBitmapLoader extends BaseBitmapLoader<InputStream> {

    public static final String ID = "loader.DefaultLoader";

    private HttpUtil mHttpUtil;

    public DefaultBitmapLoader() {
        mHttpUtil = new HttpUtil();
    }

    @Override
    protected InputStream getSource(Bundle params, BitmapFactory.Options options) {
        return mHttpUtil.getStream(params.getString(KEYS.src.getKey()));
    }

    @Override
    public String id() {
        return ID;
    }
}
