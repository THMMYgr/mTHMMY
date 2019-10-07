package gr.thmmy.mthmmy.utils;

import androidx.annotation.VisibleForTesting;

import static android.text.format.DateUtils.DAY_IN_MILLIS;
import static android.text.format.DateUtils.HOUR_IN_MILLIS;
import static android.text.format.DateUtils.MINUTE_IN_MILLIS;
import static android.text.format.DateUtils.SECOND_IN_MILLIS;
import static android.text.format.DateUtils.YEAR_IN_MILLIS;

public class DateTimeUtils {
    //TODO: move this function to ThmmyDateTimeParser class once KitKat support is dropped
    public static String convertDateTime(String dateTime, boolean removeSeconds){
        //Convert e.g. Today at 12:16:48 -> 12:16:48, but October 03, 2019, 16:40:18 remains as is
        if (!dateTime.contains(","))
            dateTime = dateTime.replaceAll(".+? ([0-9])", "$1");

        //Remove seconds
        if(removeSeconds)
            dateTime = dateTime.replaceAll("(.+?)(:[0-5][0-9])($|\\s)", "$1$3");

        return dateTime;
    }

    private static final long MONTH_IN_MILLIS = 30*DAY_IN_MILLIS;
    private static final long DECADE_IN_MILLIS = 10*YEAR_IN_MILLIS;

    @VisibleForTesting
    static String getRelativeTimeSpanString(long time) {
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
            if(count<4 && mod>10 && mod<50)
                format = format + mod +"m";
            else if(mod >= 30)
                count += 1;
        } else if (duration < 26*DAY_IN_MILLIS) {
            count = duration/DAY_IN_MILLIS;
            format = "%d day";
            mod = duration % DAY_IN_MILLIS;
            if(mod >= 12*HOUR_IN_MILLIS)
                count += 1;
            if(count>1)
                format = format + 's';
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
