package by.mcreader.imageloader.callback;

import android.widget.AbsListView;

import by.mcreader.imageloader.SuperImageLoader;

/**
 * Created by dzianis_roi on 22.07.2014.
 */
public class PauseScrollListener implements AbsListView.OnScrollListener {

    private SuperImageLoader imageLoader;

    private final AbsListView.OnScrollListener externalListener;

    private boolean pauseOnScroll, pauseOnFling;

    public PauseScrollListener(SuperImageLoader imageLoader, boolean pauseOnScroll, boolean pauseOnFling) {
        this(imageLoader, pauseOnScroll, pauseOnFling, null);
    }

    public PauseScrollListener(SuperImageLoader imageLoader, boolean pauseOnScroll, boolean pauseOnFling, AbsListView.OnScrollListener externalListener) {
        this.imageLoader = imageLoader;

        this.pauseOnScroll = pauseOnScroll;
        this.pauseOnFling = pauseOnFling;

        this.externalListener = externalListener;
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        switch (scrollState) {
            case AbsListView.OnScrollListener.SCROLL_STATE_IDLE:
                imageLoader.setPauseWork(false);
                break;
            case AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL:
                if (pauseOnScroll) imageLoader.setPauseWork(true);
                break;
            case AbsListView.OnScrollListener.SCROLL_STATE_FLING:
                if (pauseOnFling) imageLoader.setPauseWork(true);
                break;
        }

        if (externalListener != null)
            externalListener.onScrollStateChanged(view, scrollState);

    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

        if (externalListener != null)
            externalListener.onScroll(view, firstVisibleItem, visibleItemCount, totalItemCount);

    }
}
