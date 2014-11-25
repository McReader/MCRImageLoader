package by.mcreader.imageloader.processor;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.ImageView;

/**
 * Created by Dzianis_Roi on 20.11.2014.
 */
public interface IPostProcessor {
    void process(Bundle params, ImageView imageView, Bitmap bitmap);
}
