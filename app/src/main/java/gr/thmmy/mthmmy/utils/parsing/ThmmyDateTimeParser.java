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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import gr.thmmy.mthmmy.base.BaseApplication;
import timber.log.Timber;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class ThmmyDateTimeParser {
    private static final DateTimeParser[] parsers = {
            DateTimeFormat.forPattern("HH:mm:ss").getParser(),
            DateTimeFormat.forPattern("KK:mm:ss a").getParser(),
            DateTimeFormat.forPattern("MMMM d, Y, HH:mm:ss").getParser(),
            DateTimeFormat.forPattern("MMMM d, Y, KK:mm:ss a").getParser()
    };

    private static final DateTimeFormatter formatter = new DateTimeFormatterBuilder()
            .append(null, parsers)
            .toFormatter();

    private static final Locale greekLocale = Locale.forLanguageTag("el-GR");
    private static final Locale englishLocale = Locale.forLanguageTag("en-US");

    private static final Pattern pattern = Pattern.compile("\\s(1[2-9]|2[0-3]:)");

    public static String convertToTimestamp(String thmmyDateTime){
        DateTimeZone dtz;
        if(!BaseApplication.getInstance().getSessionManager().isLoggedIn())
            dtz = DateTimeZone.forID("Europe/Athens");
        else
            dtz = DateTimeZone.getDefault();

        //Add today's date for the first two cases
        if(thmmyDateTime.charAt(2)==':')
            thmmyDateTime = (new DateTime()).toString("MMMM d, Y, ") + thmmyDateTime;

        // For the stupid format 23:54:12 pm
        Matcher matcher = pattern.matcher(thmmyDateTime);
        if (matcher.find())
            thmmyDateTime = thmmyDateTime.replaceAll("\\s(am|pm|π.μ.|α.μ.)","");


        DateTime dateTime;
        try{
            thmmyDateTime = thmmyDateTime.replace("am","π.μ.");
            thmmyDateTime = thmmyDateTime.replace("pm","μ.μ.");
            dateTime=formatter.withZone(dtz).withLocale(greekLocale).parseDateTime(thmmyDateTime);
        }
        catch (IllegalArgumentException e1){
            Timber.d("Parsing DateTime using Greek Locale failed.");
            try{
                thmmyDateTime = thmmyDateTime.replace("π.μ.","am");
                thmmyDateTime = thmmyDateTime.replace("μ.μ.","pm");
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
