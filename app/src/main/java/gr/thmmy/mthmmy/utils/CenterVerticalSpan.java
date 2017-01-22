package gr.thmmy.mthmmy.utils;

import android.text.TextPaint;
import android.text.style.SuperscriptSpan;
import android.util.Log;

public class CenterVerticalSpan extends SuperscriptSpan {
    public CenterVerticalSpan() {
    }

    @Override
    public void updateDrawState(TextPaint textPaint) {
        textPaint.baselineShift -= 7f;
    }

    @Override
    public void updateMeasureState(TextPaint tp) {
        updateDrawState(tp);
    }
}