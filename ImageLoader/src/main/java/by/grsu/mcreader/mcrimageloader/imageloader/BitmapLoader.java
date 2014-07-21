package by.grsu.mcreader.mcrimageloader.imageloader;

import android.graphics.Bitmap;
import android.os.Bundle;

import java.io.IOException;

/**
 * Created by dzianis_roi on 21.07.2014.
 */
public abstract class BitmapLoader {

    private Bundle mParams;

    protected abstract Bitmap loadBitmap(String url, int width, int height) throws IOException;

    public void setParams(Bundle params) {
        this.mParams = params;
    }

    public Bundle getParams() {
        return mParams;
    }
}
