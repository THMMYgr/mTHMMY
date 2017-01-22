package mthmmy.utils;

import com.google.firebase.crash.FirebaseCrash;

import gr.thmmy.mthmmy.utils.exceptions.UnknownException;

public class Report
{

    public static void v (String TAG, String message)
    {
        log("V", TAG, message);
    }

    public static void v (String TAG, String message, Throwable tr)
    {
        exception("V", TAG, message, tr);
    }

    public static void d (String TAG, String message)
    {
        log("D", TAG, message);
    }

    public static void d (String TAG, String message, Throwable tr)
    {
        exception("D", TAG, message, tr);
    }

    public static void i (String TAG, String message)
    {
        log("I", TAG, message);
    }

    public static void i (String TAG, String message, Throwable tr)
    {
        exception("I", TAG, message, tr);
    }

    public static void w (String TAG, String message)
    {
        log("W", TAG, message);
    }

    public static void w (String TAG, String message, Throwable tr)
    {
        exception("W", TAG, message, tr);
    }

    public static void e (String TAG, String message)
    {
        log("E", TAG, message);
    }

    public static void e (String TAG, String message, Throwable tr)
    {
        exception("E", TAG, message, tr);
    }

    public static void wtf (String TAG, String message)
    {
        log("WTF", TAG, message);
    }

    public static void wtf (String TAG, String message, Throwable tr)
    {
        exception("WTF", TAG, message, tr);
    }

    private static void log(String level, String TAG, String message)
    {
        FirebaseCrash.log(level + "/" + TAG + ": " + message);
        if(level.equals("E")||level.equals("WTF"))   //report only serious exceptions
            FirebaseCrash.report(new UnknownException("UnknownException"));
    }

    private static void exception(String level, String TAG, String message, Throwable tr)
    {
        FirebaseCrash.log(level + "/" + TAG + ": " + message + ": " + tr.getMessage());
        if(level.equals("E")||level.equals("WTF"))   //report only serious exceptions
            FirebaseCrash.report(tr);
    }

    /**
     * Does nothing in release.
     */
    public static void longMessage(String TAG, String level, String message) {return;}


}
