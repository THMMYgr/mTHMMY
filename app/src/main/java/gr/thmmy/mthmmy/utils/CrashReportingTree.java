package gr.thmmy.mthmmy.utils;

import android.util.Log;

import com.google.firebase.crash.FirebaseCrash;

import gr.thmmy.mthmmy.utils.exceptions.UnknownException;
import timber.log.Timber;

public class CrashReportingTree extends Timber.Tree {
    @Override
    protected void log(int priority, String tag, String message, Throwable t) {
        if (priority == Log.VERBOSE || priority == Log.DEBUG) {
            return;
        }

        String level="A";

        if (priority == Log.INFO)
            level = "I";
        else if (priority == Log.WARN)
            level = "W";
        else if(priority == Log.ERROR)
            level = "E";

        FirebaseCrash.log(level + "/" + tag + ": " + message);

        if(t==null)
            t = new UnknownException("UnknownException");

        if ((priority == Log.ERROR))
            FirebaseCrash.report(t);
    }
}
