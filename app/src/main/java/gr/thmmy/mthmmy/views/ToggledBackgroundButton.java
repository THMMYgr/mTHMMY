package gr.thmmy.mthmmy.views;
import android.content.Context;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatButton;

public class ToggledBackgroundButton extends AppCompatButton {

    public ToggledBackgroundButton(Context context) {
        super(context);
    }

    public ToggledBackgroundButton(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ToggledBackgroundButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void setEnabled(boolean enabled) {
        setAlpha(enabled ? 1 : 0.5f);
        super.setEnabled(enabled);
    }
}

