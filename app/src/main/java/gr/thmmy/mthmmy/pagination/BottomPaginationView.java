package gr.thmmy.mthmmy.pagination;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Rect;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import gr.thmmy.mthmmy.R;

public class BottomPaginationView extends LinearLayout {

    /**
     * Holds the initial time delay before a click on bottom navigation bar is considered long
     */
    private final long INITIAL_DELAY = 500;
    private boolean autoIncrement = false;
    private boolean autoDecrement = false;
    /**
     * Holds the number of pages to be added or subtracted from current page on each step while a
     * long click is held in either next or previous buttons
     */
    private static final int SMALL_STEP = 1;
    /**
     * Holds the number of pages to be added or subtracted from current page on each step while a
     * long click is held in either first or last buttons
     */
    private static final int LARGE_STEP = 10;
    /**
     * Used for handling bottom navigation bar's buttons long click user interactions
     */
    private final Handler repeatUpdateHandler = new Handler();

    private ImageButton firstPage, previousPage, nextPage, lastPage;
    private TextView pageIndicator;

    private int onDownPageIndex, indicatorPageIndex, totalPageCount;

    private OnPageRequestedListener onPageRequestedListener;

    public interface OnPageRequestedListener {
        void onPageRequested(int index);
    }

    public BottomPaginationView(Context context) {
        this(context, null);
    }

    public BottomPaginationView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        LayoutInflater inflater = LayoutInflater.from(context);
        inflater.inflate(R.layout.pagination, this, true);

        firstPage = findViewById(R.id.page_first_button);
        previousPage = findViewById(R.id.page_previous_button);
        pageIndicator = findViewById(R.id.page_indicator);
        nextPage = findViewById(R.id.page_next_button);
        lastPage = findViewById(R.id.page_last_button);

        initDecrementButton(firstPage, LARGE_STEP);
        initDecrementButton(previousPage, SMALL_STEP);
        initIncrementButton(nextPage, SMALL_STEP);
        initIncrementButton(lastPage, LARGE_STEP);
    }

    public void setOnPageRequestedListener(OnPageRequestedListener onPageRequestedListener) {
        this.onPageRequestedListener = onPageRequestedListener;
    }

    public boolean setIndicatedPageIndex(int index) {
        if (index != indicatorPageIndex) {
            this.indicatorPageIndex = index;
            updateUI();
            return true;
        } else return false;
    }

    public void setTotalPageCount(int totalPageCount) {
        this.totalPageCount = totalPageCount;
        updateUI();
    }

    public void updateUI() {
        pageIndicator.setText(indicatorPageIndex + "/" + totalPageCount);
    }

    /**
     * This class is used to implement the repetitive incrementPageRequestValue/decrementPageRequestValue
     * of page value when long pressing one of the page navigation buttons.
     */
    private class RepetitiveUpdater implements Runnable {
        private final int step;

        /**
         * @param step number of pages to add/subtract on each repetition
         */
        RepetitiveUpdater(int step) {
            this.step = step;
        }

        public void run() {
            long REPEAT_DELAY = 250;
            if (autoIncrement) {
                incrementPageIndicator(step);
                repeatUpdateHandler.postDelayed(new BottomPaginationView.RepetitiveUpdater(step), REPEAT_DELAY);
            } else if (autoDecrement) {
                decrementPageIndicator(step);
                repeatUpdateHandler.postDelayed(new BottomPaginationView.RepetitiveUpdater(step), REPEAT_DELAY);
            }
        }
    }

    public boolean incrementPageIndicator(int step) {
        int oldIndicatorIndex = indicatorPageIndex;
        if (oldIndicatorIndex <= totalPageCount - step)
            setIndicatedPageIndex(indicatorPageIndex + step);
        else
            setIndicatedPageIndex(totalPageCount);
        return oldIndicatorIndex != indicatorPageIndex;
    }

    public boolean decrementPageIndicator(int step) {
        int oldIndicatorIndex = indicatorPageIndex;
        if (oldIndicatorIndex > step)
            setIndicatedPageIndex(indicatorPageIndex - step);
        else
            setIndicatedPageIndex(1);
        return oldIndicatorIndex != indicatorPageIndex;
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initIncrementButton(ImageButton increment, final int step) {
        // Increment once for a click
        increment.setOnClickListener(v -> {
            if (!autoIncrement && step == LARGE_STEP) {
                boolean indicatorChanged = setIndicatedPageIndex(totalPageCount);
                if (indicatorChanged) onPageRequestedListener.onPageRequested(indicatorPageIndex);
            } else if (!autoIncrement) {
                boolean indicatorChanged = incrementPageIndicator(1);
                if (indicatorChanged) onPageRequestedListener.onPageRequested(indicatorPageIndex);
            }
        });

        // Auto increment for a long click
        increment.setOnLongClickListener(
                arg0 -> {
                    paginationDisable(arg0);
                    autoIncrement = true;
                    repeatUpdateHandler.postDelayed(new BottomPaginationView.RepetitiveUpdater(step), INITIAL_DELAY);
                    return false;
                }
        );

        // When the button is released
        increment.setOnTouchListener(new View.OnTouchListener() {
            private Rect rect;

            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    onDownPageIndex = indicatorPageIndex;
                    rect = new Rect(v.getLeft(), v.getTop(), v.getRight(), v.getBottom());
                } else if (rect != null && event.getAction() == MotionEvent.ACTION_UP && autoIncrement) {
                    autoIncrement = false;
                    paginationEnabled(true);
                    onPageRequestedListener.onPageRequested(indicatorPageIndex);
                } else if (rect != null && event.getAction() == MotionEvent.ACTION_MOVE) {
                    if (!rect.contains(v.getLeft() + (int) event.getX(), v.getTop() + (int) event.getY())) {
                        autoIncrement = false;
                        setIndicatedPageIndex(onDownPageIndex);
                        paginationEnabled(true);
                    }
                }
                return false;
            }
        });
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initDecrementButton(ImageButton decrement, final int step) {
        // Decrement once for a click
        decrement.setOnClickListener(v -> {
            if (!autoDecrement && step == LARGE_STEP) {
                boolean indicatorChanged = setIndicatedPageIndex(1);
                if (indicatorChanged) onPageRequestedListener.onPageRequested(indicatorPageIndex);
            } else if (!autoDecrement) {
                boolean indicatorChanged = decrementPageIndicator(1);
                if (indicatorChanged) onPageRequestedListener.onPageRequested(indicatorPageIndex);
            }
        });

        // Auto decrement for a long click
        decrement.setOnLongClickListener(
                arg0 -> {
                    paginationDisable(arg0);
                    autoDecrement = true;
                    repeatUpdateHandler.postDelayed(new RepetitiveUpdater(step), INITIAL_DELAY);
                    return false;
                }
        );

        // When the button is released
        decrement.setOnTouchListener(new View.OnTouchListener() {
            private Rect rect;

            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    onDownPageIndex = indicatorPageIndex;
                    rect = new Rect(v.getLeft(), v.getTop(), v.getRight(), v.getBottom());
                } else if (event.getAction() == MotionEvent.ACTION_UP && autoDecrement) {
                    autoDecrement = false;
                    paginationEnabled(true);
                    onPageRequestedListener.onPageRequested(indicatorPageIndex);
                } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
                    if (rect != null &&
                            !rect.contains(v.getLeft() + (int) event.getX(), v.getTop() + (int) event.getY())) {
                        autoIncrement = false;
                        setIndicatedPageIndex(onDownPageIndex);
                        paginationEnabled(true);
                    }
                }
                return false;
            }
        });
    }

    public void paginationDisable(View exception) {
        if (exception == firstPage) {
            previousPage.setEnabled(false);
            nextPage.setEnabled(false);
            lastPage.setEnabled(false);
        } else if (exception == previousPage) {
            firstPage.setEnabled(false);
            nextPage.setEnabled(false);
            lastPage.setEnabled(false);
        } else if (exception == nextPage) {
            firstPage.setEnabled(false);
            previousPage.setEnabled(false);
            lastPage.setEnabled(false);
        } else if (exception == lastPage) {
            firstPage.setEnabled(false);
            previousPage.setEnabled(false);
            nextPage.setEnabled(false);
        } else {
            paginationEnabled(false);
        }
    }

    public void paginationEnabled(boolean enabled) {
        firstPage.setEnabled(enabled);
        previousPage.setEnabled(enabled);
        nextPage.setEnabled(enabled);
        lastPage.setEnabled(enabled);
    }
}
