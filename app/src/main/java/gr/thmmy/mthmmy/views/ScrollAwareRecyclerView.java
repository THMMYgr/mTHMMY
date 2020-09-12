package gr.thmmy.mthmmy.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

import androidx.recyclerview.widget.RecyclerView;

// Based on https://stackoverflow.com/a/46453825
// Not needed in LatestPostsFragment as there are no other ScrollAware Behaviors to steal touch events
public class ScrollAwareRecyclerView extends RecyclerView {
    public ScrollAwareRecyclerView(Context context) {
        super(context);
    }

    public ScrollAwareRecyclerView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ScrollAwareRecyclerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        final int scrollState = getScrollState();
        boolean requestCancelDisallowInterceptTouchEvent
                = ((scrollState == SCROLL_STATE_SETTLING) || (scrollState == SCROLL_STATE_IDLE));
        boolean consumed = super.onInterceptTouchEvent(event);
        final int action = event.getActionMasked();

        if (action == MotionEvent.ACTION_DOWN) {
            if (requestCancelDisallowInterceptTouchEvent) {
                getParent().requestDisallowInterceptTouchEvent(false);

                // only if it touched the top or the bottom
                if (!canScrollVertically(-1) || !canScrollVertically(1)) {
                    // stop scroll to enable child view to get the touch event
                    stopScroll();
                    // do not consume the event
                    return false;
                }
            }
        }
        return consumed;
    }

}
