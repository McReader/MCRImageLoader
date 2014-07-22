package by.grsu.mcreader.mcrimageloader.imageloader;

import android.graphics.BitmapFactory;
import android.os.Bundle;

/**
 * Created by dzianis_roi on 21.07.2014.
 */
public abstract class BaseBitmapSourceLoader {

    private Bundle mParams;

    protected abstract byte[] getBitmapSource(String url, int width, int height, BitmapFactory.Options options);

    public void setParams(Bundle params) {
        this.mParams = params;
    }

    public Bundle getParams() {
        return mParams;
    }

}
