package gr.thmmy.mthmmy.utils.ui;

import android.app.Activity;
import android.content.Context;

public class GlideUtils {
    public static boolean isValidContextForGlide(final Context context) {
        if (context == null)
            return false;

        if (context instanceof Activity) {
            final Activity activity = (Activity) context;
            return !activity.isDestroyed() && !activity.isFinishing();
        }
        return true;
    }
}
