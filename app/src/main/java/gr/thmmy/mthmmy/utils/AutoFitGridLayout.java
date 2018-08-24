package gr.thmmy.mthmmy.utils;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.GridLayout;
import gr.thmmy.mthmmy.R;

public class AutoFitGridLayout extends GridLayout {
    private int columnWidth;
    private int defaultColumnCount;

    public AutoFitGridLayout(Context context) {
        super(context);
        init(context, null, 0);
    }

    public AutoFitGridLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs, 0);
    }

    public AutoFitGridLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);
    }

    public void init(Context context, AttributeSet attrs, int defStyleAttr) {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.AutoFitGridLayout, 0, defStyleAttr);
        try {
            columnWidth = a.getDimensionPixelSize(R.styleable.AutoFitGridLayout_columnWidth, 0);

            int[] set = {android.R.attr.columnCount};
            a = context.obtainStyledAttributes(attrs, set, 0, defStyleAttr);
            defaultColumnCount = a.getInt(0, 6);
        } finally {
            a.recycle();
        }
        setColumnCount(1);
    }

    @Override
    protected void onMeasure(int widthSpec, int heightSpec) {
        super.onMeasure(widthSpec, heightSpec);

        int width = MeasureSpec.getSize(widthSpec);
        if (columnWidth > 0 && width > 0) {
            int totalSpace = width - getPaddingRight() - getPaddingLeft();
            int columnCount = Math.max(1, totalSpace / columnWidth);
            setColumnCount(columnCount);
        } else {
            setColumnCount(defaultColumnCount);
        }
    }
}
