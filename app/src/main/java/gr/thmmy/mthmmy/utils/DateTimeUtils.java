package gr.thmmy.mthmmy.utils;

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

    private static final long MONTH_IN_MILLIS = DAY_IN_MILLIS*30;
    private static final long DECADE_IN_MILLIS = YEAR_IN_MILLIS*10;

    static CharSequence getRelativeTimeSpanString(long time, long now, long minResolution) {
        boolean past = (now >= time);
        long duration = Math.abs(now - time);
        String format;
        long count;
        if (duration < MINUTE_IN_MILLIS && minResolution < MINUTE_IN_MILLIS) {
            count = duration / SECOND_IN_MILLIS;
            format = "%d sec";
        } else if (duration < HOUR_IN_MILLIS && minResolution < HOUR_IN_MILLIS) {
            count = duration / MINUTE_IN_MILLIS;
            format = "%d min";
        } else if (duration < DAY_IN_MILLIS && minResolution < DAY_IN_MILLIS) {
            count = duration / HOUR_IN_MILLIS;
            format = "%d hour";
            if(count>1)
                format = format + 's';
        } else if (duration < MONTH_IN_MILLIS && minResolution < MONTH_IN_MILLIS) {
            count = duration / DAY_IN_MILLIS;
            format = "%d day";
            if(count>1)
                format = format + 's';
        } else if (duration < YEAR_IN_MILLIS && minResolution < YEAR_IN_MILLIS) {
            count = duration / MONTH_IN_MILLIS;
            format = "%d month";
            if(count>1)
                format = format + 's';
        } else if (duration < DECADE_IN_MILLIS && minResolution < DECADE_IN_MILLIS) {
            count = duration / YEAR_IN_MILLIS;
            format = "%d year";
            if(count>1)
                format = format + 's';
        }
        else
            return past ? "a long time ago": "in the distant future";

        format = past ? format : "in " + format;
        return String.format(format, (int) count);
    }
}
