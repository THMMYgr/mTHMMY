package gr.thmmy.mthmmy.utils;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import timber.log.Timber;

public class CustomLinearLayoutManager extends LinearLayoutManager {
    private String pageUrl;

    public CustomLinearLayoutManager(Context context, String pageUrl) {
        super(context);
        this.pageUrl = pageUrl;
    }

    @Override
    public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
        try {
            super.onLayoutChildren(recycler, state);
        } catch (IndexOutOfBoundsException e) {
            Timber.wtf(e, "Inconsistency detected: topic_requested = \"" + pageUrl + "\"");
            Log.d("CustomLinearLayoutMan", "Inconsistency detected: topic_requested = \""
                    + pageUrl + "\"", e);
        }
    }

    /**
     * Disable predictive animations. There is a bug in RecyclerView which causes views that
     * are being reloaded to pull invalid ViewHolders from the internal recycler stack if the
     * adapter size has decreased since the ViewHolder was recycled.
     */
    @Override
    public boolean supportsPredictiveItemAnimations() {
        return false;
    }
}