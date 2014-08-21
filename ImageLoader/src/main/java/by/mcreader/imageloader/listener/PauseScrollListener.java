package by.mcreader.imageloader.listener;

import android.widget.AbsListView;

import by.mcreader.imageloader.SuperImageLoader;

/**
 * Created by dzianis_roi on 22.07.2014.
 */
public class PauseScrollListener implements AbsListView.OnScrollListener {

    private SuperImageLoader imageLoader;

    private final AbsListView.OnScrollListener externalListener;

    public PauseScrollListener(SuperImageLoader imageLoader) {
        this(imageLoader, null);
    }

    public PauseScrollListener(SuperImageLoader imageLoader, AbsListView.OnScrollListener externalListener) {
        this.imageLoader = imageLoader;
        this.externalListener = externalListener;
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        switch (scrollState) {
            case AbsListView.OnScrollListener.SCROLL_STATE_IDLE:
                imageLoader.setPauseWork(false);
                break;
            case AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL:
                imageLoader.setPauseWork(true);
                break;
            case AbsListView.OnScrollListener.SCROLL_STATE_FLING:
                imageLoader.setPauseWork(true);
                break;
        }

        if (externalListener != null) {

            externalListener.onScrollStateChanged(view, scrollState);

        }
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

        if (externalListener != null) {

            externalListener.onScroll(view, firstVisibleItem, visibleItemCount, totalItemCount);

        }
    }
}
