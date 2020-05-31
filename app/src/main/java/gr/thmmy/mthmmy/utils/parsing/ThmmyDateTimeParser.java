package gr.thmmy.mthmmy.utils.parsing;

import androidx.annotation.VisibleForTesting;

import org.joda.time.DateTime;
import org.joda.time.DateTimeUtils;
import org.joda.time.DateTimeZone;
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

    //TODO: Replace with Locale.forLanguageTag() (with "el-GR","en-US") when KitKat support is dropped
    private static final Locale greekLocale = new Locale("el", "GR");
    private static final Locale englishLocale = new Locale("en", "US");

    private static final Pattern pattern = Pattern.compile("\\s((1[3-9]|2[0-3]):)");

    private ThmmyDateTimeParser(){}

    public static String convertToTimestamp(String thmmyDateTime){
        Timber.v("Will attempt to convert %s to timestamp.", thmmyDateTime);
        String originalDateTime = thmmyDateTime;
        DateTimeZone dtz = getDtz();

        // Remove any unnecessary "Today at" strings
        thmmyDateTime = purifyTodayDateTime(thmmyDateTime);

        // Add today's date for the first two cases
        if(thmmyDateTime.charAt(2)==':')
            thmmyDateTime = (new DateTime()).toString("MMMM d, Y, ") + thmmyDateTime;

        // Don't even ask
        if(thmmyDateTime.contains("am"))
            thmmyDateTime = thmmyDateTime.replaceAll("\\s00:"," 12:");

        // For the stupid format 23:54:12 pm
        Matcher matcher = pattern.matcher(thmmyDateTime);
        if (matcher.find())
            thmmyDateTime = thmmyDateTime.replaceAll("\\spm","");

        DateTime dateTime;
        try{
            dateTime=formatter.withZone(dtz).withLocale(englishLocale).parseDateTime(thmmyDateTime);
        }
        catch (IllegalArgumentException e1){
            Timber.v("Parsing DateTime %s using English Locale failed.", thmmyDateTime);
            try{
                DateFormatSymbols dfs = DateTimeUtils.getDateFormatSymbols(greekLocale);
                thmmyDateTime = thmmyDateTime.replace("am",dfs.getAmPmStrings()[0]);
                thmmyDateTime = thmmyDateTime.replace("pm",dfs.getAmPmStrings()[1]);
                Timber.v("Attempting to parse DateTime %s using Greek Locale...", thmmyDateTime);
                dateTime=formatter.withZone(dtz).withLocale(greekLocale).parseDateTime(thmmyDateTime);
            }
            catch (IllegalArgumentException e2){
                Timber.d("Parsing DateTime %s using Greek Locale failed too.", thmmyDateTime);
                Timber.e("Couldn't convert DateTime %s to timestamp!", originalDateTime);
                return null;
            }
        }
        String timestamp = Long.toString(dateTime.getMillis());
        Timber.v("DateTime %s was converted to %s, or %s", originalDateTime, timestamp, dateTime.toString());

        return timestamp;
    }

    public static String simplifyDateTime(String dateTime){
        return removeSeconds(purifyTodayDateTime(dateTime));
    }

    // Converts e.g. Today at 12:16:48 -> 12:16:48, but October 03, 2019, 16:40:18 remains as is
    @VisibleForTesting
    static String purifyTodayDateTime(String dateTime){
        return dateTime.replaceAll("(Today at |Σήμερα στις )(.+)", "$2");
    }

    // Converts e.g. 12:16:48 -> 12:16, October 03, 2019, 16:40:18 -> 12:16 October 03, 2019, 16:40
    private static String removeSeconds(String dateTime){
        return dateTime.replaceAll("(.*):\\d+($|\\s.*)", "$1$2");
    }

    @VisibleForTesting
    private static DateTimeZone getDtz(){
        if(!BaseApplication.getInstance().getSessionManager().isLoggedIn())
            return DateTimeZone.forID("Europe/Athens");
        else
            return DateTimeZone.getDefault();
    }
}
