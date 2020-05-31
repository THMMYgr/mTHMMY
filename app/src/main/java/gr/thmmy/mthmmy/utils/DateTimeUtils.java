package gr.thmmy.mthmmy.utils;

import androidx.annotation.VisibleForTesting;

import static android.text.format.DateUtils.DAY_IN_MILLIS;
import static android.text.format.DateUtils.HOUR_IN_MILLIS;
import static android.text.format.DateUtils.MINUTE_IN_MILLIS;
import static android.text.format.DateUtils.SECOND_IN_MILLIS;
import static android.text.format.DateUtils.YEAR_IN_MILLIS;

public class DateTimeUtils {

    private static final long MONTH_IN_MILLIS = 30*DAY_IN_MILLIS;
    private static final long DECADE_IN_MILLIS = 10*YEAR_IN_MILLIS;

    @VisibleForTesting
    public static String getRelativeTimeSpanString(long time) {
        long now = System.currentTimeMillis();

        boolean past = (now >= time);
        long duration = Math.abs(now - time);
        String format;
        long count, mod;
        if(duration < 45*SECOND_IN_MILLIS)
            return "just now";
        else if (duration < 45*MINUTE_IN_MILLIS) {
            count = duration/MINUTE_IN_MILLIS;
            mod = duration % MINUTE_IN_MILLIS;
            if(mod >= 30*SECOND_IN_MILLIS)
                count += 1;
            format = "%dm";
        } else if (duration < 22*HOUR_IN_MILLIS) {
            count = duration/HOUR_IN_MILLIS;
            format = "%dh";
            mod = (duration%HOUR_IN_MILLIS)/MINUTE_IN_MILLIS;
            if(count<3 && mod>9 && mod<51){
                if(count==0)
                    format = mod +"m";
                else
                    format = format + " " + mod +"m";
            }
            else if(mod >= 30)
                count += 1;
        } else if (duration < 26*DAY_IN_MILLIS) {
            count = duration/DAY_IN_MILLIS;
            format = "%dd";
            mod = duration % DAY_IN_MILLIS;
            if(mod >= 12*HOUR_IN_MILLIS)
                count += 1;
        } else if (duration < 320*DAY_IN_MILLIS) {
            count = duration/MONTH_IN_MILLIS;
            format = "%d month";
            mod = duration % MONTH_IN_MILLIS;
            if(mod >= 15*DAY_IN_MILLIS)
                count += 1;
            if(count>1)
                format = format + 's';
        } else if (duration < DECADE_IN_MILLIS) {
            count = duration/YEAR_IN_MILLIS;
            format = "%d year";
            mod = duration % YEAR_IN_MILLIS;
            if(mod >= 183*DAY_IN_MILLIS)
                count += 1;
            if(count>1)
                format = format + 's';
        }
        else
            return past ? "a long time ago": "in the distant future";

        format = past ? format : "in " + format;
        return String.format(format, (int) count);
    }
}
