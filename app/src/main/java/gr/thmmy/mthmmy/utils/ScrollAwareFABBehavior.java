package gr.thmmy.mthmmy.utils;

import android.content.Context;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.View;

/**
 * Extends FloatingActionButton's behavior so the button will hide when scrolling down and show
 * otherwise.
 */
@SuppressWarnings("WeakerAccess")
public class ScrollAwareFABBehavior extends CoordinatorLayout.Behavior<FloatingActionButton> {
    String TAG = "ScrollAwareFABBehavior";

    @SuppressWarnings("UnusedParameters")
    public ScrollAwareFABBehavior(Context context, AttributeSet attrs) {
        super();
    }

    @Override
    public boolean onStartNestedScroll(final CoordinatorLayout coordinatorLayout, final FloatingActionButton child,
                                       final View directTargetChild, final View target, final int nestedScrollAxes) {
        return nestedScrollAxes == ViewCompat.SCROLL_AXIS_VERTICAL;
    }

    @Override
    public void onNestedScroll(final CoordinatorLayout coordinatorLayout,
                               final FloatingActionButton child,
                               final View target, final int dxConsumed, final int dyConsumed,
                               final int dxUnconsumed, final int dyUnconsumed) {
        super.onNestedScroll(coordinatorLayout, child, target, dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed);
        if ((dyConsumed > 0 || (!target.canScrollVertically(-1) && dyConsumed == 0
                && dyUnconsumed < 40)) && child.getVisibility() == View.VISIBLE) {
            child.hide(new FloatingActionButton.OnVisibilityChangedListener() {
                @Override
                public void onHidden(FloatingActionButton fab) {
                    super.onHidden(fab);
                    fab.setVisibility(View.INVISIBLE);
                }
            });
        } else if ((dyConsumed < 0 || (!target.canScrollVertically(1) && dyConsumed == 0
                && dyUnconsumed > 40)) && child.getVisibility() != View.VISIBLE) {
            child.show();
        }
    }
}