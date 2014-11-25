package by.mcreader.imageloader.utils;

import android.annotation.SuppressLint;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.widget.ImageView;

/**
 * Created by Dzianis_Roi on 25.11.2014.
 */
public class ImageBinder {

    private final static ColorDrawable TRANSPARENT_DRAWABLE = new ColorDrawable(android.R.color.transparent);

    private ImageBinder() {
    }

    public static void setImageDrawable(Resources r, ImageView imageView, Drawable drawable, Bitmap placeholder, boolean fadeIn, int fadeInTime) {
        final TransitionDrawable td = fadeIn && drawable != null ? new TransitionDrawable(new Drawable[]{TRANSPARENT_DRAWABLE, drawable}) : null;

        if (td == null) {

            imageView.setImageDrawable(drawable);

        } else {
            setBackground(imageView, new BitmapDrawable(r, placeholder));

            imageView.setImageDrawable(td);

            td.startTransition(fadeInTime);
        }
    }

    @SuppressLint("NewApi")
    private static void setBackground(ImageView imageView, BitmapDrawable drawable) {
        if (AndroidVersions.hasJellyBean())
            imageView.setBackground(drawable);
        else
            imageView.setBackgroundDrawable(drawable);
    }
}
