package gr.thmmy.mthmmy.utils.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.view.ViewCompat;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

/**
 * Extends FloatingActionButton's behavior so the button will hide when scrolling down and show
 * otherwise. It also lifts the {@link FloatingActionButton} when a {@link Snackbar} is shown.
 */
@SuppressWarnings("unused")
public class ScrollAwareFABBehavior extends CoordinatorLayout.Behavior<FloatingActionButton> {

    public ScrollAwareFABBehavior(Context context, AttributeSet attrs) {
        super();
    }

    @Override
    public boolean onStartNestedScroll(@NonNull final CoordinatorLayout coordinatorLayout,
                                       @NonNull final FloatingActionButton child,
                                       @NonNull final View directTargetChild, @NonNull final View target,
                                       final int nestedScrollAxes, int type) {
        return nestedScrollAxes == ViewCompat.SCROLL_AXIS_VERTICAL;
    }

    @Override
    public void onNestedScroll(@NonNull final CoordinatorLayout coordinatorLayout,
                               @NonNull final FloatingActionButton child,
                               @NonNull final View target, final int dxConsumed, final int dyConsumed,
                               final int dxUnconsumed, final int dyUnconsumed, int type) {
        super.onNestedScroll(coordinatorLayout, child, target, dxConsumed, dyConsumed, dxUnconsumed,
                dyUnconsumed, type);
        if (dyConsumed > 0 || (!target.canScrollVertically(-1) && dyConsumed == 0 && dyUnconsumed > 50)) {
            child.hide(new FloatingActionButton.OnVisibilityChangedListener() {
                @Override
                public void onHidden(FloatingActionButton fab) {
                    super.onHidden(fab);
                    fab.setVisibility(View.INVISIBLE);
                }
            });
        } else if (child.getTag() != null && (boolean) child.getTag() && (dyConsumed < 0 ||
                !target.canScrollVertically(-1) && dyUnconsumed < -50)) {
            child.show(new FloatingActionButton.OnVisibilityChangedListener() {
                @Override
                public void onShown(FloatingActionButton fab) {
                    super.onShown(fab);
                    fab.setVisibility(View.VISIBLE);
                }
            });
        }
    }

    @Override
    public boolean layoutDependsOn(CoordinatorLayout parent, FloatingActionButton child, View dependency) {
        return dependency instanceof Snackbar.SnackbarLayout;
    }

    @Override
    public boolean onDependentViewChanged(CoordinatorLayout parent, FloatingActionButton child, View dependency) {
        float translationY = Math.min(0, dependency.getTranslationY() - dependency.getHeight());
        child.setTranslationY(translationY);
        return true;
    }
}