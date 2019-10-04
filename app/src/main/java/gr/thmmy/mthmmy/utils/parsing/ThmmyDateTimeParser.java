package gr.thmmy.mthmmy.utils.parsing;

import android.os.Build;

import androidx.annotation.RequiresApi;
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

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class ThmmyDateTimeParser {
    private static final DateTimeParser[] parsers = {
            DateTimeFormat.forPattern("HH:mm:ss").getParser(),
            DateTimeFormat.forPattern("hh:mm:ss a").getParser(),
            DateTimeFormat.forPattern("MMMM d, Y, HH:mm:ss").getParser(),
            DateTimeFormat.forPattern("MMMM d, Y, hh:mm:ss a").getParser(),
            DateTimeFormat.forPattern("d MMMM Y, HH:mm:ss").getParser(),
            DateTimeFormat.forPattern("d MMMM Y, HH:mm:ss").getParser(),
            DateTimeFormat.forPattern("Y-M-d, HH:mm:ss").getParser(),
            DateTimeFormat.forPattern("d-M-Y, HH:mm:ss").getParser()
    };

    private static final DateTimeFormatter formatter = new DateTimeFormatterBuilder()
            .append(null, parsers)
            .toFormatter();

    private static final Locale greekLocale = Locale.forLanguageTag("el-GR");
    private static final Locale englishLocale = Locale.forLanguageTag("en-US");

    private static final Pattern pattern = Pattern.compile("\\s(1[3-9]|2[0-3]:)");

    public static String convertToTimestamp(String thmmyDateTime){
        String originalDateTime = thmmyDateTime;
        DateTimeZone dtz = getDtz();

        //Add today's date for the first two cases
        if(thmmyDateTime.charAt(2)==':')
            thmmyDateTime = (new DateTime()).toString("MMMM d, Y, ") + thmmyDateTime;

        //Don't even ask
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
            Timber.d("Parsing DateTime %s using English Locale failed.", thmmyDateTime);
            try{
                DateFormatSymbols dfs = DateTimeUtils.getDateFormatSymbols(greekLocale);
                thmmyDateTime = thmmyDateTime.replace("am",dfs.getAmPmStrings()[0]);
                thmmyDateTime = thmmyDateTime.replace("pm",dfs.getAmPmStrings()[1]);
                dateTime=formatter.withZone(dtz).withLocale(greekLocale).parseDateTime(thmmyDateTime);
            }
            catch (IllegalArgumentException e2){
                Timber.d("Parsing DateTime %s using Greek Locale failed too.", thmmyDateTime);
                Timber.e("Couldn't convert DateTime %s to timestamp!", originalDateTime);
                return null;
            }
        }
        return Long.toString(dateTime.getMillis());
    }

    @VisibleForTesting
    private static DateTimeZone getDtz(){
        if(!BaseApplication.getInstance().getSessionManager().isLoggedIn())
            return DateTimeZone.forID("Europe/Athens");
        else
            return DateTimeZone.getDefault();
    }
}
