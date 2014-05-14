package by.grsu.mcreader.mcrimageloader.imageloader.view;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.util.AttributeSet;
import android.widget.ImageView;

import by.grsu.mcreader.mcrimageloader.imageloader.drawable.RecyclingBitmapDrawable;

public class RecyclingImageView extends ImageView {

    public RecyclingImageView(Context context) {
        super(context);
    }

    public RecyclingImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onDetachedFromWindow() {
        setImageDrawable(null);
        super.onDetachedFromWindow();
    }

    @Override
    public void setImageDrawable(Drawable drawable) {

        final Drawable previousDrawable = getDrawable();

        super.setImageDrawable(drawable);

        notifyDrawable(drawable, true);
        
        notifyDrawable(previousDrawable, false);
    }

    private static void notifyDrawable(Drawable drawable, final boolean isDisplayed) {

        if (drawable instanceof RecyclingBitmapDrawable) {

            ((RecyclingBitmapDrawable) drawable).setIsDisplayed(isDisplayed);

        } else if (drawable instanceof LayerDrawable) {

            LayerDrawable layerDrawable = (LayerDrawable) drawable;

            for (int i = 0, z = layerDrawable.getNumberOfLayers(); i < z; i++) {

                notifyDrawable(layerDrawable.getDrawable(i), isDisplayed);

            }
        }
    }
}
