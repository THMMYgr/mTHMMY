package gr.thmmy.mthmmy.utils.parsing;

import androidx.annotation.VisibleForTesting;

import org.joda.time.DateTime;
import org.joda.time.DateTimeUtils;
import org.joda.time.DateTimeZone;
import org.joda.time.IllegalInstantException;
import org.joda.time.LocalDateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.DateTimeFormatterBuilder;
import org.joda.time.format.DateTimeParser;

import java.text.DateFormatSymbols;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import gr.thmmy.mthmmy.base.BaseApplication;
import timber.log.Timber;

public class ThmmyDateTimeParser {
    private static final DateTimeParser[] parsers = {
            DateTimeFormat.forPattern("hh:mm:ss a").getParser(),
            DateTimeFormat.forPattern("HH:mm:ss").getParser(),
            DateTimeFormat.forPattern("MMMM d, Y, hh:mm:ss a").getParser(),
            DateTimeFormat.forPattern("MMMM d, Y, HH:mm:ss").getParser(),
            DateTimeFormat.forPattern("d MMMM Y, HH:mm:ss").getParser(),
            DateTimeFormat.forPattern("Y-M-d, HH:mm:ss").getParser(),
            DateTimeFormat.forPattern("d-M-Y, HH:mm:ss").getParser()
    };

    private static final DateTimeFormatter formatter = new DateTimeFormatterBuilder()
            .append(null, parsers)
            .toFormatter();

    private static final Locale greekLocale = Locale.forLanguageTag("el-GR");
    private static final Locale englishLocale = Locale.forLanguageTag("en-US");

    private static final Pattern pattern = Pattern.compile("\\s((1[3-9]|2[0-3]):)");

    private ThmmyDateTimeParser() {}

    public static String convertToTimestamp(String thmmyDateTime) {
        Timber.v("Will attempt to convert %s to timestamp.", thmmyDateTime);
        String originalDateTime = thmmyDateTime;

        DateTimeZone dtz;

        // This was added for people who briefly travelled abroad and didn't change the displayed time in their profile settings
        final boolean useeGreekTimezone = BaseApplication.getInstance().isUseGreekTimezoneEnabled();

        if(useeGreekTimezone)
            dtz = DateTimeZone.forID("Europe/Athens");
        else
            dtz = DateTimeZone.getDefault();

        // Remove any unnecessary "Today at" strings
        thmmyDateTime = purifyTodayDateTime(thmmyDateTime);

        // Add today's date for the first two cases
        if (thmmyDateTime.charAt(2) == ':')
            thmmyDateTime = (new DateTime()).toString("MMMM d, Y, ") + thmmyDateTime;

        // Don't even ask
        if (thmmyDateTime.contains("am"))
            thmmyDateTime = thmmyDateTime.replaceAll("\\s00:", " 12:");

        // For the stupid format 23:54:12 pm
        Matcher matcher = pattern.matcher(thmmyDateTime);
        if (matcher.find())
            thmmyDateTime = thmmyDateTime.replaceAll("\\spm", "");

        DateTime dateTime;
        LocalDateTime localDateTime;
        try {
            localDateTime = formatter.withLocale(englishLocale).parseLocalDateTime(thmmyDateTime);
        } catch (IllegalArgumentException e1) {
            Timber.v("Parsing DateTime %s using English Locale failed.", thmmyDateTime);
            try {
                DateFormatSymbols dfs = DateTimeUtils.getDateFormatSymbols(greekLocale);
                thmmyDateTime = thmmyDateTime.replace("am", dfs.getAmPmStrings()[0]);
                thmmyDateTime = thmmyDateTime.replace("pm", dfs.getAmPmStrings()[1]);
                Timber.v("Attempting to parse DateTime %s using Greek Locale...", thmmyDateTime);
                localDateTime = formatter.withLocale(greekLocale).parseLocalDateTime(thmmyDateTime);
            } catch (IllegalArgumentException e2) {
                Timber.v("Parsing DateTime %s using Greek Locale failed too.", thmmyDateTime);
                Timber.e("Couldn't convert DateTime to timestamp (original: \"%s\", modified: \"%s\")!",
                        originalDateTime, thmmyDateTime);
                return null;
            }
        }

        // Ensure DST time overlaps/ gaps are handled properly
        try{
            // For autumn overlaps
            dateTime = localDateTime.toDateTime(dtz).withEarlierOffsetAtOverlap();
        } catch (IllegalInstantException e2) {
            // For spring gaps
            dateTime = localDateTime.plusHours(1).toDateTime(dtz);
        }

        String timestamp = Long.toString(dateTime.getMillis());
        Timber.v("DateTime %s was converted to %s, or %s", originalDateTime, timestamp, dateTime.toString());

        return timestamp;
    }

    public static String simplifyDateTime(String dateTime) {
        return removeSeconds(purifyTodayDateTime(dateTime));
    }

    // Converts e.g. Today at 12:16:48 -> 12:16:48, but October 03, 2019, 16:40:18 remains as is
    @VisibleForTesting
    static String purifyTodayDateTime(String dateTime) {
        return dateTime.replaceAll("(Today at |Σήμερα στις )(.+)", "$2");
    }

    // Converts e.g. 12:16:48 -> 12:16, October 03, 2019, 16:40:18 -> 12:16 October 03, 2019, 16:40
    private static String removeSeconds(String dateTime) {
        return dateTime.replaceAll("(.*):\\d+($|\\s.*)", "$1$2");
    }
}
