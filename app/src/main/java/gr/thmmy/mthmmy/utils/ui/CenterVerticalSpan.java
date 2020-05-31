package gr.thmmy.mthmmy.utils.ui;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.text.style.ReplacementSpan;

import androidx.annotation.NonNull;

public class CenterVerticalSpan extends ReplacementSpan {
    @Override
    public int getSize(@NonNull Paint paint, CharSequence text, int start, int end, Paint.FontMetricsInt fm) {
        text = text.subSequence(start, end);
        return (int) paint.measureText(text.toString());
    }

    @Override
    public void draw(@NonNull Canvas canvas, CharSequence text, int start, int end, float x, int top, int y, int bottom,@NonNull Paint paint) {
        text = text.subSequence(start, end);
        Rect charSize = new Rect();
        paint.getTextBounds(text.toString(), 0, 1, charSize);
        canvas.drawText(text.toString(), x, (bottom + charSize.height()) / 2f, paint);
    }
}