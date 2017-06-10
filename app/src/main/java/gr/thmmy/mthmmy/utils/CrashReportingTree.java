package gr.thmmy.mthmmy.utils;

import android.util.Log;

import com.google.firebase.crash.FirebaseCrash;

import timber.log.Timber.DebugTree;

public class CrashReportingTree extends DebugTree {
    
    @Override
    protected void log(int priority, String tag, String message, Throwable t) {
        if (priority == Log.VERBOSE || priority == Log.DEBUG) {
            return;
        }

        char level;

        if (priority == Log.INFO)
            level = 'I';
        else if (priority == Log.WARN)
            level = 'W';
        else if(priority == Log.ERROR)
            level = 'E';
        else
            level = 'A';

        FirebaseCrash.log(level + "/" + tag + ": " + message);

        if(priority == Log.ERROR)
        {
            if (t!=null)
                FirebaseCrash.report(t);
            else
                FirebaseCrash.report(new Exception(message));
        }

    }
}
