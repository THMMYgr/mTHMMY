package gr.thmmy.mthmmy.utils.parsing;


import android.os.Build;

import androidx.annotation.RequiresApi;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.DateTimeFormatterBuilder;
import org.joda.time.format.DateTimeParser;

import java.util.Locale;

import gr.thmmy.mthmmy.base.BaseApplication;
import timber.log.Timber;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class ThmmyDateTimeParser {
    private static final DateTimeParser[] parsers = {
            DateTimeFormat.forPattern("HH:mm:ss").getParser(),
            DateTimeFormat.forPattern("HH:mm:ss a").getParser(),
            DateTimeFormat.forPattern("MMMM d, Y, HH:mm:ss").getParser(),
            DateTimeFormat.forPattern("MMMM d, Y, HH:mm:ss a").getParser()
    };

    private static final DateTimeFormatter formatter = new DateTimeFormatterBuilder()
            .append(null, parsers)
            .toFormatter();

    private static final Locale greekLocale = Locale.forLanguageTag("el-GR");
    private static final Locale englishLocale = Locale.forLanguageTag("en-US");

    public static String convertToTimestamp(String thmmyDateTime){
        DateTimeZone dtz;
        if(!BaseApplication.getInstance().getSessionManager().isLoggedIn())
            dtz = DateTimeZone.forID("Europe/Athens");
        else
            dtz = DateTimeZone.getDefault();

        //Add today's date for the first two cases
        if(Character.isDigit(thmmyDateTime.charAt(0)))
            thmmyDateTime = (new DateTime()).toString("MMMM d, Y, ") + thmmyDateTime;

        DateTime dateTime;
        try{
            dateTime=formatter.withZone(dtz).withLocale(greekLocale).parseDateTime(thmmyDateTime);
        }
        catch (IllegalArgumentException e1){
            Timber.i("Parsing DateTime using Greek Locale failed.");
            try{
                dateTime=formatter.withZone(dtz).withLocale(englishLocale).parseDateTime(thmmyDateTime);
            }
            catch (IllegalArgumentException e2){
                Timber.e("Couldn't parse DateTime %s", thmmyDateTime);
                return null;
            }
        }
        return Long.toString(dateTime.getMillis());
    }
}
