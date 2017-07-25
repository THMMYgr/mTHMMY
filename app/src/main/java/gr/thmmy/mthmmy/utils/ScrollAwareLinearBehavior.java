package gr.thmmy.mthmmy.utils;

import android.animation.Animator;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewPropertyAnimator;

/**
 * Extends LinearLayout's behavior. Used for bottom navigation bar.
 * <p>When a nested ScrollView is scrolled down, the view will disappear.
 * When the ScrollView is scrolled back up, the view will reappear.</p>
 */
@SuppressWarnings("unused")
public class ScrollAwareLinearBehavior extends CoordinatorLayout.Behavior<View> {
    private static final int ANIMATION_DURATION = 100;

    public ScrollAwareLinearBehavior(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onStartNestedScroll(@NonNull CoordinatorLayout coordinatorLayout,
                                       @NonNull View child, @NonNull View directTargetChild,
                                       @NonNull View target, int nestedScrollAxes, int type) {
        return (nestedScrollAxes & ViewCompat.SCROLL_AXIS_VERTICAL) != 0;
    }

    @Override
    public void onNestedScroll(@NonNull CoordinatorLayout coordinatorLayout,
                               @NonNull View bottomNavBar, @NonNull View target,
                               int dxConsumed, int dyConsumed,
                               int dxUnconsumed, int dyUnconsumed, int type) {
        super.onNestedScroll(coordinatorLayout, bottomNavBar, target, dxConsumed, dyConsumed,
                dxUnconsumed, dyUnconsumed, type);
        if ((dyConsumed > 0 || (!target.canScrollVertically(-1) && dyConsumed == 0
                && dyUnconsumed < 40)) && bottomNavBar.getVisibility() == View.VISIBLE) {
            hide(bottomNavBar);
        } else if (dyConsumed < 0 && bottomNavBar.getVisibility() != View.VISIBLE) {
            show(bottomNavBar);
        }
    }

    /**
     * Animates the hiding of a bottom navigation bar.
     *
     * @param bottomNavBar bottom navigation bar View
     */
    private void hide(final View bottomNavBar) {
        ViewPropertyAnimator animator = bottomNavBar.animate()
                .translationY(bottomNavBar.getHeight())
                .setInterpolator(new FastOutSlowInInterpolator())
                .setDuration(ANIMATION_DURATION);

        animator.setListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {
            }

            @Override
            public void onAnimationEnd(Animator animator) {
                bottomNavBar.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onAnimationCancel(Animator animator) {
            }

            @Override
            public void onAnimationRepeat(Animator animator) {
            }
        });

        animator.start();
    }

    /**
     * Animates the showing of a bottom navigation bar.
     *
     * @param bottomNavBar bottom navigation bar View
     */
    private void show(final View bottomNavBar) {
        ViewPropertyAnimator animator = bottomNavBar.animate()
                .translationY(0)
                .setInterpolator(new FastOutSlowInInterpolator())
                .setDuration(ANIMATION_DURATION);

        animator.setListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {
                bottomNavBar.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animator animator) {
            }

            @Override
            public void onAnimationCancel(Animator animator) {
            }

            @Override
            public void onAnimationRepeat(Animator animator) {
            }
        });

        animator.start();
    }
}