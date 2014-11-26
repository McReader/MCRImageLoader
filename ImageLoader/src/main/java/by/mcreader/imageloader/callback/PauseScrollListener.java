package by.mcreader.imageloader.callback;

import android.widget.AbsListView;

import by.mcreader.imageloader.SuperManager;

/**
 * Created by dzianis_roi on 22.07.2014.
 */
public class PauseScrollListener implements AbsListView.OnScrollListener {

    private SuperManager imageManager;

    private final AbsListView.OnScrollListener externalListener;

    private boolean pauseOnScroll, pauseOnFling;

    public PauseScrollListener(SuperManager imageManager, boolean pauseOnScroll, boolean pauseOnFling) {
        this(imageManager, pauseOnScroll, pauseOnFling, null);
    }

    public PauseScrollListener(SuperManager imageManager, boolean pauseOnScroll, boolean pauseOnFling, AbsListView.OnScrollListener externalListener) {
        this.imageManager = imageManager;

        this.pauseOnScroll = pauseOnScroll;
        this.pauseOnFling = pauseOnFling;

        this.externalListener = externalListener;
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        switch (scrollState) {
            case AbsListView.OnScrollListener.SCROLL_STATE_IDLE:
                imageManager.pause(false);
                break;
            case AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL:
                if (pauseOnScroll) imageManager.pause(true);
                break;
            case AbsListView.OnScrollListener.SCROLL_STATE_FLING:
                if (pauseOnFling) imageManager.pause(true);
                break;
        }

        if (externalListener != null) externalListener.onScrollStateChanged(view, scrollState);
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        if (externalListener != null)
            externalListener.onScroll(view, firstVisibleItem, visibleItemCount, totalItemCount);
    }
}
