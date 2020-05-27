package gr.thmmy.mthmmy.views;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

//Custom RecyclerView, so EdgeEffect and SwipeRefresh both work
public class CustomRecyclerView extends RecyclerView {
    private volatile boolean enableRefreshing = true;

    public CustomRecyclerView(Context context) {
        super(context);
    }

    public CustomRecyclerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomRecyclerView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public void onScrolled(int dx, int dy) {
        if (dy > 0)
            enableRefreshing = false;
        super.onScrolled(dx, dy);
    }


    @Override
    public void onScrollStateChanged(int state) {
        if ((state != SCROLL_STATE_DRAGGING) && ((LinearLayoutManager) getLayoutManager()).findFirstCompletelyVisibleItemPosition() == 0)
            enableRefreshing = true;
        else if (getChildCount() == 0)
            enableRefreshing = true;
        else if (((LinearLayoutManager) getLayoutManager()).findFirstCompletelyVisibleItemPosition() != 0)
            enableRefreshing = false;


        super.onScrollStateChanged(state);
    }

    @Override
    public boolean dispatchNestedScroll(int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed, int[] offsetInWindow) {
        if (enableRefreshing)
            return super.dispatchNestedScroll(dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, offsetInWindow);
        else
            return super.dispatchNestedScroll(dxConsumed, dyConsumed, dxUnconsumed, 0, offsetInWindow);
    }

}
